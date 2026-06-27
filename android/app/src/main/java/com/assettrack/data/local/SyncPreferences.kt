package com.assettrack.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sync_prefs")

@Singleton
class SyncPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val LAST_SYNC_TIMESTAMP_MS = longPreferencesKey("last_sync_timestamp_ms")
    }

    suspend fun getLastSync(): Long {
        return context.dataStore.data.map { preferences ->
            preferences[LAST_SYNC_TIMESTAMP_MS] ?: 0L
        }.first()
    }

    suspend fun saveLastSync(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIMESTAMP_MS] = timestamp
        }
    }
}
