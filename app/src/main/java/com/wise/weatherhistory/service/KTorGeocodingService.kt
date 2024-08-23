package com.wise.weatherhistory.service

import com.wise.weatherhistory.model.GeocodingService
import com.wise.weatherhistory.model.Location
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

import kotlinx.serialization.Serializable

import java.net.URL
import javax.inject.Inject

class KTorGeocodingService @Inject constructor(private val httpClient: HttpClient ) :
    GeocodingService {

    private val baseUrl = URL("https://geocoding-api.open-meteo.com/v1/search")

    override suspend fun getLocations(locationName: String, requestParameter: GeocodingService.RequestParameter): List<Location> {
        if(locationName.isBlank()){
            return emptyList()
        }
        val response = httpClient.get(baseUrl) {
            url {
                parameters.append("name", locationName)
                parameters.append("count",requestParameter.requestLimit.toString())
                parameters.append("language",requestParameter.locale.language)
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
                name = it.admin3 ?: it.name,
                region = it.admin1,
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
        val admin1:String?=null,
        val admin2:String?=null,
        val admin3:String?=null,
        val admin4:String?=null,
    )

}

