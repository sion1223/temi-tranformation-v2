package kr.hs.gwangyang.temidelivery.basket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/** Deterministic local stand-in for app tests and a no-hardware demo. */
class FakeBasketGateway(
    initialSnapshot: BasketSnapshot = BasketSnapshot.safeFake(),
) : BasketGateway {
    private val _connectionState = MutableStateFlow(BasketConnectionState.READY)
    override val connectionState: StateFlow<BasketConnectionState> = _connectionState.asStateFlow()

    private val _status = MutableStateFlow(initialSnapshot)
    override val status: StateFlow<BasketSnapshot> = _status.asStateFlow()

    private val _events = MutableSharedFlow<BasketEvent>(extraBufferCapacity = 32)
    override val events: Flow<BasketEvent> = _events.asSharedFlow()

    private var preparedMission: BasketMission? = null
    private var nextFailure: Throwable? = null
    private val _commands = mutableListOf<FakeBasketCommand>()
    val commands: List<FakeBasketCommand>
        @Synchronized get() = _commands.toList()

    override suspend fun refresh(): Result<BasketSnapshot> {
        if (_connectionState.value != BasketConnectionState.READY) {
            return Result.failure(BasketException("가짜 바구니 연결이 준비되지 않았습니다."))
        }
        return Result.success(_status.value)
    }

    override suspend fun prepareMission(mission: BasketMission): Result<Unit> = synchronized(this) {
        command(BasketCommandType.PREPARE_MISSION).fold(
            onSuccess = {
                val snapshot = _status.value
                if (!snapshot.isSafeForPreparation()) {
                    return@synchronized Result.failure(
                        BasketSafetyException("바구니가 잠금·문닫힘·센서 정상 상태가 아닙니다."),
                    )
                }
                preparedMission = mission
                acknowledge(BasketCommandType.PREPARE_MISSION)
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun unlock(request: BasketUnlockRequest): Result<Unit> = synchronized(this) {
        command(BasketCommandType.UNLOCK).fold(
            onSuccess = {
                val mission = preparedMission
                if (mission == null || mission.missionId != request.missionId || mission.stopId != request.stopId) {
                    return@synchronized Result.failure(
                        BasketSafetyException("현재 배부 지점에 대해 준비된 바구니 작업이 없습니다."),
                    )
                }
                if (!_status.value.isSafeForUnlock()) {
                    return@synchronized Result.failure(
                        BasketSafetyException("바구니 잠금·문닫힘·센서 상태를 확인할 수 없어 열지 않습니다."),
                    )
                }
                _status.value = _status.value.copy(
                    sequence = _status.value.sequence + 1,
                    lock = BasketLockState.UNLOCKED,
                )
                acknowledge(BasketCommandType.UNLOCK)
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun lock(request: BasketLockRequest): Result<Unit> = synchronized(this) {
        command(BasketCommandType.LOCK).fold(
            onSuccess = {
                if (!missionMatches(request.missionId, request.stopId)) {
                    return@synchronized Result.failure(
                        BasketSafetyException("현재 배부 지점과 일치하지 않는 잠금 요청입니다."),
                    )
                }
                if (_status.value.door != BasketDoorState.CLOSED) {
                    return@synchronized Result.failure(
                        BasketSafetyException("바구니 문이 열려 있어 잠글 수 없습니다."),
                    )
                }
                _status.value = _status.value.copy(
                    sequence = _status.value.sequence + 1,
                    lock = BasketLockState.LOCKED,
                )
                acknowledge(BasketCommandType.LOCK)
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun safeState(reason: String): Result<Unit> = synchronized(this) {
        command(BasketCommandType.SAFE_STATE).fold(
            onSuccess = {
                if (_status.value.door != BasketDoorState.CLOSED) {
                    return@synchronized Result.failure(
                        BasketSafetyException("바구니 문이 열려 있어 안전 잠금을 확인할 수 없습니다."),
                    )
                }
                preparedMission = null
                _status.value = _status.value.copy(
                    sequence = _status.value.sequence + 1,
                    lock = BasketLockState.LOCKED,
                )
                acknowledge(BasketCommandType.SAFE_STATE)
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) },
        )
    }

    fun setConnection(state: BasketConnectionState) {
        _connectionState.value = state
        _events.tryEmit(BasketEvent.ConnectionChanged(state))
    }

    fun setSnapshot(snapshot: BasketSnapshot) {
        _status.value = snapshot
        _events.tryEmit(BasketEvent.StatusChanged(snapshot))
    }

    fun setDoor(state: BasketDoorState) {
        setSnapshot(_status.value.copy(door = state, sequence = _status.value.sequence + 1))
    }

    fun failNextCommand(cause: Throwable) {
        nextFailure = cause
    }

    private fun command(type: BasketCommandType): Result<Unit> {
        _commands += FakeBasketCommand(UUID.randomUUID().toString(), type)
        if (_connectionState.value != BasketConnectionState.READY) {
            return Result.failure(BasketException("가짜 바구니 연결이 준비되지 않았습니다."))
        }
        val failure = nextFailure
        nextFailure = null
        return if (failure == null) Result.success(Unit) else Result.failure(failure)
    }

    private fun acknowledge(type: BasketCommandType) {
        val requestId = _commands.last().requestId
        _events.tryEmit(BasketEvent.StatusChanged(_status.value))
        _events.tryEmit(BasketEvent.CommandAcknowledged(requestId, type, _status.value))
    }

    private fun missionMatches(missionId: String, stopId: String): Boolean =
        preparedMission?.let { it.missionId == missionId && it.stopId == stopId } == true

    private fun BasketSnapshot.isSafeForPreparation() =
        door == BasketDoorState.CLOSED && lock == BasketLockState.LOCKED &&
            sensor == BasketSensorState.OK && loadState == BasketLoadState.OK

    private fun BasketSnapshot.isSafeForUnlock() =
        door == BasketDoorState.CLOSED && lock == BasketLockState.LOCKED &&
            sensor == BasketSensorState.OK && loadState == BasketLoadState.OK
}

data class FakeBasketCommand(
    val requestId: String,
    val command: BasketCommandType,
)
