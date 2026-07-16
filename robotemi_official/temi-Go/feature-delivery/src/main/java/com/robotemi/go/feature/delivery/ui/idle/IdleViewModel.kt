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

package com.robotemi.go.feature.delivery.ui.idle

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.robotemi.go.core.data.LocationRepository
import com.robotemi.go.core.preferences.domain.UserPreferencesRepository
import com.robotemi.go.feature.delivery.model.GoData
import com.robotemi.go.feature.delivery.model.Tray
import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
import com.robotemi.sdk.serial.Serial
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class IdleViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val robot = Robot.getInstance()

    private var goToLocationList: ArrayList<String> = arrayListOf()

    private var trayList: ArrayList<Int> = arrayListOf()

    val goData = GoData(goToLocationList, trayList)

    private val _uiState = MutableStateFlow(IdleScreenUiState())
    val uiState = _uiState.asStateFlow()

    private val _goToSettings: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val goToSettings = _goToSettings.asStateFlow()

    private var password: String = ""
    private var startSpeech = ""
//    private var lcdText = ""

    init {
        viewModelScope.launch {
            userPreferencesRepository.getPasswordFlow().collectLatest {
                password = it
                Log.d("IdleViewModel", "Password: $it")
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getStartSpeechFlow().collectLatest {
                startSpeech = it
            }
        }
//        viewModelScope.launch {
//            userPreferencesRepository.getDefaultLcdTextFlow().collectLatest {
//                lcdText = it
//                robot.sendSerialCommand(
//                    Serial.CMD_LCD_TEXT,
//                    Serial.getLcdBytes(it)
//                )
//            }
//        }
    }

     fun initLocation(){
        viewModelScope.launch {
            locationRepository.locations.collectLatest { list ->
                _uiState.update { currentState ->
                    currentState.copy(locations = list)
                }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getIdleLocationFlow().collectLatest { idleLocation ->
                _uiState.update { currentState ->
                    currentState.copy(locations = currentState.locations.filter { it != idleLocation })
                }
            }
        }
    }

    fun setTrayLocation(location: String) {
        _uiState.update { currentState ->
            if (currentState.currentSelectedTray != null) {
                val newTray = currentState.tray.toMutableMap()
                if (goToLocationList.contains(location)) {
                    goToLocationList.add(goToLocationList.indexOf(location) + 1, location)
                    trayList.add(
                        goToLocationList.indexOf(location) + 1,
                        currentState.currentSelectedTray!!.trayNumber
                    )
                } else {
                    goToLocationList.add(location)
                    trayList.add(currentState.currentSelectedTray!!.trayNumber)
                }
                Log.d("IdleViewModel", "goToLocationList: $goToLocationList, trayList: $trayList")
                newTray[currentState.currentSelectedTray!!] = location
                toggleLED(currentState.currentSelectedTray!!, true)
                currentState.copy(tray = newTray, currentSelectedTray = null)
            } else {
                currentState
            }
        }
    }

    fun removeTrayLocation(tray: Tray) {
        _uiState.update { currentState ->
            val newTray = currentState.tray.toMutableMap()
            if (goToLocationList.count { it == currentState.tray[tray] } > 1) {
                val index = goToLocationList.indexOf(currentState.tray[tray]) + 1
                Log.d("IdleViewModel", "Index: $index")
                goToLocationList.removeAt(index)
            } else {
                goToLocationList.remove(currentState.tray[tray])
            }
            trayList.remove(tray.trayNumber)
            newTray.remove(tray)
            toggleLED(tray, false)
            currentState.copy(tray = newTray)
        }
        Log.d("IdleViewModel", "goToLocationList: $goToLocationList, trayList: $trayList")
    }

    fun setCurrentSelectedTray(tray: Tray?) {
        _uiState.update { currentState ->
            currentState.copy(currentSelectedTray = if (currentState.currentSelectedTray == tray) null else tray)
        }
    }

    private fun toggleLED(tray: Tray, isOn: Boolean) {
        if (isOn) {
            robot.sendSerialCommand(
                Serial.CMD_TRAY_LIGHT,
                byteArrayOf(tray.trayNumber.toByte(), 0X20.toByte(), 0XD1.toByte(), 0X99.toByte())
            )
        } else {
            robot.sendSerialCommand(
                Serial.CMD_TRAY_LIGHT,
                byteArrayOf(tray.trayNumber.toByte(), 0X00, 0X00, 0X00)
            )
        }
    }

//    private fun lcdScreenInit() {
//        robot.sendSerialCommand(
//            Serial.CMD_LCD_TEXT,
//            getLcdColorBytes(
//                byteArrayOf(0x00, 0x00.toByte(), 0x00.toByte(), 0x00.toByte()),
//                target = Serial.LCD.TEXT_0_COLOR
//            )
//        )
//
//        robot.sendSerialCommand(
//            Serial.CMD_LCD_TEXT,
//            getLcdColorBytes(
//                byteArrayOf(0x00, 0X20.toByte(), 0XD1.toByte(), 0X99.toByte()),
//                target = Serial.LCD.TEXT_0_BACKGROUND
//            )
//        )
//    }

    fun initLED() {
        toggleLED(Tray.TOP, false)
        toggleLED(Tray.MIDDLE, false)
        toggleLED(Tray.BOTTOM, false)
    }

    fun setLcdText() {
        robot.sendSerialCommand(
            Serial.CMD_LCD_TEXT,
            Serial.getLcdBytes("")
        )
    }

    fun reset() {
        _goToSettings.value = false
        _uiState.update {
            it.copy(
                tray = mutableMapOf(),
                currentSelectedTray = null,
                passwordInput = "",
                passwordPopUp = false
            )
        }
        goToLocationList.clear()
        trayList.clear()
    }

    fun setPasswordInput(passwordInput: String) {
        _uiState.update { it.copy(passwordInput = passwordInput) }
    }

    fun checkPassword(input: String) {
        if (input == password) {
            _goToSettings.value = true
        }
        setPasswordPopUp(false)
        setPasswordInput("")
    }

    fun setPasswordPopUp(isShowing: Boolean) {
        _uiState.update { it.copy(passwordPopUp = isShowing) }
    }

    fun startSpeech() {
        speakTTS(startSpeech)
    }

    private fun speakTTS(msg: String) {
        robot.speak(
            TtsRequest.create(
                msg,
                language = TtsRequest.Language.SYSTEM,
                isShowOnConversationLayer = false,
                cached = true
            )
        )
    }
}

data class IdleScreenUiState(
    var locations: List<String> = emptyList(),
    var tray: MutableMap<Tray, String?> = mutableMapOf(),
    var currentSelectedTray: Tray? = null,
    val passwordPopUp: Boolean = false,
    val passwordInput: String = "",
    val optionPopUp: Boolean = false
)

