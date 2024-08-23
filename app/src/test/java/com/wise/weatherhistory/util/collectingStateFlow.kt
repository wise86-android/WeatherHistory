package com.wise.weatherhistory.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> TestScope.collectingStateFlow(stateFlow: StateFlow<T>, test:TestScope.(stateFlow:StateFlow<T>) -> Unit){
    val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        stateFlow.collect()
    }
    try {
        test(stateFlow)
    }finally {
        collectJob.cancel()
    }
}