package com.example.switchgo.data.parser

import com.example.switchgo.data.model.BatteryState
import com.example.switchgo.data.model.DoorMotorState
import com.example.switchgo.data.model.LimitState
import com.example.switchgo.data.model.SpacePanelState
import com.example.switchgo.data.model.SwitchStates

/**
 * Pure-function parser for SwitchGo MCU hex response strings.
 * No Android dependencies — fully unit-testable.
 */
object StatusParser {

    /**
     * Parse the space-delimited hex string from [SwitchGo.getAllSwitchStates()]
     * into a structured [SwitchStates] object.
     *
     * Protocol byte offsets (0-indexed after prefix bytes 0x30,0x00,0x00,0x16,0x84):
     *   5: G1 motor   6: G2 motor   7: G3 motor   8: G4 motor
     *   9: G1SC      10: G1SO      11: G2SC       12: G2SO
     *  13: G3SC      14: G3SO      15: G4SC       16: G4SO
     *  17: SPACE1    18: SPACE2    19: SPACE3
     *  20: SOC       21: AUXILIARY
     *  22: G1R       23: G2R       24: G3R        25: G4R
     */
    fun parse(hexString: String): Result<SwitchStates> {
        val parts = hexString.split(" ").filter { it.isNotBlank() }
        if (parts.size < 26) {
            return Result.failure(
                IllegalArgumentException(
                    "Data format error: expected at least 26 bytes, got ${parts.size}"
                )
            )
        }

        return try {
            Result.success(
                SwitchStates(
                    doorMotors = (5..8).map { DoorMotorState.fromByte(parts.parseHex(it)) },
                    openLimits = listOf(10, 12, 14, 16).map { LimitState.fromByte(parts.parseHex(it)) },
                    closeLimits = listOf(9, 11, 13, 15).map { LimitState.fromByte(parts.parseHex(it)) },
                    spacePanels = (17..19).map { SpacePanelState.fromByte(parts.parseHex(it)) },
                    batterySoc = BatteryState.fromByte(parts.parseHex(20)),
                    auxiliaryConnected = when (val aux = parts.parseHex(21)) {
                        0xFF -> false
                        else -> true
                    },
                    resistanceAlarms = (22..25).map { parts.parseHex(it) == 0x81 },
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Format parsed states as a human-readable multi-line string for display.
     */
    fun formatForDisplay(states: SwitchStates): String = buildString {
        appendLine("=== Door Motors ===")
        states.doorMotors.forEachIndexed { i, s ->
            appendLine("G${i + 1}: ${s.description} (0x${s.code.toString(16).uppercase()})")
        }
        appendLine()
        appendLine("=== Open Limits ===")
        states.openLimits.forEachIndexed { i, s ->
            appendLine("G${i + 1}SO: ${s.description()}")
        }
        appendLine()
        appendLine("=== Close Limits ===")
        states.closeLimits.forEachIndexed { i, s ->
            appendLine("G${i + 1}SC: ${s.description(isClose = true)}")
        }
        appendLine()
        appendLine("=== Space Panels ===")
        states.spacePanels.forEachIndexed { i, s ->
            appendLine("SPACE${i + 1}: ${s.description}")
        }
        appendLine()
        appendLine("=== Other ===")
        appendLine("SOC: ${states.batterySoc.description}")
        appendLine("AUXILIARY: ${if (states.auxiliaryConnected == true) "Slave connected" else if (states.auxiliaryConnected == false) "Slave disconnected" else "Unknown"}")
        states.resistanceAlarms.forEachIndexed { i, alarm ->
            appendLine("G${i + 1}R: ${if (alarm) "Door closing resistance ALARM" else "Normal"}")
        }
    }

    /**
     * Check if a specific door is fully open based on its open limit state.
     */
    fun isDoorOpen(states: SwitchStates, doorIndex: Int): Boolean =
        doorIndex < states.openLimits.size && states.openLimits[doorIndex] == LimitState.TRIGGERED

    /**
     * Check if a specific door is fully closed based on its close limit state.
     */
    fun isDoorClosed(states: SwitchStates, doorIndex: Int): Boolean =
        doorIndex < states.closeLimits.size && states.closeLimits[doorIndex] == LimitState.TRIGGERED

    /**
     * Check if all 4 doors are open.
     */
    fun areAllDoorsOpen(states: SwitchStates): Boolean =
        states.openLimits.size >= 4 && states.openLimits.take(4).all { it == LimitState.TRIGGERED }

    /**
     * Check if all 4 doors are closed.
     */
    fun areAllDoorsClosed(states: SwitchStates): Boolean =
        states.closeLimits.size >= 4 && states.closeLimits.take(4).all { it == LimitState.TRIGGERED }

    // -- internal helpers --

    private fun List<String>.parseHex(index: Int): Int =
        getOrNull(index)?.toIntOrNull(16) ?: 0xFF
}
