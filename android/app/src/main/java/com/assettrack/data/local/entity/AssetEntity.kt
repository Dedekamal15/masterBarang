package com.assettrack.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.assettrack.domain.model.AssetStatus

@Entity(
    tableName = "assets",
    indices = [Index(value = ["serialNumber"], unique = true)]
)
data class AssetEntity(
    @PrimaryKey val id: String,
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
