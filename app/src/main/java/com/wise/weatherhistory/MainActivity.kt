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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
                    Scaffold(topBar = { Search(viewModel) }){
                        Text(text = "Weather", modifier = Modifier.padding(it))
                        Text(text = "data: " + meteoData.size)
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
        _searchText.value = text
        _isSearching.value = !_isSearching.value
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
        Log.d("Vm",location.toString())
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
fun Greeting(name: String, modifier: Modifier = Modifier) {
    var requestName by remember { mutableStateOf("Padova") }
    var results by remember { mutableStateOf(emptyList<Location>()) }
    val firstResult = results.firstOrNull()
    val scope = rememberCoroutineScope()
    LaunchedEffect(key1 = requestName){
        scope.launch{
            results=KTorGeocodingService().getLocations(requestName)
            Log.d("location","Result"+results)
        }
    }

    var weatherData by remember { mutableStateOf(emptyList<WeatherData>()) }
    LaunchedEffect(key1 = firstResult){
        scope.launch (Dispatchers.IO){
            if(firstResult!=null) {
                weatherData = KTorWeatherHistoryService().getWeatherData(firstResult,LocalDate.now().minusDays(1)..LocalDate.now())
            }
        }
    }

    val temperature by remember {
        derivedStateOf {
            val x = weatherData.mapIndexed{ index, weatherData ->  entryOf(index,weatherData.temperature) }
            ChartEntryModelProducer(x)
        }
    }

    Column {
        TextField(value = requestName, onValueChange = { requestName = it })
        LazyColumn {
            items(results) {
                Text(text = "${it.name} -> ${it.country}")
            }
        }
        Text(text = "Weather")
        Chart(
            chart = columnChart(spacing = 1.dp),
            chartModelProducer = temperature,
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(),
        )
        mpChart(data = weatherData)
    }
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WeatherHistoryTheme {
        Greeting("Android")
    }
}


private val countries = listOf(
    "Afghanistan",
    "Albania",
    "Algeria",
    "Andorra",
    "Angola",
    "Antigua and Barbuda",
    "Argentina",
    "Armenia",
    "Australia",
    "Austria",
    "Azerbaijan",
    "Bahamas",
    "Bahrain",
    "Bangladesh",
    "Barbados",
    "Belarus",
    "Belgium",
    "Belize",
    "Benin",
    "Bhutan",
    "Bolivia",
    "Bosnia and Herzegovina",
    "Botswana",
    "Brazil",
    "Brunei",
    "Bulgaria",
    "Burkina Faso",
    "Burundi",
    "Cambodia",
    "Cameroon",
    "Canada",
    "Cape Verde",
    "Central African Republic",
    "Chad",
    "Chile",
    "China",
    "Colombia",
    "Comoros",
    "Congo (Brazzaville)",
    "Congo (Kinshasa)",
    "Costa Rica",
    "Croatia",
    "Cuba",
    "Cyprus",
    "Czech Republic",
    "Denmark",
    "Djibouti",
    "Dominica",
    "Dominican Republic",
    "Ecuador",
    "Egypt",
    "El Salvador",
    "Equatorial Guinea",
    "Eritrea",
    "Estonia",
    "Eswatini",
    "Ethiopia",
    "Fiji",
    "Finland",
    "France",
    "Gabon",
    "Gambia",
    "Georgia",
    "Germany",
    "Ghana",
    "Greece",
    "Grenada",
    "Guatemala",
    "Guinea",
    "Guinea-Bissau",
    "Guyana",
    "Haiti",
    "Holy See",
    "Honduras",
    "Hungary",
    "Iceland",
    "India",
    "Indonesia",
    "Iran",
    "Iraq",
    "Ireland",
    "Israel",
    "Italy",
    "Ivory Coast",
    "Jamaica",
    "Japan",
    "Jordan",
    "Kazakhstan",
    "Kenya",
    "Kiribati",
    "Kuwait",
    "Kyrgyzstan",
    "Laos",
    "Latvia",
    "Lebanon",
    "Lesotho",
    "Liberia",
    "Libya",
    "Liechtenstein",
    "Lithuania",
    "Luxembourg",
    "Madagascar",
    "Malawi",
    "Malaysia",
    "Maldives",
    "Mali",
    "Malta",
    "Marshall Islands",
    "Mauritania",
    "Mauritius",
    "Mexico",
    "Micronesia",
    "Moldova",
    "Monaco",
    "Mongolia",
    "Montenegro",
    "Morocco",
    "Mozambique",
    "Myanmar",
    "Namibia",
    "Nauru",
    "Nepal",
    "Netherlands",
    "New Zealand",
    "Nicaragua",
    "Niger",
    "Nigeria",
    "North Korea",
    "North Macedonia",
    "Norway",
    "Oman",
    "Pakistan",
    "Palau",
    "Palestine State",
    "Panama",
    "Papua New Guinea",
    "Paraguay",
    "Peru",
    "Philippines",
    "Poland",
    "Portugal",
    "Qatar",
    "Romania",
    "Russia",
    "Rwanda",
    "Saint Kitts and Nevis",
    "Saint Lucia",
    "Saint Vincent and the Grenadines",
    "Samoa",
    "San Marino",
    "Sao Tome and Principe",
    "Saudi Arabia",
    "Senegal",
    "Serbia",
    "Seychelles",
    "Sierra Leone",
    "Singapore",
    "Slovakia",
    "Slovenia",
    "Solomon Islands",
    "Somalia",
    "South Africa",
    "South Korea",
    "South Sudan",
    "Spain",
    "Sri Lanka",
    "Sudan",
    "Suriname",
    "Sweden",
    "Switzerland",
    "Syria",
    "Taiwan",
    "Tajikistan",
    "Tanzania",
    "Thailand",
    "Timor-Leste",
    "Togo",
    "Tonga",
    "Trinidad and Tobago",
    "Tunisia",
    "Turkey",
    "Turkmenistan",
    "Tuvalu",
    "Uganda",
    "Ukraine",
    "United Arab Emirates",
    "United Kingdom",
    "United States of America",
    "Uruguay",
    "Uzbekistan",
    "Vanuatu",
    "Venezuela",
    "Vietnam",
    "Yemen",
    "Zambia",
    "Zimbabwe"
)