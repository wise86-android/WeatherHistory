package com.wise.weatherhistory.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.dataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wise.weatherhistory.model.WeatherQuerySettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration


class SettingsViewModel(private val dataStore:WeatherQuerySettings) : ViewModel(){
    val pastDays = dataStore.getDefaultTimeRange().map { it.toDays() }.stateIn(scope = viewModelScope, initialValue = 7L, started = SharingStarted.WhileSubscribed(5000))

    fun setPastDay(value:Long){
        viewModelScope.launch {
            dataStore.setDefaultTimeRange(Duration.ofDays(value))
        }
    }
}

@Composable
fun SettingsPage(viewModel: SettingsViewModel){
    val selectedValue by viewModel.pastDays.collectAsState()

    Column {
        Text(text = "select day")
        Slider(value = selectedValue.toFloat(), onValueChange = { viewModel.setPastDay(it.toLong())} , valueRange = 1.0f..30.0f , steps =30)
    }
}

