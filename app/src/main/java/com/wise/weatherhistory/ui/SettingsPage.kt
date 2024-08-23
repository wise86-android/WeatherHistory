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

@Composable
fun SettingsPage(viewModel: SettingsViewModel = hiltViewModel()){
    val selectedValue by viewModel.pastDays.collectAsState()
    var slideValue by remember(selectedValue) { mutableFloatStateOf(selectedValue.toFloat()) }
    Column {
        Text(text = "Display values in the last ${slideValue.toLong()} days")
        Slider(value = selectedValue.toFloat(),
            onValueChange = {slideValue = it} ,
            onValueChangeFinished = {viewModel.setPastDay(slideValue.toLong())},
            valueRange = 1.0f..30.0f , steps =30)
    }
}

