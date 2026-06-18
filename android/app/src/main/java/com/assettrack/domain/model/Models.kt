package com.assettrack.domain.model

enum class AssetStatus { AVAILABLE, BORROWED, MAINTENANCE }

enum class TransactionType { CHECK_OUT, CHECK_IN }

data class Asset(
    val id: String,
    val name: String,
    val category: String,
    val serialNumber: String,
    val description: String,
    val location: String,
    val status: AssetStatus,
    val isSynced: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

data class Transaction(
    val id: String,
    val assetId: String,
    val assetName: String,
    val assetSerialNumber: String,
    val type: TransactionType,
    val recipientName: String,
    val destination: String,
    val notes: String,
    val timestampMs: Long,
    val latitude: Double?,
    val longitude: Double?,
    val gpsAccuracyMeters: Float?,
    val isSynced: Boolean,
    val evidenceFilePath: String? = null,
    val evidenceType: String? = null
)
