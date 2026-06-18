package com.assettrack.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mengelola folder MasterBarang di direktori publik Downloads HP.
 *
 * Struktur folder:
 * /storage/emulated/0/Downloads/MasterBarang/
 * ├── assets/          ← export CSV daftar aset
 * ├── bukti/           ← foto & PDF bukti transaksi
 * │   ├── TRX-20240101-120000-[assetSN].jpg
 * │   └── TRX-20240101-120000-[assetSN].pdf
 * └── export/          ← export laporan
 */
@Singleton
class MasterBarangManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Root folder MasterBarang di Downloads (accessible tanpa izin khusus di Android 10+)
    val rootDir: File by lazy {
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "MasterBarang"
        ).also { it.mkdirs() }
    }

    val buktiDir: File by lazy { File(rootDir, "bukti").also { it.mkdirs() } }
    val assetsDir: File by lazy { File(rootDir, "assets").also { it.mkdirs() } }
    val exportDir: File by lazy { File(rootDir, "export").also { it.mkdirs() } }

    // ── Simpan foto bukti transaksi ───────────────────────────────────────────

    /**
     * Simpan foto dari URI kamera ke MasterBarang/bukti/
     * Return: path absolut file yang disimpan, atau null jika gagal
     */
    suspend fun savePhotoEvidence(
        sourceUri: Uri,
        assetSerialNumber: String,
        transactionId: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
            val fileName = "TRX-${timestamp}-${assetSerialNumber.take(12)}.jpg"
            val destFile = File(buktiDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                // Compress bitmap untuk hemat storage
                val bitmap = BitmapFactory.decodeStream(input)
                FileOutputStream(destFile).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
                }
            }

            if (destFile.exists()) destFile.absolutePath else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Simpan PDF bukti dari URI file picker ke MasterBarang/bukti/
     */
    suspend fun savePdfEvidence(
        sourceUri: Uri,
        assetSerialNumber: String,
        transactionId: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
            val fileName = "TRX-${timestamp}-${assetSerialNumber.take(12)}.pdf"
            val destFile = File(buktiDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (destFile.exists()) destFile.absolutePath else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Export daftar aset ke CSV di MasterBarang/assets/
     */
    suspend fun exportAssetsCsv(csvContent: String): String? = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
            val file = File(assetsDir, "DaftarAset-$timestamp.csv")
            file.writeText(csvContent)
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /**
     * List semua file bukti yang ada
     */
    fun listEvidenceFiles(): List<File> =
        buktiDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()

    /**
     * Hapus file bukti lama (lebih dari 90 hari) untuk hemat storage
     */
    suspend fun cleanOldFiles(olderThanDays: Int = 90) = withContext(Dispatchers.IO) {
        val threshold = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
        buktiDir.listFiles()
            ?.filter { it.lastModified() < threshold }
            ?.forEach { it.delete() }
    }
}
