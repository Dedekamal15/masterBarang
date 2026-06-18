package com.assettrack.presentation.screens.registration

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.assettrack.presentation.components.CameraScanner
import com.assettrack.presentation.components.ScanMode
import com.assettrack.presentation.theme.*

@Composable
fun RegistrationScreen(viewModel: RegistrationViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Full-screen scanner overlays
    if (uiState.showBarcodeScanner) {
        CameraScanner(
            mode = ScanMode.BARCODE,
            onResult = viewModel::onScanResult,
            onDismiss = viewModel::onDismissScanner
        )
        return
    }
    if (uiState.showOcrScanner) {
        CameraScanner(
            mode = ScanMode.OCR,
            onResult = viewModel::onScanResult,
            onDismiss = viewModel::onDismissScanner
        )
        return
    }

    // CSV file picker
    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.onCsvSelected(it) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column {
            Text(
                "Daftar Barang Baru",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Daftarkan barang baru secara manual atau gunakan scanner.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Quick scanner buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScannerButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.QrCodeScanner,
                label = "Scan Barcode",
                onClick = viewModel::onShowBarcodeScanner
            )
            ScannerButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.DocumentScanner,
                label = "OCR SN/IMEI",
                onClick = viewModel::onShowOcrScanner
            )
        }

        // Asset details form
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            border = BorderStroke(1.dp, OutlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Detail Barang",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Name
                FormField(
                    label = "Nama Barang *",
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    placeholder = "cth. Laptop Dell XPS 15",
                    isError = uiState.nameError != null,
                    errorText = uiState.nameError
                )

                // Category
                CategoryDropdown(
                    selected = uiState.category,
                    onSelected = viewModel::onCategoryChange
                )

                // SN / IMEI
                FormField(
                    label = "SN / IMEI *",
                    value = uiState.serialNumber,
                    onValueChange = viewModel::onSerialNumberChange,
                    placeholder = "e.g. MTK-8829-1A",
                    isError = uiState.snError != null,
                    errorText = uiState.snError,
                    trailingIcon = if (uiState.snError != null) {
                        { Icon(Icons.Filled.Error, null, tint = MaterialTheme.colorScheme.error) }
                    } else null
                )

                // Location
                FormField(
                    label = "Lokasi Penyimpanan",
                    value = uiState.location,
                    onValueChange = viewModel::onLocationChange,
                    placeholder = "e.g. Rak A-12"
                )

                // Description
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Deskripsi") },
                    placeholder = { Text("Spesifikasi atau catatan tambahan...") },
                    minLines = 3,
                    shape = RoundedCornerShape(8.dp),
                    colors = fieldColors()
                )

                // Submit button
                Button(
                    onClick = viewModel::onSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !uiState.isSubmitting,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = OnPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.Save, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Daftarkan Barang", fontWeight = FontWeight.SemiBold)
                    }
                }

                // Submit error
                uiState.submitError?.let { err ->
                    Text(
                        err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Submit success snackbar
                if (uiState.submitSuccess) {
                    LaunchedEffect(Unit) { viewModel.onDismissSuccess() }
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = StatusActiveBackground)
                    ) {
                        Text(
                            "✓ Barang berhasil didaftarkan!",
                            modifier = Modifier.padding(12.dp),
                            color = StatusActiveForeground,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Bulk import card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            border = BorderStroke(1.dp, OutlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.UploadFile, null, tint = Secondary)
                    Text(
                        "Import Massal CSV",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    "Upload file CSV untuk mendaftarkan banyak barang sekaligus.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = { csvLauncher.launch("text/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !uiState.isBulkImporting,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, OutlineVariant)
                ) {
                    if (uiState.isBulkImporting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Mengimpor...")
                    } else {
                        Icon(Icons.Filled.FolderOpen, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Pilih File CSV", fontWeight = FontWeight.SemiBold)
                    }
                }

                // Bulk result
                uiState.bulkResult?.let { result ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (result.errors.isEmpty())
                                StatusActiveBackground else StatusBorrowedBackground
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "✓ ${result.successCount} barang berhasil didaftarkan",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = StatusActiveForeground
                            )
                            if (result.errors.isNotEmpty()) {
                                Text(
                                    "${result.errors.size} error: ${result.errors.take(3).joinToString("; ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusMaintenanceForeground
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

// ── Sub-components ────────────────────────────────────────────────────────────

@Composable
private fun ScannerButton(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, OutlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceContainerLowest)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, label, tint = Primary, modifier = Modifier.size(32.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isError: Boolean = false,
    errorText: String? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.outline) },
            isError = isError,
            singleLine = true,
            trailingIcon = trailingIcon,
            shape = RoundedCornerShape(8.dp),
            colors = fieldColors()
        )
        if (isError && errorText != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Filled.ErrorOutline,
                    null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private val categories = listOf(
    "Perangkat Komputer",
    "Perangkat Jaringan",
    "Tools",
    "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.ifBlank { "Pilih kategori..." },
            onValueChange = {},
            readOnly = true,
            label = { Text("Kategori") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(8.dp),
            colors = fieldColors()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat) },
                    onClick = { onSelected(cat); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Primary,
    unfocusedBorderColor = OutlineVariant,
    focusedContainerColor = SurfaceContainerLowest,
    unfocusedContainerColor = SurfaceContainerLowest,
    errorBorderColor = MaterialTheme.colorScheme.error
)
