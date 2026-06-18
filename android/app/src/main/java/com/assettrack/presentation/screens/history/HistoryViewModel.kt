package com.assettrack.presentation.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assettrack.domain.model.Transaction
import com.assettrack.domain.model.TransactionType
import com.assettrack.domain.repository.AssetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val transactions: List<Transaction> = emptyList(),
    val searchQuery: String = "",
    val filterType: TransactionType? = null,
    val expandedId: String? = null
) {
    val filtered: List<Transaction>
        get() = if (filterType == null) transactions
                else transactions.filter { it.type == filterType }
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: AssetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(300L)
                .flatMapLatest { query ->
                    if (query.isBlank()) repository.observeTransactions()
                    else repository.searchTransactions(query)
                }
                .collect { list ->
                    _uiState.update { it.copy(transactions = list) }
                }
        }
    }

    fun onSearchChange(q: String) {
        _searchQuery.value = q
        _uiState.update { it.copy(searchQuery = q) }
    }

    fun onFilterType(type: TransactionType?) {
        _uiState.update { it.copy(filterType = type) }
    }

    fun onToggleExpand(id: String) {
        _uiState.update { state ->
            state.copy(expandedId = if (state.expandedId == id) null else id)
        }
    }
}
