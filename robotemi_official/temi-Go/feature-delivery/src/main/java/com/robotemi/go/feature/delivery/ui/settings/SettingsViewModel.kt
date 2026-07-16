package com.robotemi.go.feature.delivery.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robotemi.go.core.preferences.domain.UserPreferencesRepository
import com.robotemi.sdk.Robot
import com.robotemi.sdk.serial.Serial
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val robot = Robot.getInstance()

    private val _uiState = MutableStateFlow(SettingsScreenUiState())
    val uiState = _uiState

    init {
        viewModelScope.launch {
            userPreferencesRepository.getPasswordFlow().collectLatest { password ->
                _uiState.update { it.copy(password = password) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getIdleLocationFlow().collectLatest { idleLocation ->
                _uiState.update { it.copy(idleLocation = idleLocation) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getArriveTimeoutFlow().collectLatest { arriveTimeout ->
                _uiState.update { it.copy(arriveTimeout = arriveTimeout) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getPauseTimeoutFlow().collectLatest { pauseTimeout ->
                _uiState.update { it.copy(pauseTimeout = pauseTimeout) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getStartSpeechFlow().collectLatest { startSpeech ->
                _uiState.update { it.copy(startSpeech = startSpeech) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getObstacleSpeechFlow().collectLatest { obstacleSpeech ->
                _uiState.update { it.copy(obstacleSpeech = obstacleSpeech) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getArriveSpeechFlow().collectLatest { arriveSpeech ->
                _uiState.update { it.copy(arriveSpeech = arriveSpeech) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getNextLocationSpeechFlow().collectLatest { nextLocationSpeech ->
                _uiState.update { it.copy(nextLocationSpeech = nextLocationSpeech) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getWrongTraySpeechFlow().collectLatest { wrongTraySpeech ->
                _uiState.update { it.copy(wrongTraySpeech = wrongTraySpeech) }
            }
        }
//        viewModelScope.launch {
//            userPreferencesRepository.getDefaultLcdTextFlow().collectLatest { defaultLcdText ->
//                _uiState.update { it.copy(defaultLcdText = defaultLcdText) }
//            }
//        }
    }

    fun setPassword(password: String) {
        viewModelScope.launch {
            userPreferencesRepository.setPassword(password)
        }
    }

    fun setIdleLocation(idleLocation: String) {
        viewModelScope.launch {
            userPreferencesRepository.setIdleLocation(idleLocation)
        }
    }

    fun setArriveTimeout(arriveTimeout: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setArriveTimeout(arriveTimeout)
        }
    }

    fun setPauseTimeout(pauseTimeout: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setPauseTimeout(pauseTimeout)
        }
    }

    fun setStartSpeech(startSpeech: String) {
        viewModelScope.launch {
            userPreferencesRepository.setStartSpeech(startSpeech)
        }
    }

    fun setObstacleSpeech(obstacleSpeech: String) {
        viewModelScope.launch {
            userPreferencesRepository.setObstacleSpeech(obstacleSpeech)
        }
    }

    fun setArriveSpeech(arriveSpeech: String) {
        viewModelScope.launch {
            userPreferencesRepository.setArriveSpeech(arriveSpeech)
        }
    }

    fun setNextLocationSpeech(nextLocationSpeech: String) {
        viewModelScope.launch {
            userPreferencesRepository.setNextLocationSpeech(nextLocationSpeech)
        }
    }

    fun setWrongTraySpeech(wrongTraySpeech: String) {
        viewModelScope.launch {
            userPreferencesRepository.setWrongTraySpeech(wrongTraySpeech)
        }
    }

    fun setShowDialog(key: String, show: Boolean) {
        _uiState.update { it.copy(isShowingDialog = show, currentDialog = key) }
    }

    fun getLocations() = robot.locations.filter { it != "home base" }.sorted()

    fun setInput(string: String) {
        _uiState.update { it.copy(input = string) }
    }

    fun tareTrays() {
        robot.sendSerialCommand(Serial.CMD_TRAY_CALIBRATE, byteArrayOf())
    }

    fun goToHomeBase(){
        robot.goTo("home base")
    }
}

data class SettingsScreenUiState(
    var password: String = "",
    var idleLocation: String = "",
    var arriveTimeout: Int = 0,
    var pauseTimeout: Int = 0,
    var startSpeech: String = "",
    var obstacleSpeech: String = "",
    var arriveSpeech: String = "",
    var nextLocationSpeech: String = "",
    var wrongTraySpeech: String = "",
    var defaultLcdText: String = "",
    var isShowingDialog: Boolean = false,
    var currentDialog: String = "",
    var input: String = "",
)