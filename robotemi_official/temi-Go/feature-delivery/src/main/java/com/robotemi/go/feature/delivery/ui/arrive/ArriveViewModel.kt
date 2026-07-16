package com.robotemi.go.feature.delivery.ui.arrive

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robotemi.go.core.preferences.domain.UserPreferencesRepository
import com.robotemi.go.feature.delivery.model.GoData
import com.robotemi.go.feature.navigation.ArriveScreen
import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
import com.robotemi.sdk.listeners.OnSerialRawDataListener
import com.robotemi.sdk.serial.Serial
import com.robotemi.sdk.serial.Serial.cmd
import com.robotemi.sdk.serial.Serial.dataFrame
import com.robotemi.sdk.serial.Serial.dataHex
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArriveViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), OnSerialRawDataListener {
    private val robot = Robot.getInstance()

    private val locationList: ArrayList<String> =
        ArriveScreen.from(savedStateHandle).goData.locationList
    private val trayList: ArrayList<Int> = ArriveScreen.from(savedStateHandle).goData.trayList

    var goData = GoData(locationList, trayList)

    private val _uiState = MutableStateFlow(ArriveScreenUiState())
    val uiState = _uiState.asStateFlow()

    private val _back: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val back = _back
    private val _goToGoing = MutableStateFlow(false)
    val goToGoing = _goToGoing

    private var nextLocationSpeech = ""
    private var wrongTraySpeech = ""
    private var idleLocation = ""
    private var password = ""

    private lateinit var countDownTimer: CountDownTimer

    init {
        viewModelScope.launch {
            userPreferencesRepository.getNextLocationSpeechFlow().collectLatest {
                nextLocationSpeech = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getWrongTraySpeechFlow().collectLatest {
                wrongTraySpeech = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getIdleLocationFlow().collectLatest {
                idleLocation = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getArriveTimeoutFlow().collectLatest {
                countDownTimer = object : CountDownTimer(it * 1000L, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        _uiState.value = _uiState.value.copy(remainingTime = millisUntilFinished)
                    }

                    override fun onFinish() {
                        nextAction()
                    }
                }.start()
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getPasswordFlow().collectLatest {
                password = it
            }
        }
        robot.addOnSerialRawDataListener(this)
        setLocation(locationList.first())
        setTray(trayList.first())
    }

    private fun setLocation(location: String) {
        _uiState.update { it.copy(location = location) }
    }

    private fun setTray(tray: Int) {
        _uiState.update { it.copy(tray = tray) }
    }

    fun nextAction() {
        robot.sendSerialCommand(
            Serial.CMD_TRAY_LIGHT,
            byteArrayOf(trayList.first().toByte(), 0X00, 0X00, 0X00)
        )
        locationList.removeAt(0)
        trayList.removeAt(0)
        if (locationList.isNotEmpty() && trayList.isNotEmpty()) {
            speakTTS(nextLocationSpeech)
            goData = GoData(locationList, trayList)
        } else {
            locationList.add(idleLocation)
            trayList.add(0)
            goData = GoData(locationList, trayList)
        }
        _goToGoing.value = true
    }

    fun reset() {
        _goToGoing.value = false
        stopTimer()
        robot.removeOnSerialRawDataListener(this)
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

    fun stopTimer() {
        countDownTimer.cancel()
    }

    fun pause() {
        countDownTimer.cancel()
        setPasswordPopUp(true)
    }

    fun resume() {
        countDownTimer.start()
        setPasswordPopUp(false)
        setOptionPopUp(false)
    }

    fun setPasswordInput(passwordInput: String) {
        _uiState.update { it.copy(passwordInput = passwordInput) }
    }

    fun checkPassword(input: String) {
        if (input == password) {
            setOptionPopUp(true)
        } else {
            countDownTimer.start()
        }
        setPasswordPopUp(false)
    }

    private fun setOptionPopUp(isShowing: Boolean) {
        _uiState.update { it.copy(optionsPopUp = isShowing) }
    }

    private fun setPasswordPopUp(isShowing: Boolean) {
        setPasswordInput("")
        _uiState.update { it.copy(passwordPopUp = isShowing) }
    }

    fun selectOption(option: Int) {
        setOptionPopUp(false)
        when (option) {
            1 -> {
                _back.value = true
            }

            2 -> {
                locationList.clear()
                trayList.clear()
                locationList.add(idleLocation)
                trayList.add(0)
                goData = GoData(locationList, trayList)
                _goToGoing.value = true
            }

            3 -> {
                countDownTimer.start()
            }
        }
    }

    override fun onSerialRawData(data: ByteArray) {
        val cmd = data.cmd
        val dataFrame = data.dataFrame
        Log.d("Serial", "cmd $cmd raw data ${data.dataHex}")

        when (cmd) {
            Serial.RESP_TRAY_SENSOR -> {
                val trayIndex = dataFrame[0].toInt()
                val loaded = dataFrame[1].toInt()
                if (trayIndex == trayList.first() && loaded == 0) {
                    Log.d("ArriveViewModel", "Tray $trayIndex unloaded")
                    countDownTimer.cancel()
                    viewModelScope.launch {
                        delay(5000L)
                        nextAction()
                    }
                } else if (trayIndex != trayList.first() && loaded == 0) {
                    _uiState.update { it.copy(showWrongTrayPopUp = true) }
                    speakTTS(wrongTraySpeech)
                    viewModelScope.launch {
                        delay(5000L)
                        _uiState.update { it.copy(showWrongTrayPopUp = false) }
                    }
                }
            }
        }
    }
}

data class ArriveScreenUiState(
    val location: String = "",
    val remainingTime: Long = 0,
    val tray: Int = 0,
    val showWrongTrayPopUp: Boolean = false,
    val passwordPopUp: Boolean = false,
    val passwordInput: String = "",
    val optionsPopUp: Boolean = false
)