package com.wise.weatherhistory.model

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val latitude: Float,
    val longitude: Float,
    val elevation:Float,
    val name: String,
    val country: String,
    val region:String? = null
)
