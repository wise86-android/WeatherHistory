package com.wise.weatherhistory.model

import java.util.Locale

interface GeocodingService {
    suspend fun getLocations(locationName:String, requestParameter:RequestParameter=RequestParameter()):List<Location>

    data class RequestParameter(val requestLimit:Int = 10,val locale:Locale=Locale.getDefault())
}