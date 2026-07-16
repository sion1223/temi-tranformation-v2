package com.example.switchgo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rhizo.switchgo.McuUpdateCallback
import com.rhizo.switchgo.SwitchGo
import com.example.switchgo.Constants
import com.example.switchgo.data.model.McuUpdateState
import com.example.switchgo.data.model.SwitchStates
import com.example.switchgo.data.parser.StatusParser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class SwitchGoViewModel(application: Application) : AndroidViewModel(application) {

    private val switchGo: SwitchGo = SwitchGo.getInstance(application)
    private val context = application.applicationContext

    // --- UI State ---

    private val _logMessages = MutableStateFlow<List<String>>(emptyList())
    val logMessages: StateFlow<List<String>> = _logMessages.asStateFlow()

    private val _deviceState = MutableStateFlow<SwitchStates?>(null)
    val deviceState: StateFlow<SwitchStates?> = _deviceState.asStateFlow()

    private val _mcuUpdateState = MutableStateFlow<McuUpdateState>(McuUpdateState.Idle)
    val mcuUpdateState: StateFlow<McuUpdateState> = _mcuUpdateState.asStateFlow()

    private val _isDeviceOpen = MutableStateFlow(false)
    val isDeviceOpen: StateFlow<Boolean> = _isDeviceOpen.asStateFlow()

    private val _isFirmwareReady = MutableStateFlow(false)
    val isFirmwareReady: StateFlow<Boolean> = _isFirmwareReady.asStateFlow()

    // --- Internal ---

    private var doorTestJob: Job? = null
    private val MAX_LOG_SIZE = 50
    private val updateCallback = McuUpdateCallbackImpl()

    // --- Door toggle state ---
    private var gate1 = 2
    private var gate2 = 2
    private var gate3 = 2
    private var gate4 = 2
    private var allGate = 2
    private var turnSignal = 0
    private var interiorLight = false

    init {
        switchGo.registerCallback(updateCallback)
        copyFirmwareAssets()
    }

    private var connectionJob: Job? = null

    // --- USB Lifecycle ---

    fun startAutoConnect() {
        // Avoid duplicate collectors when called multiple times (e.g. each onResume)
        if (connectionJob?.isActive == true) return
        switchGo.startAutoConnect()
        addLog("Auto-connect started — monitoring USB device...")
        connectionJob = viewModelScope.launch {
            switchGo.isConnected.collect { connected ->
                _isDeviceOpen.value = connected
            }
        }
    }

    fun stopAutoConnect() {
        doorTestJob?.cancel()
        connectionJob?.cancel()
        connectionJob = null
        switchGo.stopAutoConnect()
        _isDeviceOpen.value = false
        addLog("Auto-connect stopped")
    }

    // --- Door Control ---

    fun toggleDoor(doorIndex: Int) {
        val gates = when (doorIndex) {
            1 -> { gate1 = if (gate1 == 2) 1 else 2; listOf(gate1, 0, 0, 0) }
            2 -> { gate2 = if (gate2 == 2) 1 else 2; listOf(0, gate2, 0, 0) }
            3 -> { gate3 = if (gate3 == 2) 1 else 2; listOf(0, 0, gate3, 0) }
            4 -> { gate4 = if (gate4 == 2) 1 else 2; listOf(0, 0, 0, gate4) }
            else -> return
        }
        switchGo.controllerAllDoors(gates[0], gates[1], gates[2], gates[3])
        addLog("Door $doorIndex: ${if (gates[doorIndex - 1] == 1) "OPEN" else "CLOSE"}")
    }

    fun toggleAllDoors() {
        allGate = if (allGate == 2) 1 else 2
        switchGo.controllerAllDoors(allGate, allGate, allGate, allGate)
        addLog("All doors: ${if (allGate == 1) "OPEN" else "CLOSE"}")
    }

    fun openAllDoors() {
        gate1 = 1; gate2 = 1; gate3 = 1; gate4 = 1; allGate = 1
        switchGo.controllerAllDoors(1, 1, 1, 1)
        addLog("All doors opened")
    }

    fun closeAllDoors() {
        gate1 = 2; gate2 = 2; gate3 = 2; gate4 = 2; allGate = 2
        switchGo.controllerAllDoors(2, 2, 2, 2)
        addLog("All doors closed")
    }

    // --- Turn Signals ---

    fun toggleLeftSignal() {
        turnSignal = if (turnSignal != 1) 1 else 0
        switchGo.controllerTurnSignal(turnSignal, 0)
        addLog("Left signal: ${if (turnSignal == 1) "ON" else "OFF"}")
    }

    fun toggleRightSignal() {
        turnSignal = if (turnSignal != 2) 2 else 0
        switchGo.controllerTurnSignal(0, if (turnSignal == 2) 1 else 0)
        addLog("Right signal: ${if (turnSignal == 2) "ON" else "OFF"}")
    }

    // --- Ambient Light ---

    fun setAmbientLight(mode: Int, r: Int, g: Int, b: Int) {
        val clampedMode = mode.coerceIn(0, 4)
        switchGo.toggleAmbientLight(clampedMode, r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))
        addLog("Ambient light: mode=$clampedMode R=$r G=$g B=$b")
    }

    fun setAmbientLightPreset(mode: Int) {
        when (mode) {
            0 -> setAmbientLight(0, 0, 0, 0)
            1 -> setAmbientLight(1, 50, 50, 50)
            2 -> setAmbientLight(2, 255, 0, 0)
            3 -> setAmbientLight(3, 0, 255, 0)
            4 -> setAmbientLight(4, 0, 0, 255)
        }
    }

    fun setCustomAmbientLight(modeStr: String, rStr: String, gStr: String, bStr: String) {
        if (modeStr.isEmpty() || rStr.isEmpty() || gStr.isEmpty() || bStr.isEmpty()) {
            addLog("Error: Please enter valid numbers for mode and RGB values")
            return
        }
        try {
            val mode = modeStr.toInt().coerceIn(0, 4)
            val r = rStr.toInt().coerceIn(0, 255)
            val g = gStr.toInt().coerceIn(0, 255)
            val b = bStr.toInt().coerceIn(0, 255)
            setAmbientLight(mode, r, g, b)
        } catch (e: NumberFormatException) {
            addLog("Error: Invalid number format for ambient light parameters")
        }
    }

    // --- Interior Light ---

    fun toggleInteriorLight() {
        interiorLight = !interiorLight
        switchGo.toggleInteriorLight(if (interiorLight) 1 else 0)
        addLog("Interior light: ${if (interiorLight) "ON" else "OFF"}")
    }

    // --- Battery Indicator ---

    fun setBatteryIndicator(level: Int) {
        switchGo.setBatteryIndicator(level.coerceIn(0, 100))
        addLog("Battery indicator set to: $level%")
    }

    // --- MCU Version & Control ---

    fun getMcuVersion(target: Int) {
        viewModelScope.launch {
            try {
                val version = switchGo.getMcuVersion(target.coerceIn(1, 2))
                addLog("MCU${if (target == 1) "1" else "2"} version: $version")
            } catch (e: Exception) {
                addLog("Failed to get MCU version: ${e.message}")
            }
        }
    }

    fun rebootMcu(target: Int) {
        viewModelScope.launch {
            try {
                switchGo.rebootMcu(target.coerceIn(1, 2))
                addLog("MCU${if (target == 1) "1" else "2"} reboot requested")
            } catch (e: Exception) {
                addLog("MCU reboot failed: ${e.message}")
            }
        }
    }

    fun updateMcuFirmware(target: Int) {
        val suffix = if (target == 1) Constants.MCU_FIR_TOP_NAME else Constants.MCU_FIR_BOTTOM_NAME
        val filePath = "${context.filesDir}/$suffix"
        if (!File(filePath).exists()) {
            addLog("Error: Firmware file not found — $filePath")
            return
        }
        addLog("Starting MCU${if (target == 1) "1" else "2"} firmware update...")
        switchGo.updateMcuVersion(target.coerceIn(1, 2), filePath)
    }

    fun recoveryMcu(target: Int) {
        val suffix = if (target == 1) Constants.MCU_FIR_TOP_NAME else Constants.MCU_FIR_BOTTOM_NAME
        val filePath = "${context.filesDir}/$suffix"
        if (!File(filePath).exists()) {
            addLog("Error: Recovery firmware file not found — $filePath")
            return
        }
        addLog("Starting MCU${if (target == 1) "1" else "2"} recovery...")
        switchGo.recoveryMcu(target.coerceIn(1, 2), filePath)
    }

    fun clearDoorBlocks() {
        viewModelScope.launch {
            for (i in 1..4) {
                switchGo.clearDoorBlockAlert(i)
                delay(10)
            }
            addLog("All door block alerts cleared")
        }
    }

    fun getHidDevices(): String {
        val devices = switchGo.getHidDevices()
        addLog("HID Devices: $devices")
        return devices
    }

    // --- Sensor State ---

    fun refreshSwitchStates() {
        viewModelScope.launch {
            try {
                val raw = switchGo.getAllSwitchStates()
                addLog("Raw: $raw")
                StatusParser.parse(raw).fold(
                    onSuccess = { states ->
                        _deviceState.value = states
                        addLog(StatusParser.formatForDisplay(states))
                    },
                    onFailure = { error ->
                        addLog("Parse error: ${error.message}")
                    },
                )
            } catch (e: Exception) {
                addLog("Failed to get switch states: ${e.message}")
            }
        }
    }

    // --- Door Test (automated open/close cycle) ---

    fun startDoorTest() {
        doorTestJob?.cancel()
        doorTestJob = viewModelScope.launch {
            var index = 0
            var openCount = 0
            var closeCount = 0

            while (isActive && index < 200) {
                if (index % 2 == 0) {
                    // Open phase
                    switchGo.controllerAllDoors(1, 1, 1, 1)
                    openCount++
                    addLog("Opening doors (#$openCount)...")
                    delay(5000)

                    val raw = switchGo.getAllSwitchStates()
                    val states = StatusParser.parse(raw).getOrNull()
                    if (states != null && StatusParser.areAllDoorsOpen(states)) {
                        addLog("Doors opened successfully (#$openCount)")
                    } else {
                        addLog("Doors not fully open, retrying...")
                        switchGo.controllerAllDoors(1, 1, 1, 1)
                    }
                } else {
                    // Close phase
                    switchGo.controllerAllDoors(2, 2, 2, 2)
                    closeCount++
                    addLog("Closing doors (#$closeCount)...")
                    delay(5000)

                    val raw = switchGo.getAllSwitchStates()
                    val states = StatusParser.parse(raw).getOrNull()
                    if (states != null && StatusParser.areAllDoorsClosed(states)) {
                        addLog("Doors closed successfully (#$closeCount)")
                    } else {
                        addLog("Doors not fully closed, retrying...")
                        switchGo.controllerAllDoors(2, 2, 2, 2)
                    }
                }
                index++
                delay(20000)
            }
            addLog("Door test completed ($openCount opens, $closeCount closes)")
        }
    }

    fun stopDoorTest() {
        doorTestJob?.cancel()
        addLog("Door test stopped")
    }

    // --- Logging ---

    fun addLogManually(message: String) = addLog(message)

    fun clearLog() {
        _logMessages.value = emptyList()
    }

    private fun addLog(message: String) {
        _logMessages.update { current ->
            (current + message).takeLast(MAX_LOG_SIZE)
        }
    }

    // --- Firmware Asset Copy ---

    private fun copyFirmwareAssets() {
        viewModelScope.launch {
            val success = copyAssetsToInternalStorage(
                arrayOf(Constants.MCU_FIR_TOP_NAME, Constants.MCU_FIR_BOTTOM_NAME)
            )
            _isFirmwareReady.value = success
            if (success) {
                addLog("Firmware files ready")
            } else {
                addLog("Warning: Firmware files not available — MCU update disabled")
            }
        }
    }

    private fun copyAssetsToInternalStorage(binFiles: Array<String>): Boolean {
        var allSuccess = true
        binFiles.forEach { fileName ->
            try {
                val inputStream: InputStream = context.assets.open(fileName)
                inputStream.use { input ->
                    val outputFile = File(context.filesDir, fileName)
                    FileOutputStream(outputFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: IOException) {
                allSuccess = false
            }
        }
        return allSuccess
    }

    // --- Cleanup ---

    override fun onCleared() {
        super.onCleared()
        switchGo.removeCallback(updateCallback)
        doorTestJob?.cancel()
        stopAutoConnect()
    }

    // --- MCU Update Callback ---

    private inner class McuUpdateCallbackImpl : McuUpdateCallback {
        override fun onUpdateStart() {
            _mcuUpdateState.value = McuUpdateState.Starting
            addLog("Firmware update started...")
        }

        override fun onProgress(len: Long) {
            _mcuUpdateState.value = McuUpdateState.InProgress(len)
            addLog("Update progress — remaining bytes: $len")
        }

        override fun onFail(str: String) {
            _mcuUpdateState.value = McuUpdateState.Failed(str)
            addLog("Update FAILED: $str")
        }

        override fun onSuccess() {
            _mcuUpdateState.value = McuUpdateState.Succeeded
            addLog("Update successful!")
        }
    }
}
