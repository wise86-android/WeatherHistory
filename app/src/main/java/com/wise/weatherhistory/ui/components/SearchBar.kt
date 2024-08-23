package com.wise.weatherhistory.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wise.weatherhistory.model.Location
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun SearchBar(modifier: Modifier = Modifier,
              isSearching:Boolean = false,
              onSearchStateChange: (Boolean)->Unit,
              queryText:String = "",
              onQueryChange: (String) -> Unit,
              onSearch: (String) -> Unit,
              foundLocation: List<Location>,
              onLocationSelected:(Location)->Unit,
              ){
    SearchBar(
        query = queryText,//text showed on SearchBar
        onQueryChange = onQueryChange, //update the value of searchText
        onSearch = onSearch, //the callback to be invoked when the input service triggers the ImeAction.Search action
        active = isSearching, //whether the user is searching or not
        onActiveChange = onSearchStateChange, //the callback to be invoked when this search bar's active state is changed
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        LazyColumn {
            items(foundLocation){ location->
                LocationResultListItem(location = location, onSelect = onLocationSelected)
            }

        }
    }
}

