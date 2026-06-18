package com.assettrack.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.assettrack.domain.model.Asset
import com.assettrack.domain.model.AssetStatus
import com.assettrack.domain.repository.AssetRepository
import com.assettrack.worker.SyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

enum class DashboardTab { GUDANG, DI_LUAR }

enum class SyncStatus { IDLE, SYNCING, SUCCESS, ERROR }

data class DashboardUiState(
    val assets: List<Asset> = emptyList(),
    val activeTab: DashboardTab = DashboardTab.GUDANG,
    val searchQuery: String = "",
    val pendingSyncCount: Int = 0,
    val isLoading: Boolean = false,
    // Sync status
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val lastSyncTime: String = "",
    val syncMessage: String = ""
) {
    val filteredAssets: List<Asset>
        get() = assets.filter { asset ->
            when (activeTab) {
                DashboardTab.GUDANG  -> asset.status == AssetStatus.AVAILABLE ||
                                        asset.status == AssetStatus.MAINTENANCE
                DashboardTab.DI_LUAR -> asset.status == AssetStatus.BORROWED
            }
        }

    val isSyncing: Boolean get() = syncStatus == SyncStatus.SYNCING
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: AssetRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        // Observe asset list reactively dari Room
        viewModelScope.launch {
            _searchQuery
                .debounce(300L)
                .flatMapLatest { query ->
                    if (query.isBlank()) repository.observeAssets()
                    else repository.searchAssets(query)
                }
                .collect { assets ->
                    _uiState.update { it.copy(assets = assets) }
                }
        }

        // Observe pending sync count
        viewModelScope.launch {
            repository.observePendingAssetCount().collect { count ->
                _uiState.update { it.copy(pendingSyncCount = count) }
            }
        }

        // Observe WorkManager state untuk update UI sync status
        viewModelScope.launch {
            workManager.getWorkInfosByTagFlow(SyncWorker.WORK_NAME)
                .collect { workInfoList ->
                    val workInfo = workInfoList.firstOrNull() ?: return@collect
                    when (workInfo.state) {
                        WorkInfo.State.RUNNING -> {
                            _uiState.update {
                                it.copy(
                                    syncStatus = SyncStatus.SYNCING,
                                    syncMessage = "Menyinkronkan data..."
                                )
                            }
                        }
                        WorkInfo.State.SUCCEEDED -> {
                            val now = SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault())
                                .format(Date())
                            _uiState.update {
                                it.copy(
                                    syncStatus = SyncStatus.SUCCESS,
                                    lastSyncTime = now,
                                    syncMessage = "Sinkronisasi berhasil"
                                )
                            }
                        }
                        WorkInfo.State.FAILED -> {
                            _uiState.update {
                                it.copy(
                                    syncStatus = SyncStatus.ERROR,
                                    syncMessage = "Sinkronisasi gagal, coba lagi"
                                )
                            }
                        }
                        else -> {
                            // ENQUEUED, BLOCKED, CANCELLED — tidak perlu update
                        }
                    }
                }
        }

        // Mulai periodic sync di background
        SyncWorker.enqueuePeriodicSync(workManager)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onTabChange(tab: DashboardTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    // ── Sync manual (dipanggil dari tombol di UI) ─────────────────────────────
    fun onSyncNow() {
        if (_uiState.value.isSyncing) return   // cegah double trigger
        _uiState.update {
            it.copy(syncStatus = SyncStatus.SYNCING, syncMessage = "Memulai sinkronisasi...")
        }
        SyncWorker.triggerImmediateSync(workManager)
    }

    fun onDismissSyncMessage() {
        _uiState.update { it.copy(syncStatus = SyncStatus.IDLE, syncMessage = "") }
    }
}
