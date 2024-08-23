package com.wise.weatherhistory.ui

import com.wise.weatherhistory.model.WeatherQuerySettingsService
import com.wise.weatherhistory.util.MainDispatcherRule
import com.wise.weatherhistory.util.collectingStateFlow
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.*
import org.junit.Test
import java.time.Duration
import kotlinx.coroutines.test.runTest
import org.junit.Rule

class SettingsViewModelTest{

    @get:Rule
    val mainRule = MainDispatcherRule()
    @Test
    fun defaultValueIs7(){
        val storeService = mockk<WeatherQuerySettingsService>()
        every { storeService.getLastTimeRange() } returns emptyFlow()

        assertEquals(SettingsViewModel(storeService).pastDays.value,7)
    }

    @Test
    fun pastDaysIsUpdatedWithNewValueFromTheFlow() = runTest {
        val storeService = mockk<WeatherQuerySettingsService>()
        val timeRange = flowOf(Duration.ofDays(10))
        every { storeService.getLastTimeRange() } returns timeRange

        val viewModel = SettingsViewModel(storeService)
        collectingStateFlow(viewModel.pastDays){
            assertEquals(10,it.value)
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun correctlyStoreTheValue() = runTest{
        val storeService = mockk<WeatherQuerySettingsService>(relaxed = true)
        every { storeService.getLastTimeRange() } returns emptyFlow()
        val viewModel = SettingsViewModel(storeService)

        viewModel.setPastDay(10)
        advanceUntilIdle()

        coVerify {
            storeService.setDefaultTimeRange(Duration.ofDays(10))
        }
    }

}