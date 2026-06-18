package com.assettrack.data.remote.dto

import com.google.gson.annotations.SerializedName

// ── Asset DTOs ────────────────────────────────────────────────────────────────

data class AssetDto(
    @SerializedName("id")            val id: String,
    @SerializedName("name")          val name: String,
    @SerializedName("category")      val category: String,
    @SerializedName("serial_number") val serialNumber: String,
    @SerializedName("description")   val description: String,
    @SerializedName("location")      val location: String,
    @SerializedName("status")        val status: String,
    @SerializedName("created_at")    val createdAt: Long,
    @SerializedName("updated_at")    val updatedAt: Long
)

data class AssetBatchDto(
    @SerializedName("assets") val assets: List<AssetDto>
)

// ── Transaction DTOs ──────────────────────────────────────────────────────────

data class TransactionDto(
    @SerializedName("id")                  val id: String,
    @SerializedName("asset_id")            val assetId: String,
    @SerializedName("asset_name")          val assetName: String,
    @SerializedName("asset_serial_number") val assetSerialNumber: String,
    @SerializedName("type")                val type: String,
    @SerializedName("recipient_name")      val recipientName: String,
    @SerializedName("destination")         val destination: String,
    @SerializedName("notes")              val notes: String,
    @SerializedName("timestamp_ms")        val timestampMs: Long,
    @SerializedName("latitude")            val latitude: Double?,
    @SerializedName("longitude")           val longitude: Double?,
    @SerializedName("gps_accuracy_meters") val gpsAccuracyMeters: Float?,
    @SerializedName("evidence_filename")   val evidenceFilename: String? = null,
    @SerializedName("evidence_type")       val evidenceType: String? = null
)

data class TransactionBatchDto(
    @SerializedName("transactions") val transactions: List<TransactionDto>
)

// ── Master Sync Response ──────────────────────────────────────────────────────

data class MasterSyncResponseDto(
    @SerializedName("total_assets")        val totalAssets: Int,
    @SerializedName("total_transactions")  val totalTransactions: Int,
    @SerializedName("assets")             val assets: List<AssetDto>,
    @SerializedName("transactions")        val transactions: List<TransactionDto>,
    @SerializedName("server_timestamp_ms") val serverTimestampMs: Long
)

// ── Sync Response ─────────────────────────────────────────────────────────────

data class SyncResponseDto(
    @SerializedName("synced_count") val syncedCount: Int,
    @SerializedName("failed_ids")   val failedIds: List<String>,
    @SerializedName("message")      val message: String
)

// ── Evidence Upload Response ──────────────────────────────────────────────────

data class EvidenceUploadResponseDto(
    @SerializedName("transaction_id") val transactionId: String,
    @SerializedName("filename")       val filename: String,
    @SerializedName("message")        val message: String
)
