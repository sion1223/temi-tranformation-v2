package kr.hs.gwangyang.temidelivery.aiguide

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class HttpSupplyGuideGateway(
    private val config: SupplyGuideClientConfig,
    private val connectTimeoutMillis: Int = 5_000,
    private val readTimeoutMillis: Int = 20_000,
) : SupplyGuideGateway {
    private val endpoint = URL("${config.baseUrl.trimEnd('/')}/api/v1/guides")

    override suspend fun explain(itemId: String, question: String?): Result<SupplyGuide> =
        withContext(Dispatchers.IO) {
            runCatching {
                require(itemId.isNotBlank()) { "물품 ID가 비어 있습니다." }
                val requestBody = JSONObject()
                    .put("itemId", itemId.trim())
                    .apply {
                        if (!question.isNullOrBlank()) put("question", question.trim())
                    }
                    .toString()
                    .toByteArray(Charsets.UTF_8)

                val connection = (endpoint.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = connectTimeoutMillis
                    readTimeout = readTimeoutMillis
                    useCaches = false
                    instanceFollowRedirects = false
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                    if (config.clientToken.isNotBlank()) {
                        setRequestProperty("Authorization", "Bearer ${config.clientToken}")
                    }
                    setFixedLengthStreamingMode(requestBody.size)
                }

                try {
                    connection.outputStream.use { it.write(requestBody) }
                    val statusCode = connection.responseCode
                    val stream = if (statusCode in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream
                    }
                    val responseBody = stream?.use(::readLimitedUtf8).orEmpty()
                    if (statusCode !in 200..299) {
                        throw parseServiceError(statusCode, responseBody)
                    }
                    parseGuide(responseBody)
                } finally {
                    connection.disconnect()
                }
            }.onFailure { cause ->
                if (cause is CancellationException) throw cause
            }
        }

    internal fun parseGuide(responseBody: String): SupplyGuide {
        val root = JSONObject(responseBody)
        require(root.optBoolean("success")) { "AI 안내 서버 응답이 실패 상태입니다." }
        val data = root.getJSONObject("data")
        val explanation = data.getString("explanation").trim()
        require(explanation.isNotEmpty()) { "AI 안내 문장이 비어 있습니다." }
        return SupplyGuide(
            itemId = data.getString("itemId"),
            itemName = data.getString("itemName"),
            explanation = explanation,
            source = when (data.getString("source")) {
                "nvidia_nim" -> SupplyGuideSource.NVIDIA_NIM
                "luna", "openai" -> SupplyGuideSource.LUNA
                "teacher_fallback" -> SupplyGuideSource.TEACHER_FALLBACK
                else -> error("알 수 없는 AI 안내 출처입니다.")
            },
            model = data.optNullableString("model"),
            warning = data.optNullableString("warning"),
        )
    }

    private fun parseServiceError(statusCode: Int, responseBody: String): Exception {
        return runCatching {
            val error = JSONObject(responseBody).getJSONObject("error")
            SupplyGuideServiceException(
                statusCode = statusCode,
                errorCode = error.optString("code", "http_error"),
                message = error.optString("message", "AI 안내 서버 오류가 발생했습니다."),
            )
        }.getOrElse {
            SupplyGuideServiceException(
                statusCode = statusCode,
                errorCode = "http_error",
                message = "AI 안내 서버가 HTTP $statusCode 오류를 반환했습니다.",
            )
        }
    }

    private fun readLimitedUtf8(input: InputStream): String {
        val limit = 64 * 1024
        val buffer = ByteArray(4 * 1024)
        val output = ArrayList<Byte>(buffer.size)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            if (output.size + count > limit) error("AI 안내 서버 응답이 너무 큽니다.")
            repeat(count) { index -> output += buffer[index] }
        }
        return output.toByteArray().toString(Charsets.UTF_8)
    }

    private fun JSONObject.optNullableString(name: String): String? {
        if (isNull(name)) return null
        return optString(name, "").trim().ifBlank { null }
    }
}
