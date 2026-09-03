package kr.hs.gwangyang.temidelivery.basket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

const val BASKET_PROTOCOL_VERSION = 1

enum class BasketConnectionState {
    DISABLED,
    DISCONNECTED,
    CONNECTING,
    READY,
    STALE,
    ERROR,
}

enum class BasketDoorState {
    OPEN,
    CLOSED,
    UNKNOWN,
}

enum class BasketLockState {
    LOCKED,
    UNLOCKED,
    UNKNOWN,
}

enum class BasketSensorState {
    OK,
    OVERLOAD,
    UNBALANCED,
    SENSOR_FAULT,
    UNKNOWN,
}

enum class BasketLoadState {
    OK,
    OVERLOAD,
    UNBALANCED,
    SENSOR_FAULT,
    UNKNOWN,
}

enum class BasketCommandType {
    PREPARE_MISSION,
    UNLOCK,
    LOCK,
    SAFE_STATE,
}

data class BasketSnapshot(
    val protocolVersion: Int,
    val deviceId: String,
    val firmwareVersion: String,
    val sequence: Long,
    val uptimeMs: Long,
    val door: BasketDoorState,
    val lock: BasketLockState,
    val sensor: BasketSensorState,
    val weightGrams: Int?,
    val loadState: BasketLoadState,
) {
    companion object {
        fun unknown(deviceId: String = "unknown") = BasketSnapshot(
            protocolVersion = BASKET_PROTOCOL_VERSION,
            deviceId = deviceId,
            firmwareVersion = "unknown",
            sequence = 0,
            uptimeMs = 0,
            door = BasketDoorState.UNKNOWN,
            lock = BasketLockState.UNKNOWN,
            sensor = BasketSensorState.UNKNOWN,
            weightGrams = null,
            loadState = BasketLoadState.UNKNOWN,
        )

        fun safeFake(deviceId: String = "fake-basket") = BasketSnapshot(
            protocolVersion = BASKET_PROTOCOL_VERSION,
            deviceId = deviceId,
            firmwareVersion = "fake",
            sequence = 0,
            uptimeMs = 0,
            door = BasketDoorState.CLOSED,
            lock = BasketLockState.LOCKED,
            sensor = BasketSensorState.OK,
            weightGrams = 0,
            loadState = BasketLoadState.OK,
        )
    }
}

data class BasketMission(
    val missionId: String,
    val stopId: String,
    val expectedQuantity: Int,
    val expectedWeightGrams: Int? = null,
    val weightToleranceGrams: Int? = null,
) {
    init {
        require(missionId.isNotBlank()) { "missionId가 비어 있습니다." }
        require(stopId.isNotBlank()) { "stopId가 비어 있습니다." }
        require(expectedQuantity > 0) { "expectedQuantity는 1 이상이어야 합니다." }
        require(expectedWeightGrams == null || expectedWeightGrams >= 0) {
            "expectedWeightGrams는 0 이상이어야 합니다."
        }
        require(weightToleranceGrams == null || weightToleranceGrams >= 0) {
            "weightToleranceGrams는 0 이상이어야 합니다."
        }
    }
}

data class BasketUnlockRequest(
    val missionId: String,
    val stopId: String,
    val expectedQuantity: Int,
    val expectedWeightGrams: Int? = null,
    val weightToleranceGrams: Int? = null,
) {
    init {
        require(missionId.isNotBlank()) { "missionId가 비어 있습니다." }
        require(stopId.isNotBlank()) { "stopId가 비어 있습니다." }
        require(expectedQuantity > 0) { "expectedQuantity는 1 이상이어야 합니다." }
    }
}

data class BasketLockRequest(
    val missionId: String,
    val stopId: String,
) {
    init {
        require(missionId.isNotBlank()) { "missionId가 비어 있습니다." }
        require(stopId.isNotBlank()) { "stopId가 비어 있습니다." }
    }
}

sealed interface BasketEvent {
    data class StatusChanged(val status: BasketSnapshot) : BasketEvent

    data class ConnectionChanged(
        val state: BasketConnectionState,
        val message: String? = null,
    ) : BasketEvent

    data class CommandAcknowledged(
        val requestId: String,
        val command: BasketCommandType,
        val status: BasketSnapshot,
    ) : BasketEvent

    data class Error(
        val command: BasketCommandType?,
        val cause: Throwable,
    ) : BasketEvent
}

interface BasketGateway {
    val connectionState: StateFlow<BasketConnectionState>
    val status: StateFlow<BasketSnapshot>
    val events: Flow<BasketEvent>

    suspend fun refresh(): Result<BasketSnapshot>

    suspend fun prepareMission(mission: BasketMission): Result<Unit>

    suspend fun unlock(request: BasketUnlockRequest): Result<Unit>

    suspend fun lock(request: BasketLockRequest): Result<Unit>

    suspend fun safeState(reason: String): Result<Unit>
}

open class BasketException(message: String, cause: Throwable? = null) : Exception(message, cause)

class BasketProtocolException(message: String, cause: Throwable? = null) : BasketException(message, cause)

class BasketSafetyException(message: String, cause: Throwable? = null) : BasketException(message, cause)

class BasketDisabledException(message: String) : BasketException(message)

class BasketHttpException(
    val statusCode: Int,
    val errorCode: String,
    message: String,
) : BasketException(message)
