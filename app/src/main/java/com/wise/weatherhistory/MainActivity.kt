package com.wise.weatherhistory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.wise.weatherhistory.model.KTorGeocodingService
import com.wise.weatherhistory.model.Location
import com.wise.weatherhistory.ui.theme.WeatherHistoryTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    var requestName by remember { mutableStateOf("Padova") }
    var results by remember { mutableStateOf(emptyList<Location>()) }
    LaunchedEffect(key1 = requestName){
        launch(Dispatchers.IO){
            results=KTorGeocodingService().getLocations(requestName)
        }
    }

    Column {
        TextField(value = requestName, onValueChange = { requestName = it })
        LazyColumn {
            items(results) {
                Text(text = "${it.name} -> ${it.country}")
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WeatherHistoryTheme {
        Greeting("Android")
    }
}