package com.wise.weatherhistory.model

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
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
        logger = CustomAndroidHttpLogger
        //logger = Logger.DEFAULT
        level = LogLevel.ALL
    }
}

private object CustomAndroidHttpLogger : Logger {
    private const val logTag = "CustomAndroidHttpLogger"

    override fun log(message: String) {
        Log.i(logTag, message)
    }
}