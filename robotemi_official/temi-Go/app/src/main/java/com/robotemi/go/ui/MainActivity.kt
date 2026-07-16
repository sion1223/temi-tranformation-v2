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

package com.robotemi.go.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import com.robotemi.go.core.ui.MyApplicationTheme
import com.robotemi.go.feature.navigation.MainNavigation
import com.robotemi.sdk.Robot
import com.robotemi.sdk.listeners.OnRobotReadyListener
import com.robotemi.sdk.permission.Permission

@AndroidEntryPoint
class MainActivity : ComponentActivity(), OnRobotReadyListener {
    private val robot = Robot.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                0)
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }
    }

    override fun onRobotReady(isReady: Boolean) {
        if (isReady) {
            requestRobotPermissions()
            robot.toggleNavigationBillboard(true)
        }
    }
    private fun requestRobotPermissions() {
        val permissionsToRequest = listOf(
            Permission.MAP,
            Permission.SETTINGS
        ).filter { robot.checkSelfPermission(it) != Permission.GRANTED }
        if (permissionsToRequest.isNotEmpty()) {
            robot.requestPermissions(permissionsToRequest, 1111)
        }
    }

    override fun onStart() {
        super.onStart()
        robot.addOnRobotReadyListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        robot.removeOnRobotReadyListener(this)
        robot.toggleNavigationBillboard(false)
    }
}
