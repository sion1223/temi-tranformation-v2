package kr.hs.gwangyang.temidelivery.basket

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BasketClientConfigTest {
    @Test
    fun `parser reads enabled device config and normalizes base url`() {
        val config = BasketClientFactory.parseConfig(
            JSONObject(
                """
                {
                  "schemaVersion": 1,
                  "enabled": true,
                  "baseUrl": "http://basket.local:8080/",
                  "deviceId": "basket-01",
                  "deviceToken": "device-secret"
                }
                """.trimIndent(),
            ),
        )

        assertTrue(config.enabled)
        assertEquals("http://basket.local:8080", config.baseUrl)
        assertEquals("basket-01", config.expectedDeviceId)
        assertEquals("device-secret", config.deviceToken)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `enabled config requires a device token`() {
        BasketClientFactory.parseConfig(
            JSONObject(
                """
                {
                  "schemaVersion": 1,
                  "enabled": true,
                  "baseUrl": "http://127.0.0.1:8080",
                  "deviceId": "basket-01",
                  "deviceToken": ""
                }
                """.trimIndent(),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `enabled config requires the expected physical device id`() {
        BasketClientFactory.parseConfig(
            JSONObject(
                """
                {
                  "schemaVersion": 1,
                  "enabled": true,
                  "baseUrl": "http://127.0.0.1:8080",
                  "deviceId": "",
                  "deviceToken": "device-secret"
                }
                """.trimIndent(),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `config rejects non HTTP endpoint`() {
        BasketClientFactory.parseConfig(
            JSONObject(
                """
                {
                  "schemaVersion": 1,
                  "enabled": true,
                  "baseUrl": "ftp://basket.local",
                  "deviceToken": "device-secret"
                }
                """.trimIndent(),
            ),
        )
    }
}
