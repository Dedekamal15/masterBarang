package com.assettrack.data.local.dao

import androidx.room.*
import com.assettrack.data.local.entity.AssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {

    @Query("SELECT * FROM assets ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE id = :id")
    suspend fun getById(id: String): AssetEntity?

    @Query("SELECT * FROM assets WHERE serialNumber = :sn LIMIT 1")
    suspend fun findBySerialNumber(sn: String): AssetEntity?

    @Query("""
        SELECT * FROM assets
        WHERE name LIKE '%' || :query || '%'
           OR serialNumber LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """)
    fun search(query: String): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE isSynced = 0")
    suspend fun getPending(): List<AssetEntity>

    @Query("SELECT COUNT(*) FROM assets WHERE isSynced = 0")
    fun observePendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asset: AssetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assets: List<AssetEntity>)

    @Update
    suspend fun update(asset: AssetEntity)

    @Query("UPDATE assets SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>)

    @Query("UPDATE assets SET status = :status, updatedAt = :now WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, now: Long = System.currentTimeMillis())

    @Delete
    suspend fun delete(asset: AssetEntity)
}
