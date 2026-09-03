package kr.hs.gwangyang.temidelivery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureSettingsTest {
    @Test
    fun `defaults keep current MVP features available with 20 second tap voice mode`() {
        val settings = FeatureSettings()

        assertTrue(settings.aiAssistantEnabled)
        assertTrue(settings.speechOutputEnabled)
        assertTrue(settings.voiceInputEnabled)
        assertTrue(settings.keepScreenOnEnabled)
        assertTrue(settings.immersiveModeEnabled)
        assertTrue(settings.blockBackEnabled)
        assertTrue(settings.deliveryControlsEnabled)
        assertFalse(settings.basketIntegrationEnabled)
        assertEquals(VoiceActivationMode.TAP_TO_TALK, settings.voiceActivationMode)
        assertEquals(20_000L, MAX_VOICE_RECORDING_MILLIS)
    }

    @Test
    fun `unknown persisted voice mode safely falls back to tap mode`() {
        assertEquals(
            VoiceActivationMode.TAP_TO_TALK,
            VoiceActivationMode.fromStoredValue("future-mode"),
        )
        assertEquals(
            VoiceActivationMode.TEMI_WAKE_WORD,
            VoiceActivationMode.fromStoredValue("TEMI_WAKE_WORD"),
        )
    }

    @Test
    fun `Arduino runtime settings normalize URL and map to basket config`() {
        val config = FeatureSettings(
            basketIntegrationEnabled = true,
            basketBaseUrl = "  http://192.168.4.1/  ",
            basketDeviceId = " basket-01 ",
            basketDeviceToken = " local-token ",
        ).toBasketClientConfig()

        assertTrue(config.enabled)
        assertEquals("http://192.168.4.1", config.baseUrl)
        assertEquals("basket-01", config.expectedDeviceId)
        assertEquals("local-token", config.deviceToken)
    }
}
