package com.assettrack.presentation.screens.transaction

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.assettrack.presentation.components.AssetStatusBadge
import com.assettrack.presentation.components.CameraScanner
import com.assettrack.presentation.components.ScanMode
import com.assettrack.presentation.theme.*

@Composable
fun TransactionScreen(viewModel: TransactionViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // ── Launchers ─────────────────────────────────────────────────────────────

    // Ambil foto dari kamera (full resolution via TakePicture)
    val cameraPhotoUri = remember {
        val file = java.io.File(context.cacheDir, "evidence_capture.jpg")
        androidx.core.content.FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) viewModel.onPhotoSelected(cameraPhotoUri)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.onPhotoSelected(it) } }

    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.onPdfSelected(it) } }

    // ── Scanner overlay ───────────────────────────────────────────────────────
    if (uiState.showScanner) {
        CameraScanner(
            mode = ScanMode.BARCODE,
            onResult = viewModel::onScanResult,
            onDismiss = viewModel::onDismissScanner
        )
        return
    }

    // ── Bukti picker dialog ───────────────────────────────────────────────────
    if (uiState.evidencePickerMode == EvidencePickerMode.PHOTO) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissPicker,
            title = { Text("Tambah Bukti Foto", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.onDismissPicker(); cameraLauncher.launch(cameraPhotoUri) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.PhotoCamera, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Ambil Foto Kamera")
                    }
                    OutlinedButton(
                        onClick = { viewModel.onDismissPicker(); galleryLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Photo, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Pilih dari Galeri")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = viewModel::onDismissPicker) { Text("Batal") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (uiState.evidencePickerMode == EvidencePickerMode.PDF) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissPicker,
            title = { Text("Upload Bukti PDF", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedButton(
                    onClick = { viewModel.onDismissPicker(); pdfLauncher.launch("application/pdf") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.PictureAsPdf, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Pilih File PDF")
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = viewModel::onDismissPicker) { Text("Batal") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ── Success dialog ────────────────────────────────────────────────────────
    uiState.lastTransaction?.let { tx ->
        AlertDialog(
            onDismissRequest = viewModel::onDismissSuccess,
            icon = {
                Icon(
                    Icons.Filled.CheckCircle, null,
                    tint = Color(0xFF137333),
                    modifier = Modifier.size(44.dp)
                )
            },
            title = { Text("Transaksi Berhasil", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        if (uiState.mode == TransactionMode.CHECK_OUT)
                            "Aset dipinjamkan ke ${tx.recipientName}"
                        else "Barang berhasil dikembalikan"
                    )
                    if (tx.evidenceFilePath != null) {
                        Text(
                            "📎 Bukti tersimpan di MasterBarang/bukti/",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::onDismissSuccess,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) { Text("OK") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ── Main content ──────────────────────────────────────────────────────────
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
                "Transaksi Barang",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Rekam barang keluar atau masuk.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Mode toggle
        ModeToggle(mode = uiState.mode, onModeChange = viewModel::onModeChange)

        // Scanner + manual entry
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(2f).height(160.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, OutlineVariant),
                onClick = viewModel::onShowScanner
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.QrCodeScanner, null, Modifier.size(48.dp), tint = Primary)
                        Text("Scan Barcode Barang", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("Tap untuk buka kamera", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Card(
                modifier = Modifier.weight(1f).height(160.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = BorderStroke(1.dp, OutlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp).fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Manual", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = uiState.manualInput,
                            onValueChange = viewModel::onManualInputChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Asset ID...") },
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = OutlineVariant,
                                focusedContainerColor = SurfaceContainerLowest,
                                unfocusedContainerColor = SurfaceContainerLowest
                            )
                        )
                    }
                    OutlinedButton(
                        onClick = viewModel::onVerifyId,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        enabled = uiState.manualInput.isNotBlank() && !uiState.isLookingUp,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        if (uiState.isLookingUp) {
                            CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Search, null, Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Cari")
                        }
                    }
                }
            }
        }

        // Lookup error
        uiState.lookupError?.let { err ->
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Text(err, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }

        // Resolved asset card
        uiState.resolvedAsset?.let { asset ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SecondaryContainer.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, Secondary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(asset.name, fontWeight = FontWeight.SemiBold)
                        Text("SN: ${asset.serialNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(asset.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    AssetStatusBadge(status = asset.status)
                }
            }
        }

        // Transaction detail form
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
                Text("Detail Transaksi", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                if (uiState.mode == TransactionMode.CHECK_OUT) {
                    OutlinedTextField(
                        value = uiState.recipientName,
                        onValueChange = viewModel::onRecipientChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nama Penerima *") },
                        leadingIcon = { Icon(Icons.Filled.Person, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = fieldColors()
                    )
                    OutlinedTextField(
                        value = uiState.destination,
                        onValueChange = viewModel::onDestinationChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Lokasi Tujuan / Afdeling") },
                        placeholder = { Text("cth. Afdeling 1, Kantor Pusat, Gudang B...") },
                        leadingIcon = { Icon(Icons.Filled.LocationCity, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = fieldColors()
                    )
                }

                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Catatan (Opsional)") },
                    placeholder = { Text("Kondisi atau instruksi khusus...") },
                    minLines = 2,
                    shape = RoundedCornerShape(8.dp),
                    colors = fieldColors()
                )
            }
        }

        // ── Bukti Transaksi (hanya untuk CHECK_OUT) ───────────────────────────
        if (uiState.mode == TransactionMode.CHECK_OUT) {
            EvidenceCard(
                uiState = uiState,
                onAddPhoto = viewModel::onShowPhotoPicker,
                onAddPdf = viewModel::onShowPdfPicker,
                onRemove = viewModel::onRemoveEvidence
            )
        }

        // GPS widget
        GpsCard(
            lat = uiState.latitude,
            lng = uiState.longitude,
            accuracy = uiState.gpsAccuracy,
            isLoading = uiState.isGpsLoading,
            onRefresh = viewModel::fetchGps
        )

        // Confirm button
        Button(
            onClick = viewModel::onConfirm,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = uiState.isFormValid && !uiState.isSubmitting && !uiState.isSavingEvidence,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            when {
                uiState.isSavingEvidence -> {
                    CircularProgressIndicator(Modifier.size(20.dp), color = OnPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Menyimpan bukti...")
                }
                uiState.isSubmitting -> {
                    CircularProgressIndicator(Modifier.size(20.dp), color = OnPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Memproses...")
                }
                else -> {
                    Icon(Icons.Filled.CheckCircle, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Konfirmasi Transaksi", fontWeight = FontWeight.Bold)
                }
            }
        }

        uiState.submitError?.let { err ->
            Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(80.dp))
    }
}

// ── Bukti Card ────────────────────────────────────────────────────────────────

@Composable
private fun EvidenceCard(
    uiState: TransactionUiState,
    onAddPhoto: () -> Unit,
    onAddPdf: () -> Unit,
    onRemove: () -> Unit
) {
    val context = LocalContext.current

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
                Icon(Icons.Filled.AttachFile, null, tint = Primary, modifier = Modifier.size(20.dp))
                Text(
                    "Bukti Transaksi",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "(Opsional)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!uiState.hasEvidence) {
                // Belum ada bukti — tampilkan tombol pilih
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onAddPhoto,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, OutlineVariant)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(Icons.Filled.PhotoCamera, null, tint = Primary, modifier = Modifier.size(24.dp))
                            Text("Foto", style = MaterialTheme.typography.labelMedium, color = Primary)
                        }
                    }
                    OutlinedButton(
                        onClick = onAddPdf,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, OutlineVariant)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(Icons.Filled.PictureAsPdf, null, tint = Primary, modifier = Modifier.size(24.dp))
                            Text("PDF", style = MaterialTheme.typography.labelMedium, color = Primary)
                        }
                    }
                }
            } else {
                // Ada bukti — tampilkan preview
                when (uiState.evidenceType) {
                    "PHOTO" -> {
                        val imgUri = uiState.evidenceUri ?: uiState.savedEvidencePath?.let { Uri.parse("file://$it") }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imgUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Bukti foto",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Remove button overlay
                            IconButton(
                                onClick = onRemove,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Filled.Close, "Hapus bukti", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    "PDF" -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                                .border(1.dp, OutlineVariant, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Filled.PictureAsPdf, null, tint = Color(0xFFBA1A1A), modifier = Modifier.size(32.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    uiState.savedEvidencePath?.substringAfterLast("/")
                                        ?: "File PDF terpilih",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Akan disimpan ke MasterBarang/bukti/",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.Close, "Hapus", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                // Info storage path
                if (uiState.savedEvidencePath != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.FolderOpen, null, tint = Color(0xFF137333), modifier = Modifier.size(14.dp))
                        Text(
                            "Tersimpan di MasterBarang/bukti/",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF137333)
                        )
                    }
                }
            }

            Text(
                "File disimpan ke: Downloads/MasterBarang/bukti/",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Sub-components ────────────────────────────────────────────────────────────

@Composable
private fun ModeToggle(mode: TransactionMode, onModeChange: (TransactionMode) -> Unit) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
        border = BorderStroke(1.dp, OutlineVariant)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            listOf(
                TransactionMode.CHECK_OUT to "Barang Keluar",
                TransactionMode.CHECK_IN  to "Barang Kembali"
            ).forEach { (m, label) ->
                val selected = mode == m
                Button(
                    onClick = { onModeChange(m) },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) SurfaceContainerLowest else Color.Transparent,
                        contentColor = if (selected) Primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (selected) 1.dp else 0.dp
                    )
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun GpsCard(
    lat: Double?, lng: Double?, accuracy: Float?,
    isLoading: Boolean, onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
        border = BorderStroke(1.dp, OutlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(SurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Primary)
                    } else {
                        Icon(Icons.Filled.MyLocation, null, tint = Primary, modifier = Modifier.size(20.dp))
                    }
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            if (lat != null) "GPS Terkunci" else "GPS Tidak Tersedia",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (lat != null) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                        }
                    }
                    if (lat != null && lng != null) {
                        Text(
                            "%.5f, %.5f".format(lat, lng),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        accuracy?.let {
                            Text("±${it.toInt()}m", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            IconButton(onClick = onRefresh, enabled = !isLoading) {
                Icon(Icons.Filled.Refresh, "Refresh GPS", tint = Primary)
            }
        }
    }
}



@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Primary,
    unfocusedBorderColor = OutlineVariant,
    focusedContainerColor = SurfaceContainerLowest,
    unfocusedContainerColor = SurfaceContainerLowest
)
