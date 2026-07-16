package com.robotemi.go.feature.delivery.ui.going

import android.media.MediaPlayer
import android.os.CountDownTimer
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robotemi.go.core.preferences.domain.UserPreferencesRepository
import com.robotemi.go.feature.delivery.di.ResourcesProvider
import com.robotemi.go.feature.delivery.model.GoData
import com.robotemi.go.feature.navigation.GoingScreen
import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener
import com.robotemi.sdk.listeners.OnSerialRawDataListener
import com.robotemi.sdk.serial.Serial
import com.robotemi.sdk.serial.Serial.cmd
import com.robotemi.sdk.serial.Serial.dataFrame
import com.robotemi.sdk.serial.Serial.dataHex
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoingViewModel @Inject constructor(
    resourcesProvider: ResourcesProvider,
    private val userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(),
    OnGoToLocationStatusChangedListener, OnSerialRawDataListener {
    private val robot = Robot.getInstance()

    private val locationList: ArrayList<String> =
        GoingScreen.from(savedStateHandle).goData.locationList
    private val trayList: ArrayList<Int> = GoingScreen.from(savedStateHandle).goData.trayList

    private var mediaPlayer: MediaPlayer? = null

    val goData = GoData(locationList, trayList)


    private val _uiState = MutableStateFlow(GoingScreenUiState())
    val uiState = _uiState.asStateFlow()

    private val _back: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val back = _back.asStateFlow()

    private val _goToArrive = MutableStateFlow(false)
    val goToArrive = _goToArrive.asStateFlow()

    private var password: String = ""
    private var obstacleSpeech = ""
    private var arriveSpeech = ""
    private var idleLocation = ""

    private var goToLocation = locationList.first()

    private lateinit var countDownTimer: CountDownTimer

    init {
        robot.addOnGoToLocationStatusChangedListener(this)
        robot.addOnSerialRawDataListener(this)
        _uiState.update { it.copy(goToLocation = goToLocation) }
        viewModelScope.launch {
            userPreferencesRepository.getPauseTimeoutFlow().collectLatest {
                countDownTimer = object : CountDownTimer(it * 1000L, 1000) {
                    override fun onTick(millisUntilFinished: Long) {}

                    override fun onFinish() {
                        resume()
                    }
                }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getPasswordFlow().collectLatest {
                password = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getObstacleSpeechFlow().collectLatest {
                obstacleSpeech = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getArriveSpeechFlow().collectLatest {
                arriveSpeech = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getIdleLocationFlow().collectLatest {
                idleLocation = it
                Log.d("GoingViewModel", "Idle Location: $idleLocation")
            }
        }
        robot.goTo(goToLocation)
        val music = "file:///storage/emulated/0/Music/temigo.mp3".toUri()
        try {
            mediaPlayer = MediaPlayer.create(resourcesProvider.getContext(), music).apply {
                isLooping = true
                start()
            }
        } catch (e: Exception) {
            mediaPlayer = null
            Log.d("GoingViewModel", "No Music")
        }

    }


    override fun onGoToLocationStatusChanged(
        location: String,
        status: String,
        descriptionId: Int,
        description: String
    ) {
        if (status == OnGoToLocationStatusChangedListener.COMPLETE && location == idleLocation) {
            mediaPlayer?.stop()
            _back.value = true
        }

        if (status == OnGoToLocationStatusChangedListener.START) {
            robot.sendSerialCommand(Serial.CMD_LCD_TEXT, Serial.getLcdBytes(location))
        }

        if (status == OnGoToLocationStatusChangedListener.COMPLETE && location != idleLocation) {
            mediaPlayer?.stop()
            speakTTS(arriveSpeech)
            _goToArrive.value = true
        }

        if (status == OnGoToLocationStatusChangedListener.ABORT && descriptionId == 1005) {
            pause()
            countDownTimer.start()
        }

        if (descriptionId == 2000 || descriptionId == 2001) {
            speakTTS(obstacleSpeech)
        }
    }

    fun pause() {
        robot.stopMovement()
        mediaPlayer?.pause()
        setPasswordPopUp(true)
    }

    fun resume() {
        robot.goTo(goToLocation)
        countDownTimer.cancel()
        mediaPlayer?.start()
        setPasswordPopUp(false)
        setOptionPopUp(false)
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

    fun setPasswordInput(passwordInput: String) {
        _uiState.update { it.copy(passwordInput = passwordInput) }
    }

    fun checkPassword(input: String) {
        if (input == password) {
            countDownTimer.cancel()
            setOptionPopUp(true)
        } else {
            robot.goTo(goToLocation)
        }
        setPasswordPopUp(false)
    }

    private fun setPasswordPopUp(isShowing: Boolean) {
        setPasswordInput("")
        _uiState.update { it.copy(passwordPopUp = isShowing) }
    }

    fun reset() {
        _goToArrive.value = false
        countDownTimer.cancel()
        mediaPlayer?.release()
        robot.removeOnGoToLocationStatusChangedListener(this)
        robot.removeOnSerialRawDataListener(this)
    }

    private fun setOptionPopUp(isShowing: Boolean) {
        _uiState.update { it.copy(optionsPopUp = isShowing) }
    }

    fun selectOption(option: Int) {
        setOptionPopUp(false)
        when (option) {
            1 -> {
                _back.value = true
            }

            2 -> {
                goToLocation = idleLocation
                _uiState.update { it.copy(goToLocation = idleLocation) }
                robot.goTo(idleLocation)
            }

            3 -> {
                robot.goTo(goToLocation)
                mediaPlayer?.start()
            }
        }
    }

    override fun onSerialRawData(data: ByteArray) {
        val cmd = data.cmd

        // Data frame of response
        val dataFrame = data.dataFrame

        // To see the hex array of raw data
        Log.d("Serial", "cmd $cmd raw data ${data.dataHex}")

        when (cmd) {
            Serial.RESP_TRAY_BACK_BUTTON -> {

                // Just in case it doesn't exist
                val event = dataFrame.firstOrNull() ?: return

                when (event.toInt()) {
                    0 -> pause()
                    1 -> pause()
                }
            }
        }
    }
}

data class GoingScreenUiState(
    val goToLocation: String = "",
    val passwordPopUp: Boolean = false,
    val passwordInput: String = "",
    val optionsPopUp: Boolean = false
)