package com.assettrack.data.local.dao

import androidx.room.*
import com.assettrack.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    // ── Observe ───────────────────────────────────────────────────────────────

    @Query("SELECT * FROM transactions ORDER BY timestampMs DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE assetName LIKE '%' || :query || '%'
           OR assetSerialNumber LIKE '%' || :query || '%'
           OR recipientName LIKE '%' || :query || '%'
        ORDER BY timestampMs DESC
    """)
    fun search(query: String): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions WHERE isSynced = 0")
    fun observePendingCount(): Flow<Int>

    // ── Query ─────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TransactionEntity?

    @Query("SELECT id FROM transactions WHERE id IN (:ids)")
    suspend fun getExistingIds(ids: List<String>): List<String>

    // Transaksi yang belum dikirim ke server
    @Query("SELECT * FROM transactions WHERE isSynced = 0")
    suspend fun getPending(): List<TransactionEntity>

    // Transaksi yang sudah sync tapi evidence belum diupload
    @Query("""
        SELECT * FROM transactions 
        WHERE isSynced = 1 
          AND evidenceFilePath IS NOT NULL 
          AND isEvidenceUploaded = 0
    """)
    suspend fun getPendingEvidenceUpload(): List<TransactionEntity>

    // ── Insert / Update ───────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfNotExists(tx: TransactionEntity)

    @Query("UPDATE transactions SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>)

    @Query("UPDATE transactions SET isEvidenceUploaded = 1 WHERE id = :id")
    suspend fun markEvidenceUploaded(id: String)
}
