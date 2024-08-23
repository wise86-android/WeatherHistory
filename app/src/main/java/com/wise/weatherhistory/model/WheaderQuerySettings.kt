package com.wise.weatherhistory.model

import kotlinx.coroutines.flow.Flow
import java.time.Duration

interface WeatherQuerySettingsService {
    suspend fun setDefaultTimeRange(defaultPassedTime:Duration)
    fun getLastTimeRange(): Flow<Duration>

    fun getLastLocation():Flow<Location>
    suspend fun setLastLocation(location: Location)

}