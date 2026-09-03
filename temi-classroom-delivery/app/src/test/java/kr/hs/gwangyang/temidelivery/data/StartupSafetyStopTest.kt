package kr.hs.gwangyang.temidelivery.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class StartupSafetyStopTest {
    @Test
    fun `does not stop before robot SDK is ready`() {
        val guard = StartupSafetyStop()
        var calls = 0

        val attempted = guard.attempt(isReady = false) { calls += 1 }

        assertFalse(attempted)
        assertEquals(0, calls)
    }

    @Test
    fun `stops exactly once after SDK becomes ready`() {
        val guard = StartupSafetyStop()
        var calls = 0

        assertTrue(guard.attempt(isReady = true) { calls += 1 })
        assertFalse(guard.attempt(isReady = true) { calls += 1 })

        assertEquals(1, calls)
    }

    @Test
    fun `retries when the first SDK stop call fails`() {
        val guard = StartupSafetyStop()
        var calls = 0

        assertThrows(IllegalStateException::class.java) {
            guard.attempt(isReady = true) {
                calls += 1
                throw IllegalStateException("SDK unavailable")
            }
        }
        assertTrue(guard.attempt(isReady = true) { calls += 1 })

        assertEquals(2, calls)
    }
}
