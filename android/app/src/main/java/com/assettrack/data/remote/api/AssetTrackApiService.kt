package com.assettrack.data.remote.api

import com.assettrack.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface AssetTrackApiService {

    // ── PULL: tarik semua data dari server ke device ─────────────────────────
    // Dipanggil saat standby/sync: since_ms=0 → semua data
    @GET("api/v1/master-sync")
    suspend fun masterSync(
        @Query("since_ms") sinceMs: Long = 0
    ): Response<MasterSyncResponseDto>

    // ── PUSH: kirim data lokal ke server ────────────────────────────────────
    @POST("api/v1/assets/batch")
    suspend fun syncAssets(@Body payload: AssetBatchDto): Response<SyncResponseDto>

    @POST("api/v1/transactions/batch")
    suspend fun syncTransactions(@Body payload: TransactionBatchDto): Response<SyncResponseDto>

    // ── UPLOAD: file bukti transaksi ─────────────────────────────────────────
    @Multipart
    @POST("api/v1/transactions/{transactionId}/evidence")
    suspend fun uploadEvidence(
        @Path("transactionId") transactionId: String,
        @Part file: MultipartBody.Part
    ): Response<EvidenceUploadResponseDto>

    // ── Health check ─────────────────────────────────────────────────────────
    @GET("api/v1/health")
    suspend fun healthCheck(): Response<Unit>
}
