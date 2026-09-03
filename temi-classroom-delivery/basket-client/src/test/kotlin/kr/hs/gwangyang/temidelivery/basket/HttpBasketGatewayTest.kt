package kr.hs.gwangyang.temidelivery.basket

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger

class HttpBasketGatewayTest {
    @Test
    fun `status request sends bearer token and parses protocol status`() = runBlocking {
        val requests = AtomicInteger()
        var authorization: String? = null
        val server = server { exchange ->
            requests.incrementAndGet()
            authorization = exchange.requestHeaders.getFirst("Authorization")
            respond(exchange, statusEnvelope())
        }

        try {
            val gateway = gateway(server)
            val status = gateway.refresh().getOrThrow()

            assertEquals(1, requests.get())
            assertEquals("Bearer device-secret", authorization)
            assertEquals("basket-01", status.deviceId)
            assertEquals(BasketLockState.LOCKED, status.lock)
            assertEquals(BasketSensorState.OK, status.sensor)
            assertEquals(BasketConnectionState.READY, gateway.connectionState.value)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `command sends request id and does not retry a side effect`() = runBlocking {
        val requests = AtomicInteger()
        var command: JSONObject? = null
        val server = server { exchange ->
            requests.incrementAndGet()
            if (exchange.requestMethod == "POST") {
                command = JSONObject(exchange.requestBody.bufferedReader().use { it.readText() })
                val lock = if (command?.getString("command") == "UNLOCK") "UNLOCKED" else "LOCKED"
                respond(exchange, commandEnvelope(command!!.getString("requestId"), lock))
            } else {
                respond(exchange, statusEnvelope())
            }
        }

        try {
            val gateway = gateway(server)
            gateway.refresh().getOrThrow()
            gateway.prepareMission(BasketMission("mission-1", "group-1", 1)).getOrThrow()
            val result = gateway.unlock(BasketUnlockRequest("mission-1", "group-1", 1))

            assertTrue(result.isSuccess)
            assertEquals(3, requests.get())
            assertEquals("UNLOCK", command?.getString("command"))
            assertTrue(command?.getString("requestId")?.isNotBlank() == true)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `HTTP command failure is returned without an automatic retry`() = runBlocking {
        val requests = AtomicInteger()
        val server = server { exchange ->
            requests.incrementAndGet()
            respond(
                exchange,
                """
                {"success":false,"error":{"code":"busy","message":"바구니가 사용 중입니다."}}
                """.trimIndent(),
                statusCode = 409,
            )
        }

        try {
            val gateway = gateway(server)
            val result = gateway.prepareMission(BasketMission("mission-1", "group-1", 1))

            assertFalse(result.isSuccess)
            assertEquals(1, requests.get())
            assertTrue(result.exceptionOrNull() is BasketHttpException)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `side effect timeout is returned after exactly one request`() = runBlocking {
        val requests = AtomicInteger()
        val server = server { exchange ->
            requests.incrementAndGet()
            if (exchange.requestMethod == "POST") {
                exchange.requestBody.use { it.readBytes() }
                Thread.sleep(350)
                runCatching { respond(exchange, commandEnvelope("late", "LOCKED")) }
            } else {
                respond(exchange, statusEnvelope())
            }
        }

        try {
            val gateway = HttpBasketGateway(
                BasketClientConfig(
                    enabled = true,
                    baseUrl = "http://127.0.0.1:${server.address.port}",
                    expectedDeviceId = "basket-01",
                    deviceToken = "device-secret",
                    connectTimeoutMillis = 500,
                    readTimeoutMillis = 100,
                ),
            )
            val result = gateway.prepareMission(BasketMission("mission-1", "group-1", 1))

            assertFalse(result.isSuccess)
            assertEquals(1, requests.get())
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `unlock is blocked when status is stale or not known to be safe`() = runBlocking {
        val requests = AtomicInteger()
        val server = server { exchange ->
            requests.incrementAndGet()
            respond(exchange, statusEnvelope())
        }

        try {
            var now = 0L
            val gateway = gateway(
                server,
                staleAfterMillis = 250,
                nowMillis = { now },
            )
            gateway.refresh().getOrThrow()
            now = 251L
            val result = gateway.unlock(BasketUnlockRequest("mission-1", "group-1", 1))

            assertFalse(result.isSuccess)
            assertEquals(1, requests.get())
            assertTrue(result.exceptionOrNull() is BasketSafetyException)
        } finally {
            server.stop(0)
        }
    }

    private fun gateway(
        server: HttpServer,
        staleAfterMillis: Long = 3_000,
        nowMillis: () -> Long = System::currentTimeMillis,
    ) = HttpBasketGateway(
        BasketClientConfig(
            enabled = true,
            baseUrl = "http://127.0.0.1:${server.address.port}",
            expectedDeviceId = "basket-01",
            deviceToken = "device-secret",
            connectTimeoutMillis = 500,
            readTimeoutMillis = 500,
            staleAfterMillis = staleAfterMillis,
        ),
        nowMillis = nowMillis,
    )

    private fun server(handler: (com.sun.net.httpserver.HttpExchange) -> Unit): HttpServer =
        HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/api/v1/basket/status") { exchange -> handler(exchange) }
            createContext("/api/v1/basket/commands") { exchange -> handler(exchange) }
            start()
        }

    private fun statusEnvelope(lock: String = "LOCKED") =
        """
        {
          "success": true,
          "data": {
            "status": {
              "protocolVersion": 1,
              "deviceId": "basket-01",
              "firmwareVersion": "0.1.0",
              "sequence": 42,
              "uptimeMs": 1234567,
              "door": "CLOSED",
              "lock": "$lock",
              "sensor": "OK",
              "weightGrams": 1040,
              "loadState": "OK"
            }
          }
        }
        """.trimIndent()

    private fun commandEnvelope(requestId: String, lock: String): String {
        val status = JSONObject(statusEnvelope(lock))
            .getJSONObject("data")
            .getJSONObject("status")
        return JSONObject()
            .put("success", true)
            .put(
                "data",
                JSONObject()
                    .put("requestId", requestId)
                    .put("status", status),
            )
            .toString()
    }

    private fun respond(
        exchange: com.sun.net.httpserver.HttpExchange,
        body: String,
        statusCode: Int = 200,
    ) {
        val bytes = body.toByteArray(Charsets.UTF_8)
        exchange.responseHeaders.add("Content-Type", "application/json")
        exchange.sendResponseHeaders(statusCode, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }
}
