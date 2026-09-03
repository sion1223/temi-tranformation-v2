package kr.hs.gwangyang.temidelivery.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class DeliverySpeedTest {
    @Test
    fun `very slow navigation is capped below the temi preset`() {
        assertEquals(0.2f, DeliverySpeed.VERY_SLOW.maxMetersPerSecond, 0.0001f)
    }

    @Test
    fun `standard speed caps match the temi levels`() {
        assertEquals(0.5f, DeliverySpeed.SLOW.maxMetersPerSecond, 0.0001f)
        assertEquals(0.7f, DeliverySpeed.MEDIUM.maxMetersPerSecond, 0.0001f)
    }
}
