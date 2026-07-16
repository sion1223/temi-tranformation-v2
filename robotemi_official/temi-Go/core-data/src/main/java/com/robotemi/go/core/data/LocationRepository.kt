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

package com.robotemi.go.core.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import com.robotemi.go.core.database.MyModel
import com.robotemi.go.core.database.MyModelDao
import com.robotemi.sdk.Robot
import com.robotemi.sdk.listeners.OnLocationsUpdatedListener
import com.robotemi.sdk.listeners.OnRobotReadyListener
import com.robotemi.sdk.permission.OnRequestPermissionResultListener
import com.robotemi.sdk.permission.Permission
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

interface LocationRepository {
    val locations: Flow<List<String>>

    suspend fun add(name: String)
}

class RealLocationRepository @Inject constructor(
    private val myModelDao: MyModelDao
) : LocationRepository, OnLocationsUpdatedListener, OnRobotReadyListener,
    OnRequestPermissionResultListener {

    private val robot = Robot.getInstance()

    private val flow = MutableStateFlow<List<String>>(listOf())

    init {
        robot.addOnRobotReadyListener(this)
        robot.addOnLocationsUpdatedListener(this)
        robot.addOnRequestPermissionResultListener(this)
    }

    override val locations: Flow<List<String>> = flow

    override suspend fun add(name: String) {
        myModelDao.insertMyModel(MyModel(name = name))
    }

    override fun onLocationsUpdated(locations: List<String>) {
        Log.d("LOCATIONS", "locations $locations")
        flow.value = locations
    }

    override fun onRobotReady(isReady: Boolean) {
        if (isReady) {
            val locations = Robot.getInstance().locations.filter { it != "home base" }.sorted()
            Log.d("LOCATIONS", "initial locations $locations")
            flow.value = locations

            if (robot.isSelectedKioskApp()) {
                checkPermission()
            }
        }
    }

    private fun checkPermission() {
        if (robot.checkSelfPermission(Permission.SETTINGS) == Permission.GRANTED) {
            robot.toggleNavigationBillboard(true)
        } else {
            robot.requestPermissions(listOf(Permission.SETTINGS), requestCode = 0)
        }
    }

    override fun onRequestPermissionResult(
        permission: Permission,
        grantResult: Int,
        requestCode: Int
    ) {
        Log.d("PERMISSION", "permission $permission, grantResult $grantResult")
        if (permission == Permission.SETTINGS && grantResult == Permission.GRANTED) {
            robot.toggleNavigationBillboard(true)
        }
    }
}
