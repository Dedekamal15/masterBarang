package com.assettrack.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.assettrack.BuildConfig
import com.assettrack.R
import com.assettrack.domain.repository.AssetRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SyncForegroundService : Service() {

    @Inject lateinit var repository: AssetRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val CHANNEL_ID = "masterBarang_sync"
    private val NOTIF_ID = 2001

    // Ambil dari BuildConfig supaya konsisten dengan Retrofit
    private val SERVER_HOST = BuildConfig.SERVER_HOST
    private val SERVER_PORT = BuildConfig.SERVER_PORT

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("Memulai..."))
        startSyncLoop()
    }

    private fun startSyncLoop() {
        scope.launch {
            while (isActive) {
                if (isServerReachable()) {
                    try {
                        Log.i("SyncService", "▶ Server reachable, memulai sync...")
                        val result = repository.syncPendingData()
                        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

                        val pushOk = result.assetsSynced > 0 || result.transactionsSynced > 0
                        val msg = if (pushOk) {
                            "✓ ${time} · ${result.assetsSynced} aset · ${result.transactionsSynced} tx"
                        } else {
                            "Standby ${time} — tidak ada data baru"
                        }

                        if (result.errors.isNotEmpty()) {
                            Log.w("SyncService", "Errors: ${result.errors}")
                        }

                        Log.i("SyncService", msg)
                        updateNotification(msg)

                    } catch (e: Exception) {
                        Log.e("SyncService", "✗ Sync error: ${e.message}", e)
                        updateNotification("Error sync — coba lagi...")
                    }
                } else {
                    Log.w("SyncService", "⚠ Server tidak reachable dari jaringan ini, skip sync")
                    updateNotification("⚠ Tidak terhubung ke jaringan server")
                }

                delay(3 * 60 * 1000L)
            }
        }
    }

    private fun isServerReachable(): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(SERVER_HOST, SERVER_PORT), 3000)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun updateNotification(text: String) {
        getSystemService(NotificationManager::class.java)?.notify(NOTIF_ID, buildNotification(text))
    }

    private fun buildNotification(text: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MasterBarang")
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    private fun createNotificationChannel() {
        NotificationChannel(CHANNEL_ID, "Sinkronisasi Aset", NotificationManager.IMPORTANCE_LOW)
            .apply {
                description = "Proses sync data MasterBarang"
                setShowBadge(false)
            }.also {
                getSystemService(NotificationManager::class.java)?.createNotificationChannel(it)
            }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onDestroy() { scope.cancel(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}