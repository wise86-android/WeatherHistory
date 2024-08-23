package com.wise.weatherhistory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.wise.weatherhistory.model.WeatherQuerySettingsStoreData
import com.wise.weatherhistory.ui.SettingsPage
import com.wise.weatherhistory.ui.SettingsViewModel
import com.wise.weatherhistory.ui.components.Search
import com.wise.weatherhistory.ui.components.TemperaturePlot
import com.wise.weatherhistory.ui.theme.WeatherHistoryTheme
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = hiltViewModel<MainViewModel>()
            val meteoData by viewModel.meteoData.collectAsState(emptyList())
            WeatherHistoryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(topBar = { TopAppBar( colors = topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),title = { Text(text = "title")}) }){
                        Column(modifier = Modifier.padding(it)) {
                            Search(viewModel)
                            Text(text = "Weather")
                            SettingsPage()
                            if(meteoData.size > 0) {
                                TemperaturePlot(data = meteoData)
                            }
                        }

                    }
                }
            }
        }
    }
}


