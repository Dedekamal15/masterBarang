package com.assettrack.domain.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.assettrack.data.local.SyncPreferences
import com.assettrack.data.local.dao.AssetDao
import com.assettrack.data.local.dao.TransactionDao
import com.assettrack.data.local.entity.AssetEntity
import com.assettrack.data.local.entity.TransactionEntity
import com.assettrack.data.remote.api.AssetTrackApiService
import com.assettrack.data.remote.dto.*
import com.assettrack.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

data class BulkImportResult(val successCount: Int, val errors: List<String>)

data class SyncResult(
    val assetsSynced: Int       = 0,
    val transactionsSynced: Int = 0,
    val evidenceUploaded: Int   = 0,
    val assetsPulled: Int       = 0,
    val transactionsPulled: Int = 0,
    val errors: List<String>    = emptyList()
)

@Singleton
class AssetRepository @Inject constructor(
    private val assetDao: AssetDao,
    private val transactionDao: TransactionDao,
    private val api: AssetTrackApiService,
    @ApplicationContext private val context: Context,
    private val syncPrefs: SyncPreferences
) {
    companion object { private const val TAG = "AssetRepository" }

    // ── Observe ───────────────────────────────────────────────────────────────

    fun observeAssets(): Flow<List<Asset>> =
        assetDao.observeAll().map { it.map(AssetEntity::toDomain) }

    fun searchAssets(q: String): Flow<List<Asset>> =
        assetDao.search(q).map { it.map(AssetEntity::toDomain) }

    fun observePendingAssetCount(): Flow<Int> =
        assetDao.observePendingCount()

    fun observeTransactions(): Flow<List<Transaction>> =
        transactionDao.observeAll().map { it.map(TransactionEntity::toDomain) }

    fun searchTransactions(q: String): Flow<List<Transaction>> =
        transactionDao.search(q).map { it.map(TransactionEntity::toDomain) }

    // ── Lookup ────────────────────────────────────────────────────────────────

    suspend fun getAssetBySerialNumber(sn: String): Asset? =
        assetDao.findBySerialNumber(sn)?.toDomain()

    suspend fun isDuplicateSerialNumber(sn: String, excludeId: String? = null): Boolean {
        val existing = assetDao.findBySerialNumber(sn) ?: return false
        return excludeId == null || existing.id != excludeId
    }

    // ── Register asset ────────────────────────────────────────────────────────

    suspend fun registerAsset(
        name: String, category: String, serialNumber: String,
        description: String, location: String
    ): Result<Asset> {
        if (isDuplicateSerialNumber(serialNumber))
            return Result.failure(Exception("SN/IMEI '$serialNumber' sudah terdaftar"))
        val now = System.currentTimeMillis()
        val entity = AssetEntity(
            id = UUID.randomUUID().toString(), name = name, category = category,
            serialNumber = serialNumber, description = description, location = location,
            status = AssetStatus.AVAILABLE, isSynced = false, createdAt = now, updatedAt = now
        )
        assetDao.insert(entity)
        return Result.success(entity.toDomain())
    }

    suspend fun bulkRegisterFromCsv(rows: List<Map<String, String>>): BulkImportResult {
        var success = 0
        val errors = mutableListOf<String>()
        val now = System.currentTimeMillis()

        for (row in rows) {
            // ✅ null check biasa — continue langsung di for loop, bukan di dalam lambda
            val sn = row["serial_number"] ?: row["imei"]
            if (sn == null) {
                errors.add("Row tanpa serial_number")
                continue
            }

            if (isDuplicateSerialNumber(sn)) {
                errors.add("Duplikat: $sn")
                continue
            }

            try {
                assetDao.insert(
                    AssetEntity(
                        id = UUID.randomUUID().toString(),
                        name = row["name"] ?: "Unknown",
                        category = row["category"] ?: "Uncategorized",
                        serialNumber = sn,
                        description = row["description"] ?: "",
                        location = row["location"] ?: "",
                        status = AssetStatus.AVAILABLE,
                        isSynced = false,
                        createdAt = now,
                        updatedAt = now
                    )
                )
                success++
            } catch (e: Exception) {
                errors.add("Error $sn: ${e.message}")
            }
        }

        return BulkImportResult(success, errors)
    }

    // ── Transactions ──────────────────────────────────────────────────────────

    suspend fun checkOut(
        assetId: String, recipientName: String, destination: String, notes: String,
        latitude: Double?, longitude: Double?, gpsAccuracy: Float?,
        evidenceFilePath: String? = null, evidenceType: String? = null
    ): Result<Transaction> {
        val asset = assetDao.getById(assetId)
            ?: return Result.failure(Exception("Aset tidak ditemukan"))
        if (asset.status == AssetStatus.BORROWED)
            return Result.failure(Exception("Aset sedang dipinjam"))
        val now = System.currentTimeMillis()
        val tx = TransactionEntity(
            id = UUID.randomUUID().toString(), assetId = assetId,
            assetName = asset.name, assetSerialNumber = asset.serialNumber,
            type = TransactionType.CHECK_OUT, recipientName = recipientName,
            destination = destination, notes = notes, timestampMs = now,
            latitude = latitude, longitude = longitude, gpsAccuracyMeters = gpsAccuracy,
            isSynced = false, evidenceFilePath = evidenceFilePath,
            evidenceType = evidenceType, isEvidenceUploaded = false
        )
        transactionDao.insert(tx)
        assetDao.updateStatus(assetId, AssetStatus.BORROWED.name, now)
        return Result.success(tx.toDomain())
    }

    suspend fun checkIn(
        assetId: String, notes: String,
        latitude: Double?, longitude: Double?, gpsAccuracy: Float?
    ): Result<Transaction> {
        val asset = assetDao.getById(assetId)
            ?: return Result.failure(Exception("Aset tidak ditemukan"))
        val now = System.currentTimeMillis()
        val tx = TransactionEntity(
            id = UUID.randomUUID().toString(), assetId = assetId,
            assetName = asset.name, assetSerialNumber = asset.serialNumber,
            type = TransactionType.CHECK_IN, recipientName = "", destination = "",
            notes = notes, timestampMs = now, latitude = latitude, longitude = longitude,
            gpsAccuracyMeters = gpsAccuracy, isSynced = false,
            evidenceFilePath = null, evidenceType = null, isEvidenceUploaded = true
        )
        transactionDao.insert(tx)
        assetDao.updateStatus(assetId, AssetStatus.AVAILABLE.name, now)
        return Result.success(tx.toDomain())
    }

    // ── Full Sync ─────────────────────────────────────────────────────────────

    suspend fun syncPendingData(forceFullSync: Boolean = false): SyncResult {
        var assetsSynced = 0; var txSynced = 0
        var evidenceUploaded = 0; var assetsPulled = 0; var txPulled = 0
        val errors = mutableListOf<String>()

        // 1. PUSH assets yang belum sync
        val pendingAssets = assetDao.getPending()
        if (pendingAssets.isNotEmpty()) {
            pendingAssets.chunked(50).forEach { chunk ->
                runCatching {
                    val resp = api.syncAssets(AssetBatchDto(chunk.map { it.toDto() }))
                    if (resp.isSuccessful) {
                        val body = resp.body()!!
                        val failSet = body.failedIds.toSet()
                        val okIds = chunk.map { it.id }.filterNot { it in failSet }
                        if (okIds.isNotEmpty()) assetDao.markSynced(okIds)
                        assetsSynced += okIds.size
                        errors.addAll(body.failedIds.map { "Asset push failed: $it" })
                    } else errors.add("Assets push HTTP ${resp.code()}")
                }.onFailure { errors.add("Assets push: ${it.message}") }
            }
        }

        // 2. PUSH transactions yang belum sync
        val pendingTx = transactionDao.getPending()
        if (pendingTx.isNotEmpty()) {
            pendingTx.chunked(50).forEach { chunk ->
                runCatching {
                    val resp = api.syncTransactions(TransactionBatchDto(chunk.map { it.toDto() }))
                    if (resp.isSuccessful) {
                        val body = resp.body()!!
                        val failSet = body.failedIds.toSet()
                        val okIds = chunk.map { it.id }.filterNot { it in failSet }
                        if (okIds.isNotEmpty()) transactionDao.markSynced(okIds)
                        txSynced += okIds.size
                        errors.addAll(body.failedIds.map { "TX push failed: $it" })
                    } else errors.add("TX push HTTP ${resp.code()}")
                }.onFailure { errors.add("TX push: ${it.message}") }
            }
        }

        // 3. UPLOAD evidence files yang belum diupload
        val pendingEvidence = transactionDao.getPendingEvidenceUpload()
        for (tx in pendingEvidence) {
            val filePath = tx.evidenceFilePath ?: continue
            val file = File(filePath)
            if (!file.exists()) {
                transactionDao.markEvidenceUploaded(tx.id)
                Log.w(TAG, "Evidence file missing, skipping: $filePath")
                continue
            }
            runCatching {
                val mimeType = if (tx.evidenceType == "PDF") "application/pdf" else "image/jpeg"
                var uploadFile = file
                if (tx.evidenceType != "PDF") {
                    uploadFile = compressImage(file)
                }

                val reqBody = uploadFile.asRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", uploadFile.name, reqBody)
                val resp = api.uploadEvidence(tx.id, part)
                if (resp.isSuccessful) {
                    transactionDao.markEvidenceUploaded(tx.id)
                    evidenceUploaded++
                    Log.i(TAG, "Evidence uploaded: ${tx.id}")
                } else errors.add("Evidence upload HTTP ${resp.code()} tx=${tx.id}")

                if (uploadFile != file && uploadFile.exists()) {
                    uploadFile.delete() // cleanup compressed file
                }
            }.onFailure { errors.add("Evidence upload: ${it.message}") }
        }

        // 4. PULL semua data terbaru dari server
        runCatching {
            val lastSyncMs = if (forceFullSync) 0L else syncPrefs.getLastSync()
            val resp = api.masterSync(sinceMs = lastSyncMs)
            if (resp.isSuccessful) {
                val body = resp.body()!!
                // Update/insert assets dari server
                for (dto in body.assets) {
                    val local = assetDao.getById(dto.id)
                    if (local == null || dto.updatedAt > local.updatedAt) {
                        assetDao.insert(dto.toEntity())
                        assetsPulled++
                    }
                }
                // Insert transactions dari server yang belum ada lokal
                for (dto in body.transactions) {
                    if (transactionDao.getById(dto.id) == null) {
                        transactionDao.insert(dto.toEntity())
                        txPulled++
                    }
                }
                syncPrefs.saveLastSync(body.serverTimestampMs)
                Log.i(TAG, "Pull: $assetsPulled assets, $txPulled tx from server")
            } else errors.add("Master sync pull HTTP ${resp.code()}")
        }.onFailure { errors.add("Master sync pull: ${it.message}") }

        return SyncResult(assetsSynced, txSynced, evidenceUploaded, assetsPulled, txPulled, errors)
    }

    private fun compressImage(originalFile: File): File {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(originalFile.absolutePath, options)
            
            options.inSampleSize = calculateInSampleSize(options, 1280, 1280)
            options.inJustDecodeBounds = false
            
            val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath, options)
                ?: return originalFile
            val outputFile = File(context.cacheDir, "compressed_${originalFile.name}")
            
            FileOutputStream(outputFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            bitmap.recycle()
            outputFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to compress image, fallback to original: ${e.message}", e)
            originalFile
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}

// ── Mappers ───────────────────────────────────────────────────────────────────

private fun AssetEntity.toDomain() = Asset(
    id, name, category, serialNumber, description, location, status, isSynced, createdAt, updatedAt
)

private fun AssetEntity.toDto() = AssetDto(
    id, name, category, serialNumber, description, location, status.name, createdAt, updatedAt
)

private fun AssetDto.toEntity() = AssetEntity(
    id = id, name = name, category = category, serialNumber = serialNumber,
    description = description, location = location,
    status = runCatching { AssetStatus.valueOf(status) }.getOrDefault(AssetStatus.AVAILABLE),
    isSynced = true, createdAt = createdAt, updatedAt = updatedAt
)

private fun TransactionEntity.toDomain() = Transaction(
    id, assetId, assetName, assetSerialNumber, type,
    recipientName, destination, notes, timestampMs,
    latitude, longitude, gpsAccuracyMeters, isSynced,
    evidenceFilePath, evidenceType
)

private fun TransactionEntity.toDto() = TransactionDto(
    id = id, assetId = assetId, assetName = assetName,
    assetSerialNumber = assetSerialNumber, type = type.name,
    recipientName = recipientName, destination = destination, notes = notes,
    timestampMs = timestampMs, latitude = latitude, longitude = longitude,
    gpsAccuracyMeters = gpsAccuracyMeters,
    evidenceFilename = evidenceFilePath?.substringAfterLast("/"),
    evidenceType = evidenceType
)

private fun TransactionDto.toEntity() = TransactionEntity(
    id = id, assetId = assetId, assetName = assetName,
    assetSerialNumber = assetSerialNumber,
    type = runCatching { TransactionType.valueOf(type) }.getOrDefault(TransactionType.CHECK_OUT),
    recipientName = recipientName, destination = destination, notes = notes,
    timestampMs = timestampMs, latitude = latitude, longitude = longitude,
    gpsAccuracyMeters = gpsAccuracyMeters, isSynced = true,
    evidenceFilePath = null, evidenceType = evidenceType, isEvidenceUploaded = true
)

