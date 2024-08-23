package com.wise.weatherhistory.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wise.weatherhistory.model.WeatherQuerySettingsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val dataStore: WeatherQuerySettingsService) : ViewModel(){
    val pastDays = dataStore.getLastTimeRange()
        .map { it.toDays() }
        .stateIn(scope = viewModelScope,
            initialValue = 7L,
            started = SharingStarted.WhileSubscribed(5000)
        )

    fun setPastDay(value:Long){
        viewModelScope.launch {
            dataStore.setDefaultTimeRange(Duration.ofDays(value))
        }
    }
}