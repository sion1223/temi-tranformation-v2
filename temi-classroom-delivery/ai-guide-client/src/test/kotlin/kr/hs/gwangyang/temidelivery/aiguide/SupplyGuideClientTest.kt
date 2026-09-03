package kr.hs.gwangyang.temidelivery.aiguide

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.InetSocketAddress

class SupplyGuideClientTest {
    @Test
    fun `config parser reads an enabled backend`() {
        val config = SupplyGuideClientFactory.parseConfig(
            JSONObject(
                """
                {
                  "schemaVersion": 1,
                  "enabled": true,
                  "baseUrl": "http://192.168.0.10:8787/",
                  "clientToken": "classroom"
                }
                """.trimIndent(),
            ),
        )

        assertTrue(config.enabled)
        assertEquals("http://192.168.0.10:8787/", config.baseUrl)
        assertEquals("classroom", config.clientToken)
    }

    @Test
    fun `HTTP client maps legacy OpenAI source to Luna and sends its token`() = runBlocking {
        var authorization: String? = null
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/api/v1/guides") { exchange ->
                authorization = exchange.requestHeaders.getFirst("Authorization")
                exchange.requestBody.use { it.readBytes() }
                val response = """
                    {
                      "success": true,
                      "data": {
                        "itemId": "science-kit",
                        "itemName": "과학 실험 키트",
                        "explanation": "선생님의 안내에 따라 사용하세요.",
                        "source": "openai",
                        "model": "gpt-5.6-luna",
                        "warning": null
                      }
                    }
                """.trimIndent().toByteArray()
                exchange.responseHeaders.add("Content-Type", "application/json")
                exchange.sendResponseHeaders(200, response.size.toLong())
                exchange.responseBody.use { it.write(response) }
            }
            start()
        }

        try {
            val gateway = HttpSupplyGuideGateway(
                SupplyGuideClientConfig(
                    enabled = true,
                    baseUrl = "http://127.0.0.1:${server.address.port}",
                    clientToken = "robot-token",
                ),
            )

            val result = gateway.explain("science-kit").getOrThrow()

            assertEquals("Bearer robot-token", authorization)
            assertEquals(SupplyGuideSource.LUNA, result.source)
            assertEquals("gpt-5.6-luna", result.model)
            assertEquals("선생님의 안내에 따라 사용하세요.", result.explanation)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `guide parser distinguishes NVIDIA NIM and Luna sources`() {
        val gateway = HttpSupplyGuideGateway(
            SupplyGuideClientConfig(
                enabled = true,
                baseUrl = "http://127.0.0.1:8787",
                clientToken = "",
            ),
        )

        val nim = gateway.parseGuide(guideResponse("nvidia_nim", "deepseek-ai/deepseek-v4-flash-0731"))
        val luna = gateway.parseGuide(guideResponse("luna", "gpt-5.6-luna"))

        assertEquals(SupplyGuideSource.NVIDIA_NIM, nim.source)
        assertEquals(SupplyGuideSource.LUNA, luna.source)
    }

    @Test
    fun `school answer client sends a role question and parses teacher matches`() = runBlocking {
        var authorization: String? = null
        var requestQuestion: String? = null
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/api/v1/school/answers") { exchange ->
                authorization = exchange.requestHeaders.getFirst("Authorization")
                requestQuestion = JSONObject(exchange.requestBody.bufferedReader().use { it.readText() })
                    .getString("question")
                val response = """
                    {
                      "success": true,
                      "data": {
                        "answer": "진로 상담은 김진로 선생님을 찾아가세요.",
                        "source": "nvidia_nim",
                        "model": "deepseek-ai/deepseek-v4-flash-0731",
                        "warning": null,
                        "matches": [
                          {
                            "id": "career-counselor",
                            "name": "김진로",
                            "title": "교사",
                            "department": "진로상담부",
                            "location": "진로상담실",
                            "responsibilities": ["진로 상담", "진학 상담"]
                          }
                        ]
                      }
                    }
                """.trimIndent().toByteArray()
                exchange.responseHeaders.add("Content-Type", "application/json")
                exchange.sendResponseHeaders(200, response.size.toLong())
                exchange.responseBody.use { it.write(response) }
            }
            start()
        }

        try {
            val gateway = HttpSchoolAnswerGateway(
                SupplyGuideClientConfig(
                    enabled = true,
                    baseUrl = "http://127.0.0.1:${server.address.port}",
                    clientToken = "robot-token",
                ),
            )

            val result = gateway.answer("진로 상담은 어떤 선생님을 찾아야 해?").getOrThrow()

            assertEquals("Bearer robot-token", authorization)
            assertEquals("진로 상담은 어떤 선생님을 찾아야 해?", requestQuestion)
            assertEquals(SupplyGuideSource.NVIDIA_NIM, result.source)
            assertEquals("김진로", result.matches.single().name)
            assertEquals(listOf("진로 상담", "진학 상담"), result.matches.single().responsibilities)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `speech transcription client sends WAV and parses transcript`() = runBlocking {
        var authorization: String? = null
        var contentType: String? = null
        var audioSize = 0
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/api/v1/transcriptions") { exchange ->
                authorization = exchange.requestHeaders.getFirst("Authorization")
                contentType = exchange.requestHeaders.getFirst("Content-Type")
                audioSize = exchange.requestBody.use { it.readBytes().size }
                val response = """
                    {
                      "success": true,
                      "data": {
                        "text": "진로 상담 선생님을 찾아 주세요.",
                        "model": "gpt-transcribe",
                        "durationMs": 850,
                        "warning": null
                      }
                    }
                """.trimIndent().toByteArray()
                exchange.responseHeaders.add("Content-Type", "application/json")
                exchange.sendResponseHeaders(200, response.size.toLong())
                exchange.responseBody.use { it.write(response) }
            }
            start()
        }

        try {
            val gateway = HttpSpeechTranscriptionGateway(
                SupplyGuideClientConfig(
                    enabled = true,
                    baseUrl = "http://127.0.0.1:${server.address.port}",
                    clientToken = "robot-token",
                ),
            )
            val wav = ByteArray(144) { index -> index.toByte() }

            val result = gateway.transcribeWav(wav).getOrThrow()

            assertEquals("Bearer robot-token", authorization)
            assertEquals("audio/wav", contentType)
            assertEquals(wav.size, audioSize)
            assertEquals("진로 상담 선생님을 찾아 주세요.", result.text)
            assertEquals("gpt-transcribe", result.model)
            assertEquals(850, result.durationMs)
        } finally {
            server.stop(0)
        }
    }

    private fun guideResponse(source: String, model: String): String = """
        {
          "success": true,
          "data": {
            "itemId": "science-kit",
            "itemName": "과학 실험 키트",
            "explanation": "안전하게 사용하세요.",
            "source": "$source",
            "model": "$model",
            "warning": null
          }
        }
    """.trimIndent()
}
