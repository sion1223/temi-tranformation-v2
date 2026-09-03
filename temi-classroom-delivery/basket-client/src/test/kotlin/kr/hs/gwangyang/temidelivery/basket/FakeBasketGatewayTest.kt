package kr.hs.gwangyang.temidelivery.basket

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeBasketGatewayTest {
    @Test
    fun `fake basket defaults to a locked safe state`() = runBlocking {
        val gateway = FakeBasketGateway()

        assertEquals(BasketConnectionState.READY, gateway.connectionState.value)
        assertEquals(BasketLockState.LOCKED, gateway.status.value.lock)
        assertEquals(BasketDoorState.CLOSED, gateway.status.value.door)
        assertEquals(BasketSensorState.OK, gateway.status.value.sensor)
    }

    @Test
    fun `unlock requires prepared mission and safe status`() = runBlocking {
        val gateway = FakeBasketGateway()
        val request = BasketUnlockRequest("mission-1", "group-1", expectedQuantity = 1)

        assertFalse(gateway.unlock(request).isSuccess)
        assertTrue(gateway.prepareMission(request.toMission()).isSuccess)
        assertTrue(gateway.unlock(request).isSuccess)
        assertEquals(BasketLockState.UNLOCKED, gateway.status.value.lock)
    }

    @Test
    fun `open door blocks lock and safe state remains failed closed`() = runBlocking {
        val gateway = FakeBasketGateway()
        val request = BasketUnlockRequest("mission-1", "group-1", expectedQuantity = 1)
        gateway.prepareMission(request.toMission()).getOrThrow()
        gateway.unlock(request).getOrThrow()
        gateway.setDoor(BasketDoorState.OPEN)

        assertFalse(gateway.lock(BasketLockRequest("mission-1", "group-1")).isSuccess)
        assertFalse(gateway.safeState("app exit").isSuccess)
        assertEquals(BasketLockState.UNLOCKED, gateway.status.value.lock)
    }

    private fun BasketUnlockRequest.toMission() = BasketMission(
        missionId = missionId,
        stopId = stopId,
        expectedQuantity = expectedQuantity,
        expectedWeightGrams = expectedWeightGrams,
        weightToleranceGrams = weightToleranceGrams,
    )
}
