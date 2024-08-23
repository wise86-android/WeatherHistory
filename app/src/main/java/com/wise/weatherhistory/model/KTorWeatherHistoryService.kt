package com.wise.weatherhistory.model

import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
class KTorWeatherHistoryService(engine: HttpClientEngine = OkHttp.create()) : WeatherHistoryService {
    private val httpClient = buildHttpClient(engine)

    private val baseUrl = URL("https://api.open-meteo.com/v1/forecast")

    override suspend fun getWeatherData(
        location: Location,
        range: ClosedRange<LocalDate>
    ): List<WeatherData> {
        val response = httpClient.get(baseUrl) {
            url {
                parameters.append("latitude", location.latitude.toString())
                parameters.append("longitude", location.longitude.toString())
                parameters.append("hourly","temperature_2m,apparent_temperature,precipitation,rain,showers,snowfall,snow_depth")
                parameters.append("start_date",DateTimeFormatter.ISO_DATE.format(range.start))
                parameters.append("end_date",DateTimeFormatter.ISO_DATE.format(range.endInclusive))
            }
        }
        val data = response.body<WeatherHistoryDataApiResponse>().hourly

        return data.time.mapIndexed{index, stringDate ->
            WeatherData(
                time = LocalDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(stringDate)),
                temperature = data.temperature_2m[index],
                precipitation = data.precipitation[index],
                rain = data.rain[index],
                showers = data.showers[index],
                snowfall = data.snowfall[index],
                snowDepth = data.snow_depth[index]
            )
        }
    }

    @Serializable
    data class WeatherHourlyDataApiResponse(
        val time:Array<String>,
        val temperature_2m:Array<Float>,
        val apparent_temperature:Array<Float>,
        val precipitation:Array<Float>,
        val rain:Array<Float>,
        val showers:Array<Float>,
        val snowfall:Array<Float>,
        val snow_depth :Array<Float>,
    )

    @Serializable
    data class WeatherHistoryDataApiResponse(
        val latitude:Float,
        val longitude:Float,
        val elevation:Float,
        val hourly:WeatherHourlyDataApiResponse
    )

}