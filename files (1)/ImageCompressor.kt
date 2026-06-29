package com.assettrack.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG             = "ImageCompressor"
        private const val MAX_WIDTH       = 1280   // px
        private const val MAX_HEIGHT      = 1280   // px
        private const val QUALITY         = 75     // JPEG quality 0-100
        private const val MAX_SIZE_BYTES  = 500_000 // 500KB target
    }

    /**
     * Kompres foto dari URI sumber ke file tujuan.
     * - Resize jika dimensi melebihi MAX_WIDTH/MAX_HEIGHT
     * - Pertahankan aspect ratio
     * - Koreksi orientasi dari EXIF (foto portrait tidak miring)
     * - JPEG quality 75 (tidak terlihat bedanya secara visual)
     *
     * @return File hasil kompres, atau null jika gagal
     */
    suspend fun compress(sourceUri: Uri, destinationFile: File): File? =
        withContext(Dispatchers.IO) {
            try {
                // Baca EXIF dulu untuk orientasi
                val inputStream = context.contentResolver.openInputStream(sourceUri)
                    ?: return@withContext null
                val exif        = ExifInterface(inputStream)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                inputStream.close()

                // Baca ukuran asli tanpa load ke memory
                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(sourceUri)?.use {
                    BitmapFactory.decodeStream(it, null, opts)
                }

                // Hitung inSampleSize agar tidak OOM saat load
                val sampleSize = calculateSampleSize(opts.outWidth, opts.outHeight)

                // Load bitmap dengan sample size
                val loadOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                var bitmap = context.contentResolver.openInputStream(sourceUri)?.use {
                    BitmapFactory.decodeStream(it, null, loadOpts)
                } ?: return@withContext null

                // Koreksi orientasi EXIF
                bitmap = correctOrientation(bitmap, orientation)

                // Resize jika masih terlalu besar
                bitmap = resizeIfNeeded(bitmap)

                // Simpan ke file tujuan dengan kompres JPEG
                destinationFile.parentFile?.mkdirs()
                var quality = QUALITY
                do {
                    FileOutputStream(destinationFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                    }
                    quality -= 10
                } while (destinationFile.length() > MAX_SIZE_BYTES && quality > 30)

                bitmap.recycle()

                val originalSize = getFileSize(sourceUri)
                Log.i(TAG, "Kompres: ${originalSize}KB → ${destinationFile.length() / 1024}KB (quality=$quality)")

                destinationFile
            } catch (e: Exception) {
                Log.e(TAG, "Kompres gagal: ${e.message}", e)
                null
            }
        }

    private fun calculateSampleSize(width: Int, height: Int): Int {
        var sampleSize = 1
        var w = width; var h = height
        while (w > MAX_WIDTH * 2 || h > MAX_HEIGHT * 2) {
            sampleSize *= 2; w /= 2; h /= 2
        }
        return sampleSize
    }

    private fun resizeIfNeeded(bitmap: Bitmap): Bitmap {
        val w = bitmap.width; val h = bitmap.height
        if (w <= MAX_WIDTH && h <= MAX_HEIGHT) return bitmap

        val ratio  = minOf(MAX_WIDTH.toFloat() / w, MAX_HEIGHT.toFloat() / h)
        val newW   = (w * ratio).toInt()
        val newH   = (h * ratio).toInt()
        val scaled = Bitmap.createScaledBitmap(bitmap, newW, newH, true)
        if (scaled != bitmap) bitmap.recycle()
        return scaled
    }

    private fun correctOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90  -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL   -> matrix.preScale(1f, -1f)
            else -> return bitmap
        }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) bitmap.recycle()
        return rotated
    }

    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use {
                it.statSize / 1024
            } ?: 0L
        } catch (e: Exception) { 0L }
    }
}
