package kr.hs.gwangyang.temidelivery.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeliveryCoordinatorTest {
    @Test
    fun `repeated app kill preparations always stop the robot`() {
        val gateway = FakeRobotGateway()
        val coordinator = coordinatorFor(gateway)

        assertTrue(coordinator.stopForAppKill().isSuccess)
        assertTrue(coordinator.stopForAppKill().isSuccess)

        assertEquals(2, gateway.stopMovementCalls)
    }

    @Test
    fun `app kill preparation returns failure when stopping the robot throws`() {
        val failure = IllegalStateException("stop command failed")
        val gateway = FakeRobotGateway(stopFailure = failure)
        val coordinator = coordinatorFor(gateway)

        val result = coordinator.stopForAppKill()

        assertTrue(result.isFailure)
        assertEquals(failure, result.exceptionOrNull())
        assertEquals(1, gateway.stopMovementCalls)
    }

    @Test
    fun `app kill preparation still attempts stop when readiness is stale`() {
        val gateway = FakeRobotGateway(ready = false)
        val coordinator = coordinatorFor(gateway)

        val result = coordinator.stopForAppKill()

        assertTrue(result.isSuccess)
        assertEquals(1, gateway.stopMovementCalls)
    }

    @Test
    fun `app kill preparation stops while navigation is active`() {
        val gateway = FakeRobotGateway(
            locations = listOf("배부-1", "교탁"),
        )
        val coordinator = coordinatorFor(gateway)
        coordinator.start(route())

        val result = coordinator.stopForAppKill()

        assertTrue(result.isSuccess)
        assertEquals(1, gateway.stopMovementCalls)
    }

    @Test
    fun `app kill preparation retries stop after a failed cancel`() {
        val gateway = FakeRobotGateway(
            locations = listOf("배부-1", "교탁"),
            stopFailure = IllegalStateException("stop command failed"),
        )
        val coordinator = coordinatorFor(gateway)
        coordinator.start(route())

        coordinator.cancel()

        assertEquals(DeliveryPhase.FAILED, coordinator.state.value.phase)
        assertTrue(coordinator.stopForAppKill().isFailure)
        assertEquals(2, gateway.stopMovementCalls)
    }

    @Test
    fun `failed stop prevents the following return navigation command`() {
        val gateway = FakeRobotGateway(
            locations = listOf("배부-1", "교탁"),
            stopFailure = IllegalStateException("stop command failed"),
        )
        val coordinator = coordinatorFor(gateway)
        coordinator.start(route())

        coordinator.returnNow()

        assertEquals(DeliveryPhase.FAILED, coordinator.state.value.phase)
        assertEquals(1, gateway.navigateToCalls)
    }

    private fun coordinatorFor(gateway: FakeRobotGateway) = DeliveryCoordinator(
        robotGateway = gateway,
        scope = CoroutineScope(Dispatchers.Unconfined),
    )

    private fun route() = DeliveryRoute(
        name = "테스트 배부",
        stops = listOf(
            DeliveryStop(
                id = "one",
                destination = Destination.SavedLocation("배부-1"),
                recipient = "1모둠",
                supply = "교재",
                quantity = 1,
            ),
        ),
        returnDestination = Destination.SavedLocation("교탁"),
    )

    private class FakeRobotGateway(
        ready: Boolean = true,
        private val locations: List<String> = emptyList(),
        private val stopFailure: Throwable? = null,
    ) : RobotGateway {
        override val isReady = MutableStateFlow(ready)
        override val savedLocations = MutableStateFlow(locations)
        override val navigationEvents: Flow<NavigationEvent> = emptyFlow()
        var stopMovementCalls: Int = 0
            private set
        var navigateToCalls: Int = 0
            private set

        override fun refreshRobotState() = Unit

        override fun currentPose(): RobotPose? = null

        override fun navigateTo(
            destination: Destination,
            speed: DeliverySpeed,
            highAccuracyArrival: Boolean,
        ) {
            navigateToCalls += 1
        }

        override fun stopMovement() {
            stopMovementCalls += 1
            stopFailure?.let { throw it }
        }

        override fun followMe(speed: DeliverySpeed) = Unit

        override fun speak(text: String) = Unit
    }
}
