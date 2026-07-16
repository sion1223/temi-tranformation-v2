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

package com.robotemi.go.feature.navigation


import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.robotemi.go.feature.delivery.model.GoData
import com.robotemi.go.feature.delivery.model.serializableType
import com.robotemi.go.feature.delivery.ui.arrive.ArriveScreen
import com.robotemi.go.feature.delivery.ui.idle.IdleScreen
import com.robotemi.go.feature.delivery.ui.going.GoingScreen
import com.robotemi.go.feature.delivery.ui.settings.SettingsScreen
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

@Composable
fun MainNavigation() {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = IdleScreen) {
        composable<IdleScreen> {
            BackHandler(true) {}
            IdleScreen(navController = navController)
        }

        composable<GoingScreen>(typeMap = GoingScreen.typeMap) {
            BackHandler(true) {}
            GoingScreen(navController = navController)
        }

        composable<ArriveScreen>(typeMap = ArriveScreen.typeMap) {
            BackHandler(true) {}
            ArriveScreen(navController = navController)
        }

        composable<SettingsScreen> { SettingsScreen(navController = navController) }
    }
}

@Serializable
object IdleScreen

@Serializable
data class GoingScreen(val goData: GoData) {
    companion object {
        val typeMap = mapOf(typeOf<GoData>() to serializableType<GoData>())

        fun from(savedStateHandle: SavedStateHandle) =
            savedStateHandle.toRoute<GoingScreen>(typeMap)
    }
}

@Serializable
data class ArriveScreen(val goData: GoData) {
    companion object {
        val typeMap = mapOf(typeOf<GoData>() to serializableType<GoData>())

        fun from(savedStateHandle: SavedStateHandle) =
            savedStateHandle.toRoute<ArriveScreen>(typeMap)
    }
}

@Serializable
object SettingsScreen