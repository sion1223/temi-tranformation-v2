package kr.hs.gwangyang.temidelivery.basket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class DisabledBasketGateway(
    private val reason: String,
) : BasketGateway {
    private val _connectionState = MutableStateFlow(BasketConnectionState.DISABLED)
    override val connectionState: StateFlow<BasketConnectionState> = _connectionState.asStateFlow()

    private val _status = MutableStateFlow(BasketSnapshot.unknown())
    override val status: StateFlow<BasketSnapshot> = _status.asStateFlow()

    private val _events = MutableSharedFlow<BasketEvent>(extraBufferCapacity = 8)
    override val events: Flow<BasketEvent> = _events.asSharedFlow()

    override suspend fun refresh(): Result<BasketSnapshot> = failure()

    override suspend fun prepareMission(mission: BasketMission): Result<Unit> = failure()

    override suspend fun unlock(request: BasketUnlockRequest): Result<Unit> = failure()

    override suspend fun lock(request: BasketLockRequest): Result<Unit> = failure()

    override suspend fun safeState(reason: String): Result<Unit> = failure()

    private fun <T> failure(): Result<T> =
        Result.failure(BasketDisabledException(reason))
}
