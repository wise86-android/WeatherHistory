package com.wise.weatherhistory.ui

import app.cash.turbine.test
import com.wise.weatherhistory.model.GeocodingService
import com.wise.weatherhistory.model.Location
import com.wise.weatherhistory.model.WeatherHistoryService
import com.wise.weatherhistory.model.WeatherQuerySettingsService
import com.wise.weatherhistory.ui.previewUtils.randomListWeatherData
import com.wise.weatherhistory.util.MainDispatcherRule
import com.wise.weatherhistory.util.collectingStateFlow
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.Duration


@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val geocodingService =mockk<GeocodingService>(relaxed = true)
    private val weatherHistoryService = mockk<WeatherHistoryService>(relaxed = true)
    private val settingsService = mockk<WeatherQuerySettingsService>(relaxed = true)

    @Before
    fun setUp() {
        clearMocks(geocodingService,weatherHistoryService,settingsService)
    }

    @Test
    fun whenSearchingStateIsSetToTrue() = runTest{
        val vm = SearchViewModel(geocodingService,weatherHistoryService,settingsService)
        collectingStateFlow(vm.isSearching){
            Assert.assertEquals(false, it.value)
            vm.onSearchStateChange(true)
            Assert.assertEquals(true, it.value)
        }
    }

    @Test
    fun whenSearchingStateIsSetToFalseResetSearchText() = runTest{
        val vm = SearchViewModel(geocodingService,weatherHistoryService,settingsService)
        vm.onSearchStateChange(true)

        collectingStateFlow(vm.isSearching){
            vm.onSearchStateChange(false)
            vm.searchText.test {
                Assert.assertEquals("",awaitItem())
            }
        }
    }

    @Test
    fun whenSearchingIsOngoingSearchTextIsUpdated() = runTest{
        val vm = SearchViewModel(geocodingService,weatherHistoryService,settingsService)
        vm.onSearchStateChange(true)
        vm.onSearchTextChange("text")

        vm.searchText.test {
            Assert.assertEquals("text",awaitItem())
        }
    }

    @Test
    fun whenSearginIsOngoingLocationIsUpdated() = runTest{
        val vm = SearchViewModel(geocodingService,weatherHistoryService,settingsService)
        vm.onSearchStateChange(true)
        val searchLocation = "text"
        collectingStateFlow(vm.locationList) {
            vm.onSearchTextChange(searchLocation)
            val location = Location(10.0f, 20.0f, 1000.0f, "name", "country")
            coEvery { geocodingService.getLocations(searchLocation) } returns listOf(location)
            advanceTimeBy(1000)
            coVerify { geocodingService.getLocations(searchLocation,any()) }
        }
    }

    @Test
    fun whenLocationIsSelectedWheaderDataIsLoaded() = runTest{
        val location = Location(10.0f, 20.0f, 1000.0f, "name", "country")
        val valueRange = Duration.ofDays(4)
        coEvery { settingsService.getLastTimeRange() } returns flowOf(valueRange)
        coEvery { settingsService.getLastLocation() } returns flowOf(location)
        coEvery { weatherHistoryService.getWeatherData(location,any()) } returns randomListWeatherData(10)

        val vm = SearchViewModel(geocodingService,weatherHistoryService,settingsService)

        collectingStateFlow(vm.meteoData) {
            val today = LocalDate.now()
            advanceTimeBy(1000)
            coVerify { weatherHistoryService.getWeatherData(location,today.minusDays(4).. today) }
        }
    }

    @Test
    fun whenLocationIsSelectedLastLocationSettingIsStored() = runTest{
        val vm = SearchViewModel(geocodingService,weatherHistoryService,settingsService)
        vm.onSearchStateChange(true)
        val location = Location(10.0f, 20.0f, 1000.0f, "name", "country")

        vm.onSelectLocation(location)
        coVerify { settingsService.setLastLocation(location) }
    }

    @Test
    fun whenLocationIsSelectedSearchEnds() = runTest{
        val vm = SearchViewModel(geocodingService,weatherHistoryService,settingsService)
        vm.onSearchStateChange(true)
        val location = Location(10.0f, 20.0f, 1000.0f, "name", "country")

        collectingStateFlow(vm.isSearching) {
            vm.onSelectLocation(location)
            Assert.assertEquals(false, it.value)
        }
    }

}