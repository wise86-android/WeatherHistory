package com.wise.weatherhistory.model

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL

class KTorGeocodingService : GeocodingService{

    private val baseUrl = URL("https://geocoding-api.open-meteo.com/v1/search")

    override suspend fun getLocations(name: String,requestParameter: GeocodingService.RequestParameter): List<Location> {
        if(name.isBlank()){
            return emptyList()
        }
        val response = HttpClient.get(baseUrl) {
            url {
                parameters.append("name", name)
                parameters.append("count",requestParameter.requestLimit.toString())
                    //append("language",requestParameter.locale.language)
                parameters.append("format","json")

            }

        }

        val data = response.body<Response>()
        return data.results.mapNotNull {
            if(it.country==null){
                return@mapNotNull null
            }
            Location(
                latitude = it.latitude,
                longitude = it.longitude,
                name = it.name,
                country = it.country,
                elevation = it.elevation
            )
        }

    }



    @Serializable
    private class Response(
        val results:Array<ResponseItem> = emptyArray()
    )

    @Serializable
    private class ResponseItem(                            // Proto order number
        val name: String,                           // #02
        val latitude: Float,               // #04
        val longitude: Float,              // #05
        val elevation: Float,              // #07
        val country: String?=null,                        // #19
    )

}

