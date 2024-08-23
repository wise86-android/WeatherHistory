package com.wise.weatherhistory.service

import android.os.Build
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.EMPTY
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun buildHttpClient(engine: HttpClientEngine) = HttpClient(engine){
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys=true
            isLenient = true
        })
    }
    install(Logging) {
        logger = Logger.ANDROID
        level = LogLevel.ALL
    }
}