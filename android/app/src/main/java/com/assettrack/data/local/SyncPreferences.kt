package com.assettrack.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences>
    by preferencesDataStore(name = "sync_prefs")

@Singleton
class SyncPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_LAST_SYNC_MS = longPreferencesKey("last_sync_timestamp_ms")
    }

    /** Timestamp (epoch ms) terakhir kali pull berhasil dari server. 0 = belum pernah sync. */
    suspend fun getLastSyncMs(): Long =
        context.dataStore.data
            .map { it[KEY_LAST_SYNC_MS] ?: 0L }
            .first()

    /** Simpan timestamp setelah pull berhasil. */
    suspend fun saveLastSyncMs(timestampMs: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_SYNC_MS] = timestampMs
        }
    }

    /** Reset — paksa full sync di berikutnya. */
    suspend fun reset() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_LAST_SYNC_MS)
        }
    }
}
