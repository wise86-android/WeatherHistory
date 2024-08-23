package com.wise.weatherhistory

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.wise.weatherhistory.model.GeocodingService
import com.wise.weatherhistory.model.KTorGeocodingService
import com.wise.weatherhistory.model.KTorWeatherHistoryService
import com.wise.weatherhistory.model.Location
import com.wise.weatherhistory.model.WeatherData
import com.wise.weatherhistory.ui.components.Search
import com.wise.weatherhistory.ui.components.TemperaturePlot
import com.wise.weatherhistory.ui.theme.WeatherHistoryTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = MainViewModel()
        setContent {
            val meteoData by viewModel.meteoData.collectAsState()
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
class MainViewModel : ViewModel() {

    val geocodingService = KTorGeocodingService()

    //first state whether the search is happening or not
    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    //second state the text typed by the user
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    //third state the list to be filtered
    private val _locationList = MutableStateFlow<List<Location>>(emptyList())
    val locationList = _locationList.asStateFlow()

    private val _meteoData = MutableStateFlow<List<WeatherData>>(emptyList())
    val meteoData = _meteoData.asStateFlow()
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
        viewModelScope.launch {
            val data = KTorWeatherHistoryService().getWeatherData(location, lastWeek())
            _meteoData.update { data }
        }
    }

}

fun lastWeek():ClosedRange<LocalDate>{
    val today = LocalDate.now()
    return today.minusDays(1L)..today
}


@Composable
fun mpChart(data:List<WeatherData>){
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.4f),
        factory = { context ->
        com.github.mikephil.charting.charts.LineChart(context).apply {

        }},
            update = {view ->
                val temp = data.map { com.github.mikephil.charting.data.Entry(it.time.toEpochSecond(
                    ZoneOffset.UTC).toFloat(),it.temperature) }
                val lineData = LineData(LineDataSet(temp,"temp").apply {
                    this.setDrawCircles(false)
                    this.setDrawCircleHole(false)
                })
                lineData.setDrawValues(false)
                view.data = lineData
                view.notifyDataSetChanged()
            }
    )
}
