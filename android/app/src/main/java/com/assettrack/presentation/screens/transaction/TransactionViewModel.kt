package com.assettrack.presentation.screens.transaction

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assettrack.data.LocationHelper
import com.assettrack.data.MasterBarangManager
import com.assettrack.domain.model.Asset
import com.assettrack.domain.model.AssetStatus
import com.assettrack.domain.model.Transaction
import com.assettrack.domain.repository.AssetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TransactionMode { CHECK_OUT, CHECK_IN }
enum class EvidencePickerMode { NONE, PHOTO, PDF }

data class TransactionUiState(
    val mode: TransactionMode = TransactionMode.CHECK_OUT,
    // Scan / lookup
    val manualInput: String = "",
    val resolvedAsset: Asset? = null,
    val lookupError: String? = null,
    val isLookingUp: Boolean = false,
    val showScanner: Boolean = false,
    // Form
    val recipientName: String = "",
    val destination: String = "",
    val notes: String = "",
    // GPS
    val latitude: Double? = null,
    val longitude: Double? = null,
    val gpsAccuracy: Float? = null,
    val isGpsLoading: Boolean = false,
    // Bukti transaksi
    val evidenceUri: Uri? = null,
    val evidenceType: String? = null,   // "PHOTO" | "PDF"
    val evidencePickerMode: EvidencePickerMode = EvidencePickerMode.NONE,
    val isSavingEvidence: Boolean = false,
    val savedEvidencePath: String? = null,
    // Submit
    val isSubmitting: Boolean = false,
    val lastTransaction: Transaction? = null,
    val submitError: String? = null
) {
    val isFormValid: Boolean
        get() = resolvedAsset != null &&
                (mode == TransactionMode.CHECK_IN || recipientName.isNotBlank())

    val hasEvidence: Boolean get() = evidenceUri != null || savedEvidencePath != null

    val evidenceLabel: String get() = when {
        savedEvidencePath != null -> {
            val name = savedEvidencePath.substringAfterLast("/")
            "📎 $name"
        }
        evidenceUri != null -> "📎 ${evidenceType ?: "File"} terpilih"
        else -> ""
    }
}

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: AssetRepository,
    private val locationHelper: LocationHelper,
    private val masterBarangManager: MasterBarangManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    init { fetchGps() }

    // ── Mode ──────────────────────────────────────────────────────────────────

    fun onModeChange(mode: TransactionMode) {
        _uiState.update {
            it.copy(
                mode = mode,
                resolvedAsset = null,
                lookupError = null,
                manualInput = "",
                evidenceUri = null,
                evidenceType = null,
                savedEvidencePath = null
            )
        }
    }

    // ── Scanner ───────────────────────────────────────────────────────────────

    fun onShowScanner()    { _uiState.update { it.copy(showScanner = true) } }
    fun onDismissScanner() { _uiState.update { it.copy(showScanner = false) } }

    fun onScanResult(value: String) {
        _uiState.update { it.copy(showScanner = false, manualInput = value) }
        lookupBySn(value)
    }

    fun onManualInputChange(v: String) {
        _uiState.update { it.copy(manualInput = v, lookupError = null) }
    }

    fun onVerifyId() { lookupBySn(_uiState.value.manualInput.trim()) }

    private fun lookupBySn(sn: String) {
        if (sn.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLookingUp = true, lookupError = null, resolvedAsset = null) }
            val asset = repository.getAssetBySerialNumber(sn)
            if (asset == null) {
                _uiState.update {
                    it.copy(isLookingUp = false, lookupError = "Barang '$sn' tidak ditemukan.")
                }
            } else {
                val modeError = when (_uiState.value.mode) {
                    TransactionMode.CHECK_OUT ->
                        if (asset.status == AssetStatus.BORROWED) "Barang sedang dipinjam." else null
                    TransactionMode.CHECK_IN  ->
                        if (asset.status == AssetStatus.AVAILABLE) "Barang sudah di gudang." else null
                }
                _uiState.update {
                    it.copy(isLookingUp = false, resolvedAsset = asset, lookupError = modeError)
                }
            }
        }
    }

    // ── Form fields ───────────────────────────────────────────────────────────

    fun onRecipientChange(v: String)   { _uiState.update { it.copy(recipientName = v) } }
    fun onDestinationChange(v: String) { _uiState.update { it.copy(destination = v) } }
    fun onNotesChange(v: String)       { _uiState.update { it.copy(notes = v) } }

    // ── GPS ───────────────────────────────────────────────────────────────────

    fun fetchGps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGpsLoading = true) }
            val loc = locationHelper.getCurrentLocation()
            _uiState.update {
                it.copy(
                    isGpsLoading = false,
                    latitude = loc?.latitude,
                    longitude = loc?.longitude,
                    gpsAccuracy = loc?.accuracyMeters
                )
            }
        }
    }

    // ── Bukti Transaksi ───────────────────────────────────────────────────────

    fun onShowPhotoPicker()  { _uiState.update { it.copy(evidencePickerMode = EvidencePickerMode.PHOTO) } }
    fun onShowPdfPicker()    { _uiState.update { it.copy(evidencePickerMode = EvidencePickerMode.PDF) } }
    fun onDismissPicker()    { _uiState.update { it.copy(evidencePickerMode = EvidencePickerMode.NONE) } }
    fun onRemoveEvidence()   {
        _uiState.update {
            it.copy(
                evidenceUri = null,
                evidenceType = null,
                savedEvidencePath = null,
                evidencePickerMode = EvidencePickerMode.NONE
            )
        }
    }

    fun onPhotoSelected(uri: Uri) {
        _uiState.update {
            it.copy(
                evidenceUri = uri,
                evidenceType = "PHOTO",
                evidencePickerMode = EvidencePickerMode.NONE
            )
        }
    }

    fun onPdfSelected(uri: Uri) {
        _uiState.update {
            it.copy(
                evidenceUri = uri,
                evidenceType = "PDF",
                evidencePickerMode = EvidencePickerMode.NONE
            )
        }
    }

    // ── Simpan bukti ke MasterBarang sebelum submit ───────────────────────────

    private suspend fun saveEvidence(txId: String): String? {
        val state = _uiState.value
        val uri   = state.evidenceUri ?: return null
        val type  = state.evidenceType ?: return null

        return when (type) {
            "PHOTO" -> masterBarangManager.saveBuktiPhoto(uri, txId)
            "PDF"   -> masterBarangManager.saveBuktiPdf(uri, txId)
            else    -> null
        }
    }

    // ── Submit Transaksi ──────────────────────────────────────────────────────

    fun onConfirm() {
        val state = _uiState.value
        val asset = state.resolvedAsset ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }

            // Simpan bukti dulu ke MasterBarang jika ada
            val txId = java.util.UUID.randomUUID().toString()
            val evidencePath = if (state.evidenceUri != null) {
                _uiState.update { it.copy(isSavingEvidence = true) }
                val path = saveEvidence(txId)
                _uiState.update { it.copy(isSavingEvidence = false, savedEvidencePath = path) }
                path
            } else null

            val result = when (state.mode) {
                TransactionMode.CHECK_OUT -> repository.checkOut(
                    assetId           = asset.id,
                    recipientName     = state.recipientName.trim(),
                    destination       = state.destination.trim(),
                    notes             = state.notes.trim(),
                    latitude          = state.latitude,
                    longitude         = state.longitude,
                    gpsAccuracy       = state.gpsAccuracy,
                    evidenceFilePath  = evidencePath,
                    evidenceType      = state.evidenceType
                )
                TransactionMode.CHECK_IN -> repository.checkIn(
                    assetId     = asset.id,
                    notes       = state.notes.trim(),
                    latitude    = state.latitude,
                    longitude   = state.longitude,
                    gpsAccuracy = state.gpsAccuracy
                )
            }

            result.fold(
                onSuccess = { tx ->
                    _uiState.update {
                        TransactionUiState(
                            mode            = state.mode,
                            lastTransaction = tx,
                            latitude        = state.latitude,
                            longitude       = state.longitude,
                            gpsAccuracy     = state.gpsAccuracy
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSubmitting = false, submitError = e.message) }
                }
            )
        }
    }

    fun onDismissSuccess() { _uiState.update { it.copy(lastTransaction = null) } }
}