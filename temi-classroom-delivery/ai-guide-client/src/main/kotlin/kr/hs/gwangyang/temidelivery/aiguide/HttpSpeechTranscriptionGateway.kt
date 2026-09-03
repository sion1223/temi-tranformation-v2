package kr.hs.gwangyang.temidelivery.aiguide

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class HttpSpeechTranscriptionGateway(
    private val config: SupplyGuideClientConfig,
    private val connectTimeoutMillis: Int = 5_000,
    private val readTimeoutMillis: Int = 40_000,
) : SpeechTranscriptionGateway {
    private val endpoint = URL("${config.baseUrl.trimEnd('/')}/api/v1/transcriptions")

    override suspend fun transcribeWav(wavBytes: ByteArray): Result<SpeechTranscript> =
        withContext(Dispatchers.IO) {
            runCatching {
                require(wavBytes.size in MIN_WAV_BYTES..MAX_WAV_BYTES) {
                    "음성 데이터 크기가 올바르지 않습니다."
                }
                val connection = (endpoint.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = connectTimeoutMillis
                    readTimeout = readTimeoutMillis
                    useCaches = false
                    instanceFollowRedirects = false
                    setRequestProperty("Content-Type", "audio/wav")
                    setRequestProperty("Accept", "application/json")
                    if (config.clientToken.isNotBlank()) {
                        setRequestProperty("Authorization", "Bearer ${config.clientToken}")
                    }
                    setFixedLengthStreamingMode(wavBytes.size)
                }
                try {
                    connection.outputStream.use { it.write(wavBytes) }
                    val statusCode = connection.responseCode
                    val stream = if (statusCode in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream
                    }
                    val responseBody = stream?.use(::readLimitedUtf8).orEmpty()
                    if (statusCode !in 200..299) throw parseServiceError(statusCode, responseBody)
                    parseTranscript(responseBody)
                } finally {
                    connection.disconnect()
                }
            }.onFailure { cause ->
                if (cause is CancellationException) throw cause
            }
        }

    internal fun parseTranscript(responseBody: String): SpeechTranscript {
        val root = JSONObject(responseBody)
        require(root.optBoolean("success")) { "음성 인식 서버 응답이 실패 상태입니다." }
        val data = root.getJSONObject("data")
        val text = data.getString("text").trim()
        require(text.isNotEmpty()) { "음성 인식 결과가 비어 있습니다." }
        val durationMs = data.getInt("durationMs")
        require(durationMs in 1..MAX_DURATION_MILLIS) { "음성 길이가 올바르지 않습니다." }
        return SpeechTranscript(
            text = text,
            model = data.getString("model").trim().also {
                require(it.isNotEmpty()) { "음성 인식 모델 정보가 비어 있습니다." }
            },
            durationMs = durationMs,
            warning = data.optNullableString("warning"),
        )
    }

    private fun parseServiceError(statusCode: Int, responseBody: String): Exception = runCatching {
        val error = JSONObject(responseBody).getJSONObject("error")
        SupplyGuideServiceException(
            statusCode = statusCode,
            errorCode = error.optString("code", "http_error"),
            message = error.optString("message", "음성 인식 서버 오류가 발생했습니다."),
        )
    }.getOrElse {
        SupplyGuideServiceException(
            statusCode = statusCode,
            errorCode = "http_error",
            message = "음성 인식 서버가 HTTP $statusCode 오류를 반환했습니다.",
        )
    }

    private fun readLimitedUtf8(input: InputStream): String {
        val buffer = ByteArray(4 * 1024)
        val output = java.io.ByteArrayOutputStream()
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            if (output.size() + count > MAX_RESPONSE_BYTES) error("음성 인식 서버 응답이 너무 큽니다.")
            output.write(buffer, 0, count)
        }
        return output.toByteArray().toString(Charsets.UTF_8)
    }

    private fun JSONObject.optNullableString(name: String): String? {
        if (isNull(name)) return null
        return optString(name, "").trim().ifBlank { null }
    }

    private companion object {
        const val MIN_WAV_BYTES = 45
        const val MAX_WAV_BYTES = 700 * 1024
        const val MAX_RESPONSE_BYTES = 64 * 1024
        const val MAX_DURATION_MILLIS = 20_000
    }
}
