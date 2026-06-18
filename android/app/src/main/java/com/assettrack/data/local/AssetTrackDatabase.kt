package com.assettrack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.assettrack.data.local.dao.AssetDao
import com.assettrack.data.local.dao.TransactionDao
import com.assettrack.data.local.entity.AssetEntity
import com.assettrack.data.local.entity.TransactionEntity
import com.assettrack.domain.model.AssetStatus
import com.assettrack.domain.model.TransactionType

class AssetStatusConverter {
    @TypeConverter fun fromStatus(v: AssetStatus): String = v.name
    @TypeConverter fun toStatus(v: String): AssetStatus = AssetStatus.valueOf(v)
}

class TransactionTypeConverter {
    @TypeConverter fun fromType(v: TransactionType): String = v.name
    @TypeConverter fun toType(v: String): TransactionType = TransactionType.valueOf(v)
}

@Database(
    entities = [AssetEntity::class, TransactionEntity::class],
    version = 2,                    // naik dari 1 → 2 karena ada field baru
    exportSchema = false
)
@TypeConverters(AssetStatusConverter::class, TransactionTypeConverter::class)
abstract class AssetTrackDatabase : RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "assettrack.db"
    }
}
