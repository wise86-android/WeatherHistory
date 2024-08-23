package com.wise.weatherhistory.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
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
class SettingsViewModel @Inject constructor(private val dataStore:WeatherQuerySettingsService) : ViewModel(){
    val pastDays = dataStore.getLastTimeRange().map { it.toDays() }.stateIn(scope = viewModelScope, initialValue = 7L, started = SharingStarted.WhileSubscribed(5000))

    fun setPastDay(value:Long){
        viewModelScope.launch {
            dataStore.setDefaultTimeRange(Duration.ofDays(value))
        }
    }
}

@Composable
fun SettingsPage(viewModel: SettingsViewModel = hiltViewModel()){
    val selectedValue by viewModel.pastDays.collectAsState()
    var slideValue by remember { mutableFloatStateOf(selectedValue.toFloat()) }
    Column {
        Text(text = "select day")
        Slider(value = slideValue, onValueChange = {slideValue = it} , onValueChangeFinished = {viewModel.setPastDay(slideValue.toLong())}, valueRange = 1.0f..30.0f , steps =30)
    }
}

