package com.wise.weatherhistory.model

import java.time.LocalDate
import java.time.LocalDateTime

interface WeatherHistoryService {
    suspend fun getWeatherData(location: Location,range:ClosedRange<LocalDate>):List<WeatherData>
}

data class WeatherData(
    val time:LocalDateTime,
    val temperature:Float,
    val precipitation:Float,
    val rain:Float,
    val showers:Float,
    val snowfall:Float,
    val snowDepth:Float
)