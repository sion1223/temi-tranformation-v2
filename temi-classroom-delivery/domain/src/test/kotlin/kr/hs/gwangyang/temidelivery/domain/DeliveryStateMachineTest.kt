package kr.hs.gwangyang.temidelivery.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeliveryStateMachineTest {
    @Test
    fun `saved location route is rejected when a location is missing`() {
        val machine = DeliveryStateMachine()

        val transition = machine.start(
            route = route(),
            robotReady = true,
            savedLocations = listOf("배부-1", "교탁"),
        )

        assertEquals(DeliveryPhase.CONFIGURATION_ERROR, transition.state.phase)
        assertTrue(transition.state.message.contains("배부-2"))
        assertTrue(transition.commands.isEmpty())
    }

    @Test
    fun `mission visits each stop only after pickup confirmation and then returns`() {
        val machine = startedMachine()

        val firstArrival = machine.onNavigationEvent(complete("배부-1"))
        assertEquals(DeliveryPhase.WAITING_FOR_PICKUP, firstArrival.state.phase)
        assertEquals(0, firstArrival.state.stopIndex)
        assertTrue(firstArrival.commands.single() is RobotCommand.Speak)

        val next = machine.confirmPickup()
        assertEquals(DeliveryPhase.NAVIGATING, next.state.phase)
        assertEquals(1, next.state.stopIndex)
        assertNavigateTo(next, "배부-2")

        machine.onNavigationEvent(complete("배부-2"))
        val returning = machine.confirmPickup()
        assertEquals(DeliveryPhase.RETURNING, returning.state.phase)
        assertNavigateTo(returning, "교탁")

        val completed = machine.onNavigationEvent(complete("교탁"))
        assertEquals(DeliveryPhase.COMPLETED, completed.state.phase)
        assertEquals(2, completed.state.completedStopCount)
    }

    @Test
    fun `stale callback for another saved location is ignored`() {
        val machine = startedMachine()

        val transition = machine.onNavigationEvent(complete("배부-2"))

        assertEquals(DeliveryPhase.NAVIGATING, transition.state.phase)
        assertEquals(0, transition.state.stopIndex)
        assertTrue(transition.commands.isEmpty())
    }

    @Test
    fun `aborted navigation can retry the same stop`() {
        val machine = startedMachine()
        val failed = machine.onNavigationEvent(
            NavigationEvent("배부-1", NavigationStatus.ABORT, 2000, "장애물로 경로가 막힘"),
        )

        assertEquals(DeliveryPhase.FAILED, failed.state.phase)
        assertEquals(FailureTarget.CURRENT_STOP, failed.state.failure?.target)

        val retried = machine.retry()
        assertEquals(DeliveryPhase.NAVIGATING, retried.state.phase)
        assertNavigateTo(retried, "배부-1")
    }

    @Test
    fun `emergency stop emits stop command and blocks navigation state`() {
        val machine = startedMachine()

        val stopped = machine.emergencyStop()

        assertEquals(DeliveryPhase.EMERGENCY_STOPPED, stopped.state.phase)
        assertEquals(listOf(RobotCommand.StopMovement), stopped.commands)
        assertTrue(machine.onNavigationEvent(complete("배부-1")).commands.isEmpty())
        assertEquals(DeliveryPhase.EMERGENCY_STOPPED, machine.state.phase)

        val restarted = machine.start(
            route = route(),
            robotReady = true,
            savedLocations = listOf("배부-1", "배부-2", "교탁"),
        )
        assertEquals(DeliveryPhase.EMERGENCY_STOPPED, restarted.state.phase)
        assertTrue(restarted.commands.single() is RobotCommand.Speak)
    }

    @Test
    fun `cancelling active navigation emits stop command and marks mission cancelled`() {
        val machine = startedMachine()

        val cancelled = machine.cancel()

        assertEquals(DeliveryPhase.CANCELLED, cancelled.state.phase)
        assertTrue(cancelled.commands.contains(RobotCommand.StopMovement))
        assertTrue(machine.onNavigationEvent(complete("배부-1")).commands.isEmpty())
        assertEquals(DeliveryPhase.CANCELLED, machine.state.phase)
    }

    @Test
    fun `coordinate destination does not require a saved location`() {
        val machine = DeliveryStateMachine()
        val coordinateRoute = DeliveryRoute(
            name = "좌표 배부",
            stops = listOf(
                DeliveryStop(
                    id = "one",
                    destination = Destination.Coordinate(1.2f, -0.4f, 0.5f, "1모둠"),
                    recipient = "1모둠",
                    supply = "교재",
                    quantity = 1,
                ),
            ),
            returnDestination = Destination.Coordinate(0f, 0f, 999f, "출발점"),
        )

        val started = machine.start(coordinateRoute, robotReady = true, savedLocations = emptyList())
        assertEquals(DeliveryPhase.NAVIGATING, started.state.phase)
        assertTrue(started.commands.last() is RobotCommand.NavigateTo)

        val arrived = machine.onNavigationEvent(complete(location = ""))
        assertEquals(DeliveryPhase.WAITING_FOR_PICKUP, arrived.state.phase)
    }

    @Test
    fun `skipping last stop returns to base`() {
        val machine = startedMachine()
        machine.onNavigationEvent(complete("배부-1"))
        machine.confirmPickup()
        machine.onNavigationEvent(complete("배부-2"))

        val transition = machine.skipCurrentStop()

        assertEquals(DeliveryPhase.RETURNING, transition.state.phase)
        assertNavigateTo(transition, "교탁")
    }

    private fun startedMachine() = DeliveryStateMachine().also { machine ->
        val transition = machine.start(
            route = route(),
            robotReady = true,
            savedLocations = listOf("배부-1", "배부-2", "교탁"),
        )
        assertEquals(DeliveryPhase.NAVIGATING, transition.state.phase)
        assertNavigateTo(transition, "배부-1")
    }

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
            DeliveryStop(
                id = "two",
                destination = Destination.SavedLocation("배부-2"),
                recipient = "2모둠",
                supply = "교재",
                quantity = 1,
            ),
        ),
        returnDestination = Destination.SavedLocation("교탁"),
    )

    private fun complete(location: String) = NavigationEvent(
        location = location,
        status = NavigationStatus.COMPLETE,
        descriptionId = 0,
        description = "",
    )

    private fun assertNavigateTo(transition: DeliveryTransition, location: String) {
        val command = transition.commands.filterIsInstance<RobotCommand.NavigateTo>().single()
        assertEquals(location, (command.destination as Destination.SavedLocation).name)
    }
}
