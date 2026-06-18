package com.assettrack.presentation.screens.registration

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assettrack.data.CsvParser
import com.assettrack.domain.repository.AssetRepository
import com.assettrack.domain.repository.BulkImportResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegistrationUiState(
    val name: String = "",
    val category: String = "",
    val serialNumber: String = "",
    val description: String = "",
    val location: String = "",
    // Validation
    val snError: String? = null,
    val nameError: String? = null,
    // UI states
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val submitError: String? = null,
    // Scan
    val showBarcodeScanner: Boolean = false,
    val showOcrScanner: Boolean = false,
    // Bulk
    val isBulkImporting: Boolean = false,
    val bulkResult: BulkImportResult? = null
)

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val repository: AssetRepository,
    private val csvParser: CsvParser
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    // ── Field updates ─────────────────────────────────────────────────────────

    fun onNameChange(v: String)        { _uiState.update { it.copy(name = v, nameError = null) } }
    fun onCategoryChange(v: String)    { _uiState.update { it.copy(category = v) } }
    fun onDescriptionChange(v: String) { _uiState.update { it.copy(description = v) } }
    fun onLocationChange(v: String)    { _uiState.update { it.copy(location = v) } }

    fun onSerialNumberChange(v: String) {
        _uiState.update { it.copy(serialNumber = v, snError = null) }
        if (v.length >= 5) validateSn(v)
    }

    private fun validateSn(sn: String) {
        viewModelScope.launch {
            val isDuplicate = repository.isDuplicateSerialNumber(sn)
            if (isDuplicate) {
                _uiState.update { it.copy(snError = "Duplicate SN/IMEI detected in database.") }
            }
        }
    }

    // ── Scanner callbacks ─────────────────────────────────────────────────────

    fun onShowBarcodeScanner() { _uiState.update { it.copy(showBarcodeScanner = true) } }
    fun onShowOcrScanner()     { _uiState.update { it.copy(showOcrScanner = true) } }
    fun onDismissScanner()     { _uiState.update { it.copy(showBarcodeScanner = false, showOcrScanner = false) } }

    fun onScanResult(value: String) {
        _uiState.update {
            it.copy(
                serialNumber = value,
                showBarcodeScanner = false,
                showOcrScanner = false,
                snError = null
            )
        }
        validateSn(value)
    }

    // ── Submit ────────────────────────────────────────────────────────────────

    fun onSubmit() {
        val state = _uiState.value
        // Validate
        var hasError = false
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Nama barang wajib diisi") }
            hasError = true
        }
        if (state.serialNumber.isBlank()) {
            _uiState.update { it.copy(snError = "SN / IMEI wajib diisi") }
            hasError = true
        }
        if (state.snError != null) hasError = true
        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }
            val result = repository.registerAsset(
                name         = state.name.trim(),
                category     = state.category.trim(),
                serialNumber = state.serialNumber.trim(),
                description  = state.description.trim(),
                location     = state.location.trim()
            )
            result.fold(
                onSuccess = {
                    _uiState.update {
                        RegistrationUiState(submitSuccess = true) // reset form
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSubmitting = false, submitError = e.message) }
                }
            )
        }
    }

    fun onDismissSuccess() { _uiState.update { it.copy(submitSuccess = false) } }
    fun onDismissError()   { _uiState.update { it.copy(submitError = null) } }

    // ── Bulk Import ───────────────────────────────────────────────────────────

    fun onCsvSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBulkImporting = true, bulkResult = null) }
            try {
                val rows   = csvParser.parse(uri)
                val result = repository.bulkRegisterFromCsv(rows)
                _uiState.update { it.copy(isBulkImporting = false, bulkResult = result) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isBulkImporting = false,
                        submitError = "CSV parse error: ${e.message}"
                    )
                }
            }
        }
    }

    fun onDismissBulkResult() { _uiState.update { it.copy(bulkResult = null) } }
}
