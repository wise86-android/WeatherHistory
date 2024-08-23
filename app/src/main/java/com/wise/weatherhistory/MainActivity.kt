package com.wise.weatherhistory

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wise.weatherhistory.model.GeocodingService
import com.wise.weatherhistory.model.KTorGeocodingService
import com.wise.weatherhistory.model.KTorWeatherHistoryService
import com.wise.weatherhistory.model.Location
import com.wise.weatherhistory.model.WeatherData
import com.wise.weatherhistory.model.WeatherQuerySettings
import com.wise.weatherhistory.model.WeatherQuerySettingsStoreData
import com.wise.weatherhistory.ui.SettingsPage
import com.wise.weatherhistory.ui.SettingsViewModel
import com.wise.weatherhistory.ui.components.Search
import com.wise.weatherhistory.ui.components.TemperaturePlot
import com.wise.weatherhistory.ui.theme.WeatherHistoryTheme
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

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


@FlowPreview
class MainViewModel(querySettings: WeatherQuerySettings) : ViewModel() {

    val geocodingService = KTorGeocodingService()
    val historyService = KTorWeatherHistoryService()

    //first state whether the search is happening or not
    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    //second state the text typed by the user
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    //third state the list to be filtered
    private val _locationList = MutableStateFlow<List<Location>>(emptyList())
    val locationList = _locationList.asStateFlow()

    private val selectedLocation= MutableStateFlow<Location>(Location(0.0f,0.0f,0.0f,"",""))
    
    private val daysToLoad = querySettings.getDefaultTimeRange()
        .map { val today = LocalDate.now(); today.minusDays(it.toDays()).. today }
        
    val meteoData = selectedLocation
        .combine(daysToLoad,historyService::getWeatherData)
        .map { Log.d("MainViewModel","DataSize: ${it.size}");it }
        .stateIn(viewModelScope,
            SharingStarted.WhileSubscribed(300),
            emptyList()
        )
    init {
        viewModelScope.launch {
            _searchText.debounce(500).collectLatest { text ->
                val locations = geocodingService.getLocations(text, GeocodingService.RequestParameter(4))
                _locationList.update { locations }
            }
        }
    }

    fun onSearchTextChange(text: String) {
        _searchText.update { text }
    }

    fun takeFirstResult(text: String) {
        viewModelScope.launch {
            _locationList.collectLatest {
                it.firstOrNull()?.let(::onSelectLocation)
            }
        }

    }

    fun onToogleSearch() {
        _isSearching.value = !_isSearching.value
        if (!_isSearching.value) {
            onSearchTextChange("")
        }
    }

    fun onSelectLocation(location: Location){
        _isSearching.value = !_isSearching.value
        _searchText.update { location.name }
        selectedLocation.value = location
    }

}