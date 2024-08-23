package com.wise.weatherhistory.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.wise.weatherhistory.ui.components.SearchBar
import com.wise.weatherhistory.ui.components.TemperaturePlot
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
@Composable
fun SearchPage(openSettings: () -> Unit,modifier: Modifier = Modifier,viewModel: SearchViewModel = hiltViewModel()){

    val meteoData by viewModel.meteoData.collectAsState(initial = emptyList())
    val locations by viewModel.locationList.collectAsState()
    val searchText by viewModel.searchText.collectAsState(initial = "")
    val isSearching by viewModel.isSearching.collectAsState(initial = false)
    val dayToLoad  by viewModel.daysToLoad.collectAsState()

    Column(modifier = modifier) {
        SearchBar(
            isSearching = isSearching,
            onSearchStateChange = viewModel::onSearchStateChange,
            queryText = searchText,
            onQueryChange = viewModel::onSearchTextChange,
            foundLocation = locations,
            onSearch = viewModel::takeFirstResult,
            onLocationSelected = viewModel::onSelectLocation
        )
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()) {
            Text(text = "Weather in the last $dayToLoad days",modifier=Modifier)
            TextButton(onClick = openSettings) {
                Text(text = "Change")
            }
        }

        if(meteoData.isNotEmpty()) {
            TemperaturePlot(data = meteoData)
        }
    }
}