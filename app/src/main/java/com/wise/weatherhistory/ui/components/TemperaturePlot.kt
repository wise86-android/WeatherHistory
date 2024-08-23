package com.wise.weatherhistory.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.wise.weatherhistory.model.WeatherData
import com.wise.weatherhistory.model.previewUtils.randomListWeatherData

@Composable
fun TemperaturePlot(data:List<WeatherData>){
    val x = data.mapIndexed{ index, weatherData ->  entryOf(index,weatherData.temperature) }
    val plotDataModel = ChartEntryModelProducer(x)
    return Chart(
        chart = lineChart(),
        chartModelProducer = plotDataModel,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(),
    )
}

@Preview
@Composable
fun TemperaturePlotPreview(){
    val data = randomListWeatherData(12)
    TemperaturePlot(data = data)
}
