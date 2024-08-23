package com.wise.weatherhistory.model.previewUtils

import com.wise.weatherhistory.model.WeatherData
import java.time.LocalDateTime
import kotlin.random.Random

fun hourTimeGenerator(startFrom: LocalDateTime = LocalDateTime.now()):Sequence<LocalDateTime>{
    return generateSequence(startFrom) { it.plusHours(1) }
}
fun randomWeatherData(time: LocalDateTime): WeatherData {
    return WeatherData(
        time = time,
        temperature = Random.nextDouble(-10.0,30.0).toFloat(),
        precipitation = Random.nextDouble(-10.0,30.0).toFloat(),
        rain = Random.nextDouble(-10.0,30.0).toFloat(),
        showers = Random.nextDouble(-10.0,30.0).toFloat(),
        snowfall = Random.nextDouble(-10.0,30.0).toFloat(),
        snowDepth = Random.nextDouble(-10.0,30.0).toFloat())
}

fun randomListWeatherData(size:Int):List<WeatherData>{
    val timeSequence = hourTimeGenerator()
    return timeSequence.take(size).map { randomWeatherData(it) }.toList()
}