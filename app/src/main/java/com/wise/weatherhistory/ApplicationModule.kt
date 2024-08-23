package com.wise.weatherhistory

import android.content.Context
import com.wise.weatherhistory.model.GeocodingService
import com.wise.weatherhistory.model.KTorGeocodingService
import com.wise.weatherhistory.model.KTorWeatherHistoryService
import com.wise.weatherhistory.model.WeatherHistoryService
import com.wise.weatherhistory.model.WeatherQuerySettings
import com.wise.weatherhistory.model.WeatherQuerySettingsStoreData
import com.wise.weatherhistory.model.buildHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.internal.modules.ApplicationContextModule
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ApplicationSingletonModule {

    @Provides
    @Singleton
    fun provideHttpClient():HttpClient{
        return buildHttpClient(OkHttp.create())
    }

    @Provides
    @Singleton
    fun provideWeatherHistoryService(httpClient: HttpClient):WeatherHistoryService{
        return KTorWeatherHistoryService(httpClient)
    }

    @Provides
    @Singleton
    fun provideGeo(httpClient: HttpClient):GeocodingService{
        return KTorGeocodingService(httpClient)
    }
    @Provides
    @Singleton
    fun provideWeatherQuerySettings(@ApplicationContext context:Context):WeatherQuerySettings{
        return WeatherQuerySettingsStoreData(context)
    }
}
