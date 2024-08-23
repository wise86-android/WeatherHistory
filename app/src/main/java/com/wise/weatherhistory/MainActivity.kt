package com.wise.weatherhistory

import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wise.weatherhistory.model.GeocodingService
import com.wise.weatherhistory.model.KTorGeocodingService
import com.wise.weatherhistory.model.KTorWeatherHistoryService
import com.wise.weatherhistory.model.Location
import com.wise.weatherhistory.model.WeatherQuerySettings
import com.wise.weatherhistory.model.WeatherQuerySettingsStoreData
import com.wise.weatherhistory.ui.SettingsPage
import com.wise.weatherhistory.ui.SettingsViewModel
import com.wise.weatherhistory.ui.components.Search
import com.wise.weatherhistory.ui.components.TemperaturePlot
import com.wise.weatherhistory.ui.theme.WeatherHistoryTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val querySettings = WeatherQuerySettingsStoreData(this)
        val viewModel = MainViewModel(querySettings)
        val settingsViewModel = SettingsViewModel(querySettings)
        setContent {
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
                            SettingsPage(viewModel = settingsViewModel)
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


@OptIn(ExperimentalCoroutinesApi::class)
@FlowPreview
class MainViewModel(private val querySettings: WeatherQuerySettings) : ViewModel() {

    val geocodingService = KTorGeocodingService()
    val historyService = KTorWeatherHistoryService()

    //first state whether the search is happening or not
    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    //second state the text typed by the user
    private val _searchText = MutableStateFlow("")
    val searchText = isSearching.map { if(it){ _searchText } else sele

    //third state the list to be filtered
    val locationList =
        _searchText
            .debounce(500)
            .mapLatest { text-> geocodingService.getLocations(text, GeocodingService.RequestParameter(4)) }
            .stateIn(viewModelScope,
                SharingStarted.WhileSubscribed(300),
                emptyList()
            )

    private val selectedLocation= querySettings.getLastLocation()
    
    private val daysToLoad = querySettings.getLastTimeRange()
        .map { val today = LocalDate.now(); today.minusDays(it.toDays()).. today }
        
    val meteoData = selectedLocation
        .combine(daysToLoad,historyService::getWeatherData)
        .map { Log.d("MainViewModel","DataSize: ${it.size}");it }
        .stateIn(viewModelScope,
            SharingStarted.WhileSubscribed(300),
            emptyList(),

        )

    fun onSearchTextChange(text: String) {
        _searchText.update { text }
    }

    fun takeFirstResult(text: String) {
        locationList.value.firstOrNull()?.let(::onSelectLocation)
    }

    fun onToogleSearch() {
        _isSearching.value = !_isSearching.value
        if (!_isSearching.value) {
            onSearchTextChange("")
        }
    }

    fun onSelectLocation(location: Location){
        _isSearching.value = !_isSearching.value
        viewModelScope.launch {
            querySettings.setLastLocation(location)
        }

    }

}