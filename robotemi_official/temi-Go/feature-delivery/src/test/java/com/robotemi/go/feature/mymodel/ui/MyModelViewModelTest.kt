/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.robotemi.go.feature.mymodel.ui


import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.robotemi.go.core.data.LocationRepository

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@OptIn(ExperimentalCoroutinesApi::class) // TODO: Remove when stable
class MyModelViewModelTest {
//    @Test
//    fun uiState_initiallyLoading() = runTest {
//        val viewModel = IdleViewModel(FakeLocationRepository())
//        assertEquals(viewModel.uiState.first(), IdleScreenUiState)
//    }

//    @Test
//    fun uiState_onItemSaved_isDisplayed() = runTest {
//        val viewModel = IdleViewModel(FakeLocationRepository())
//        assertEquals(viewModel.uiState.first(), IdleScreenUiState)
//    }
}

private class FakeLocationRepository : LocationRepository {

    private val data = mutableListOf<String>()

    override val locations: Flow<List<String>>
        get() = flow { emit(data.toList()) }

    override suspend fun add(name: String) {
        data.add(0, name)
    }
}
