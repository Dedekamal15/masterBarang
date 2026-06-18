package com.assettrack.presentation.components

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.assettrack.presentation.theme.Primary
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

enum class ScanMode { BARCODE, OCR }

@Composable
fun CameraScanner(
    mode: ScanMode,
    onResult: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Executor khusus untuk ImageAnalysis — BUKAN untuk listener CameraProvider
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    var hasResult by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { analysisExecutor.shutdown() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // ── Camera Preview ────────────────────────────────────────────────────
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    // PENTING: implementationMode COMPATIBLE lebih stabil di banyak device
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                // KRITIS: listener harus di Main Thread (ContextCompat.getMainExecutor)
                // bukan di background executor
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder()
                            .build()
                            .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(analysisExecutor) { proxy ->
                                    if (hasResult) {
                                        proxy.close()
                                        return@setAnalyzer
                                    }
                                    when (mode) {
                                        ScanMode.BARCODE -> analyzeBarcode(proxy) { value ->
                                            hasResult = true
                                            onResult(value)
                                        }
                                        ScanMode.OCR -> analyzeOcr(proxy) { value ->
                                            hasResult = true
                                            onResult(value)
                                        }
                                    }
                                }
                            }

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("CameraScanner", "Camera bind failed: ${e.message}", e)
                    }
                }, ContextCompat.getMainExecutor(ctx)) // ← Main thread, BUKAN background

                previewView
            }
        )

        // ── Viewfinder frame ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(280.dp, 160.dp)
        ) {
            // Garis scan
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                color = Primary.copy(alpha = 0.9f),
                thickness = 2.dp
            )
        }

        // ── Hint text ─────────────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp, start = 32.dp, end = 32.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.65f)
            )
        ) {
            Text(
                text = if (mode == ScanMode.BARCODE)
                    "Arahkan ke barcode atau QR code"
                else
                    "Arahkan ke teks IMEI atau Serial Number",
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }

        // ── Close button ──────────────────────────────────────────────────────
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Tutup scanner",
                tint = Color.White
            )
        }
    }
}

// ── Image analyzers ───────────────────────────────────────────────────────────

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun analyzeBarcode(proxy: ImageProxy, onFound: (String) -> Unit) {
    val mediaImage = proxy.image ?: run { proxy.close(); return }
    val image = InputImage.fromMediaImage(mediaImage, proxy.imageInfo.rotationDegrees)
    BarcodeScanning.getClient()
        .process(image)
        .addOnSuccessListener { barcodes ->
            barcodes.firstOrNull { it.rawValue != null }?.rawValue?.let { onFound(it) }
        }
        .addOnCompleteListener { proxy.close() }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun analyzeOcr(proxy: ImageProxy, onFound: (String) -> Unit) {
    val mediaImage = proxy.image ?: run { proxy.close(); return }
    val image = InputImage.fromMediaImage(mediaImage, proxy.imageInfo.rotationDegrees)
    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        .process(image)
        .addOnSuccessListener { result ->
            val text = result.text
            val found = Regex("\\b\\d{15}\\b").find(text)?.value
                ?: Regex("\\b[A-Z0-9]{8,20}\\b").findAll(text).map { it.value }.firstOrNull()
            found?.let { onFound(it) }
        }
        .addOnCompleteListener { proxy.close() }
}
