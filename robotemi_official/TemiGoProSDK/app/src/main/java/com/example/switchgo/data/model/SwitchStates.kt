package com.example.switchgo.data.model

/**
 * Parsed device state from the SwitchGo MCU hex response.
 * See readme.md for the byte-level protocol specification.
 */
data class SwitchStates(
    val doorMotors: List<DoorMotorState>,
    val openLimits: List<LimitState>,
    val closeLimits: List<LimitState>,
    val spacePanels: List<SpacePanelState>,
    val batterySoc: BatteryState,
    val auxiliaryConnected: Boolean?,
    val resistanceAlarms: List<Boolean>,
) {
    companion object {
        val EMPTY = SwitchStates(
            doorMotors = emptyList(),
            openLimits = emptyList(),
            closeLimits = emptyList(),
            spacePanels = emptyList(),
            batterySoc = BatteryState.Unknown,
            auxiliaryConnected = null,
            resistanceAlarms = emptyList(),
        )
    }
}

enum class DoorMotorState(val code: Int, val description: String) {
    STOPPED(0x00, "Motor stopped"),
    RUNNING(0x01, "Motor powered and running"),
    POWERED_NOT_ROTATING(0x03, "Motor powered but not rotating"),
    EXTERNAL_ROTATION(0x04, "Motor not powered but detected rotation"),
    UNKNOWN(0xFF, "Unknown status");

    companion object {
        fun fromByte(byte: Int): DoorMotorState =
            entries.find { it.code == byte } ?: UNKNOWN
    }
}

enum class LimitState(val code: Int) {
    NOT_TRIGGERED(0x00),
    TRIGGERED(0x01),
    UNKNOWN(0xFF);

    fun description(isClose: Boolean = false): String = when (this) {
        NOT_TRIGGERED -> "Limit not triggered"
        TRIGGERED -> if (isClose) "Limit triggered, door closed"
        else "Limit triggered, door open"
        UNKNOWN -> "Unknown limit status"
    }

    companion object {
        fun fromByte(byte: Int): LimitState =
            entries.find { it.code == byte } ?: UNKNOWN
    }
}

enum class SpacePanelState(val code: Int, val description: String) {
    INSTALLED(0x01, "Space panel installed"),
    NOT_INSTALLED(0x02, "Space panel not installed"),
    UNKNOWN(0xFF, "Unknown space panel status");

    companion object {
        fun fromByte(byte: Int): SpacePanelState =
            entries.find { it.code == byte } ?: UNKNOWN
    }
}

sealed class BatteryState {
    data class Level(val percentage: Int) : BatteryState()
    object Disconnected : BatteryState()
    object Unknown : BatteryState()

    val description: String
        get() = when (this) {
            is Level -> "Battery: ${percentage}%"
            Disconnected -> "Battery indicator module disconnected"
            Unknown -> "Battery: unknown"
        }

    companion object {
        fun fromByte(byte: Int): BatteryState = when (byte) {
            0xFF -> Disconnected
            in 0..100 -> Level(byte)
            else -> Unknown
        }
    }
}

sealed class McuUpdateState {
    object Idle : McuUpdateState()
    object Starting : McuUpdateState()
    data class InProgress(val remainingBytes: Long) : McuUpdateState()
    data class Failed(val reason: String) : McuUpdateState()
    object Succeeded : McuUpdateState()
}
