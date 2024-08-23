package com.wise.weatherhistory.ui.components

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.wise.weatherhistory.model.WeatherData
import com.wise.weatherhistory.model.previewUtils.randomListWeatherData
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun TemperaturePlot(data:List<WeatherData>){
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.4f),
        factory = { context ->
            com.github.mikephil.charting.charts.LineChart(context).apply {
                axisRight.isEnabled=false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.valueFormatter = object : IAxisValueFormatter {
                    val dateFormatter = DateTimeFormatter.ofPattern("EEE - HH")
                    override fun getFormattedValue(value: Float, axis: AxisBase?): String? {
                        val epocSeconds = value.toLong()
                        val time = LocalDateTime.ofEpochSecond(epocSeconds,0, ZoneOffset.UTC)
                        return time.format(dateFormatter)
                    }
                }
                xAxis.labelRotationAngle = -45.0f
                description.text= "Tempeture C"
                legend.isEnabled = false
            }},
        update = {view ->
            val temp = data.map { Entry(it.time.toEpochSecond(
                ZoneOffset.UTC).toFloat(),it.temperature) }
            val lineData = LineData(LineDataSet(temp,"temp").apply {
                setDrawCircles(false)
                setDrawCircleHole(false)
                color = Color.RED
            })
            lineData.setDrawValues(false)
            view.data = lineData
            view.notifyDataSetChanged()
        }
    )
}

@Preview
@Composable
fun TemperaturePlotPreview2(){
    val data = randomListWeatherData(12)
    TemperaturePlot(data = data)
}
