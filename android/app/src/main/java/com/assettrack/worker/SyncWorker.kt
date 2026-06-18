package com.assettrack.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.assettrack.data.MasterBarangManager
import com.assettrack.domain.repository.AssetRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: AssetRepository,
    private val masterBarangManager: MasterBarangManager
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "SyncWorker"

        // Tag harus konsisten — diobserve di DashboardViewModel
        const val WORK_NAME = "assettrack_sync"
        const val TAG_PERIODIC = "assettrack_sync"
        const val TAG_IMMEDIATE = "assettrack_sync_immediate"

        fun enqueuePeriodicSync(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .addTag(TAG_PERIODIC)    // ← tag ini diobserve UI
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun triggerImmediateSync(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .addTag(TAG_PERIODIC)    // ← sama, supaya UI bisa observe
                .addTag(TAG_IMMEDIATE)
                .build()

            workManager.enqueueUniqueWork(
                "immediate_sync",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        Log.i(TAG, "=== Sync started (attempt ${runAttemptCount + 1}) ===")
        return try {
            val result = repository.syncPendingData()

            Log.i(TAG, "↑ Push: ${result.assetsSynced} assets, ${result.transactionsSynced} tx")
            Log.i(TAG, "↑ Evidence: ${result.evidenceUploaded} files uploaded")
            Log.i(TAG, "↓ Pull: ${result.assetsPulled} assets, ${result.transactionsPulled} tx")

            if (result.errors.isNotEmpty()) {
                Log.w(TAG, "Errors (${result.errors.size}): ${result.errors.take(3)}")
            }

            // Bersihkan file bukti lama (>90 hari)
            masterBarangManager.cleanOldFiles(olderThanDays = 90)

            // Gagal total → retry
            if (result.errors.size > 3 &&
                result.assetsSynced == 0 && result.transactionsSynced == 0 &&
                result.assetsPulled == 0) {
                Log.w(TAG, "All operations failed, scheduling retry...")
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            } else {
                Log.i(TAG, "=== Sync complete ===")
                Result.success()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync crashed: ${e.message}", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
