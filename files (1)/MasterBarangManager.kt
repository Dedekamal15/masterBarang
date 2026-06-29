package com.assettrack.data

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MasterBarangManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageCompressor: ImageCompressor   // ← inject compressor
) {
    companion object {
        private const val TAG       = "MasterBarangManager"
        private const val ROOT_DIR  = "MasterBarang"
        private const val BUKTI_DIR = "bukti"
    }

    /** Root folder: Downloads/MasterBarang/ */
    private fun getRootDir(): File {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            ROOT_DIR
        )
        dir.mkdirs()
        return dir
    }

    /** Folder bukti: Downloads/MasterBarang/bukti/ */
    private fun getBuktiDir(): File {
        val dir = File(getRootDir(), BUKTI_DIR)
        dir.mkdirs()
        return dir
    }

    /**
     * Simpan foto dari URI kamera/galeri ke MasterBarang/bukti/.
     * ✅ Foto dikompres otomatis sebelum disimpan (resize + JPEG 75%).
     *
     * @return path file yang tersimpan, atau null jika gagal
     */
    suspend fun saveBuktiPhoto(sourceUri: Uri, transactionId: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val fileName = "${transactionId}.jpg"
                val destFile = File(getBuktiDir(), fileName)

                // ✅ Kompres foto sebelum simpan
                val result = imageCompressor.compress(sourceUri, destFile)
                if (result != null) {
                    Log.i(TAG, "Foto tersimpan: ${result.absolutePath} (${result.length() / 1024}KB)")
                    result.absolutePath
                } else {
                    // Fallback: copy tanpa kompres jika kompres gagal
                    Log.w(TAG, "Kompres gagal, copy langsung")
                    copyFromUri(sourceUri, destFile)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gagal simpan foto: ${e.message}", e)
                null
            }
        }

    /**
     * Simpan PDF dari URI ke MasterBarang/bukti/.
     * PDF tidak dikompres karena sudah compressed.
     *
     * @return path file yang tersimpan, atau null jika gagal
     */
    suspend fun saveBuktiPdf(sourceUri: Uri, transactionId: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val fileName = "${transactionId}.pdf"
                val destFile = File(getBuktiDir(), fileName)
                copyFromUri(sourceUri, destFile)
            } catch (e: Exception) {
                Log.e(TAG, "Gagal simpan PDF: ${e.message}", e)
                null
            }
        }

    /**
     * Hapus file bukti lama yang lebih dari [olderThanDays] hari.
     * Dipanggil oleh SyncWorker setelah sync berhasil.
     */
    suspend fun cleanOldFiles(olderThanDays: Int = 90) = withContext(Dispatchers.IO) {
        try {
            val cutoff = System.currentTimeMillis() - (olderThanDays.toLong() * 24 * 60 * 60 * 1000)
            val deleted = getBuktiDir().listFiles()
                ?.filter { it.lastModified() < cutoff }
                ?.count { it.delete() } ?: 0
            if (deleted > 0) Log.i(TAG, "Hapus $deleted file bukti lama (>$olderThanDays hari)")
        } catch (e: Exception) {
            Log.e(TAG, "cleanOldFiles error: ${e.message}")
        }
    }

    /** Cek apakah file bukti ada di lokal */
    fun fileExists(filePath: String): Boolean = File(filePath).exists()

    /** Copy file dari content URI ke destination file */
    private fun copyFromUri(uri: Uri, dest: File): String? {
        dest.parentFile?.mkdirs()
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(dest).use { output ->
                input.copyTo(output)
            }
        }
        return if (dest.exists()) dest.absolutePath else null
    }
}
