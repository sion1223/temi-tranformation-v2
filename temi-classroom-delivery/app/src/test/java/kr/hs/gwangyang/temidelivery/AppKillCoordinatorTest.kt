package kr.hs.gwangyang.temidelivery

import kr.hs.gwangyang.temidelivery.data.KioskGateway
import kr.hs.gwangyang.temidelivery.data.KioskState
import org.junit.Assert.assertEquals
import org.junit.Test

class AppKillCoordinatorTest {
    @Test
    fun `robot stop is requested before kiosk is disabled`() {
        val events = mutableListOf<String>()
        val gateway = FakeKioskGateway(events = events)
        val coordinator = AppKillCoordinator(
            stopRobot = {
                events += "stop-robot"
                Result.success(Unit)
            },
            kioskGateway = gateway,
        )

        coordinator.prepareForAppKill()

        assertEquals(listOf("stop-robot", "disable-kiosk"), events)
    }

    @Test
    fun `stop failure cannot prevent kiosk disable attempt`() {
        val gateway = FakeKioskGateway()
        val coordinator = AppKillCoordinator(
            stopRobot = { Result.failure(IllegalStateException("stop failed")) },
            kioskGateway = gateway,
        )

        coordinator.prepareForAppKill()

        assertEquals(1, gateway.disableForExitCalls)
    }

    @Test
    fun `unexpected cleanup exceptions cannot prevent process kill caller from continuing`() {
        val gateway = FakeKioskGateway(throwOnDisable = true)
        var stopCalls = 0
        val coordinator = AppKillCoordinator(
            stopRobot = {
                stopCalls += 1
                throw IllegalStateException("unexpected stop exception")
            },
            kioskGateway = gateway,
        )

        coordinator.prepareForAppKill()

        assertEquals(1, stopCalls)
        assertEquals(1, gateway.disableForExitCalls)
    }

    @Test
    fun `repeated preparations always retry both cleanup operations`() {
        var stopCalls = 0
        val gateway = FakeKioskGateway()
        val coordinator = AppKillCoordinator(
            stopRobot = {
                stopCalls += 1
                Result.success(Unit)
            },
            kioskGateway = gateway,
        )

        coordinator.prepareForAppKill()
        coordinator.prepareForAppKill()

        assertEquals(2, stopCalls)
        assertEquals(2, gateway.disableForExitCalls)
    }

    private class FakeKioskGateway(
        private val throwOnDisable: Boolean = false,
        private val events: MutableList<String> = mutableListOf(),
    ) : KioskGateway {
        var disableForExitCalls: Int = 0
            private set

        override fun readState(): KioskState = KioskState(
            sdkReady = true,
            selectedKioskApp = true,
            kioskModeOn = true,
        )

        override fun disableForExit(): Result<Unit> {
            disableForExitCalls += 1
            events += "disable-kiosk"
            if (throwOnDisable) throw IllegalStateException("unexpected kiosk exception")
            return Result.success(Unit)
        }
    }
}
