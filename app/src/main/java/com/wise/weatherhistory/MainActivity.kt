package com.wise.weatherhistory

import android.content.Entity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.style.ChartStyle
import com.patrykandpatrick.vico.core.axis.Axis
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.scale.AutoScaleUp
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entriesOf
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import com.wise.weatherhistory.model.KTorGeocodingService
import com.wise.weatherhistory.model.KTorWeatherHistoryService
import com.wise.weatherhistory.model.Location
import com.wise.weatherhistory.model.WeatherData
import com.wise.weatherhistory.ui.theme.WeatherHistoryTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherHistoryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
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