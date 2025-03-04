package com.wise.weatherhistory.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wise.weatherhistory.model.GeocodingService
import com.wise.weatherhistory.model.Location
import com.wise.weatherhistory.model.WeatherHistoryService
import com.wise.weatherhistory.model.WeatherQuerySettingsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@FlowPreview
@HiltViewModel
class SearchViewModel @Inject constructor(private val geocodingService: GeocodingService,
                                          private val historyService: WeatherHistoryService,
                                          private val querySettings: WeatherQuerySettingsService) : ViewModel() {



    //first state whether the search is happening or not
    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    //second state the text typed by the user
    private val _searchText = MutableStateFlow("")
    val searchText = isSearching.flatMapLatest {
        searching -> if(searching) { _searchText } else{ selectedLocation.map { it.name } }}

    //third state the list to be filtered
    val locationList =
        _searchText
            .debounce(500)
            .mapLatest { queryText-> geocodingService.getLocations(queryText,
                GeocodingService.RequestParameter(4)
            ) }
            .stateIn(viewModelScope,
                SharingStarted.WhileSubscribed(300),
                emptyList()
            )

    private val selectedLocation= querySettings.getLastLocation()

    val daysToLoad = querySettings.getLastTimeRange()
        .map { it.toDays() }
        .stateIn(viewModelScope,SharingStarted.WhileSubscribed(300),0)

    private val timeRangeToLoad = daysToLoad
        .map { val today = LocalDate.now(); today.minusDays(it).. today }

    val meteoData = selectedLocation
        .combine(timeRangeToLoad,historyService::getWeatherData)
        .stateIn(viewModelScope,
            SharingStarted.WhileSubscribed(300),
            emptyList(),
        )

    fun onSearchTextChange(text: String) {
        _isSearching.update { true }
        _searchText.update { text }
    }

    fun takeFirstResult(text: String) {
        onSearchTextChange(text)
        viewModelScope.launch {
            locationList.collectLatest { it.firstOrNull()?.let(::onSelectLocation) }
        }
    }

    fun onSearchStateChange(newSearchState: Boolean) {
        _isSearching.value = newSearchState
        if (!newSearchState) {
            onSearchTextChange("")
        }
    }

    fun onSelectLocation(location: Location){
        _isSearching.value = !_isSearching.value
        viewModelScope.launch {
            querySettings.setLastLocation(location)
        }

    }

}