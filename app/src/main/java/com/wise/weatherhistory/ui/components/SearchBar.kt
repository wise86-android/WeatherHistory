package com.wise.weatherhistory.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wise.weatherhistory.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(viewModel: MainViewModel){

    val searchText by viewModel.searchText.collectAsState("")
    val isSearching by viewModel.isSearching.collectAsState()
    val locations by viewModel.locationList.collectAsState()

    SearchBar(
        query = searchText,//text showed on SearchBar
        onQueryChange = viewModel::onSearchTextChange, //update the value of searchText
        onSearch = viewModel::takeFirstResult, //the callback to be invoked when the input service triggers the ImeAction.Search action
        active = isSearching, //whether the user is searching or not
        onActiveChange = { viewModel.onToogleSearch() }, //the callback to be invoked when this search bar's active state is changed
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        LazyColumn {
            items(locations){ location->
                LocationResultListItem(location = location, onSelect = viewModel::onSelectLocation)
            }

        }
    }
}

