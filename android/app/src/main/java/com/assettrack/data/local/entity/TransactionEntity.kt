package com.assettrack.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.assettrack.domain.model.TransactionType

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AssetEntity::class,
            parentColumns = ["id"],
            childColumns = ["assetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("assetId")]
)
data class TransactionEntity(
    @PrimaryKey val id: String,
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
    // Bukti transaksi
    val evidenceFilePath: String? = null,    // path lokal di MasterBarang/bukti/
    val evidenceType: String? = null,        // "PHOTO" | "PDF" | null
    val isEvidenceUploaded: Boolean = false  // sudah diupload ke server?
)
