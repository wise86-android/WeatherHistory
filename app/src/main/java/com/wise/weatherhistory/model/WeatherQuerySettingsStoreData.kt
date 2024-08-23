package com.wise.weatherhistory.model

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Duration

class WeatherQuerySettingsStoreData(private val context: Context) : WeatherQuerySettings {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("time_range_settings")

    val TIME_RANGE_DAY = longPreferencesKey("time_range_days")

    override suspend fun setDefaultTimeRange(defaultPassedTime: Duration) {
        context.dataStore.edit { preferences ->
            Log.d("DataStore","set: "+defaultPassedTime.toString())
            preferences[TIME_RANGE_DAY] = defaultPassedTime.toDays()
        }
    }

    override fun getDefaultTimeRange(): Flow<Duration> {
        return context.dataStore.data.map {
            val days = it[TIME_RANGE_DAY] ?: 7
            Log.d("DataStore","get: "+days)
            Duration.ofDays(days)
        }
    }
}