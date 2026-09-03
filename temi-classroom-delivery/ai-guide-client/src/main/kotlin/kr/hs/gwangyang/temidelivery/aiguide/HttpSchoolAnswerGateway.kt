package kr.hs.gwangyang.temidelivery.aiguide

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class HttpSchoolAnswerGateway(
    private val config: SupplyGuideClientConfig,
    private val connectTimeoutMillis: Int = 5_000,
    private val readTimeoutMillis: Int = 25_000,
) : SchoolAnswerGateway {
    private val endpoint = URL("${config.baseUrl.trimEnd('/')}/api/v1/school/answers")

    override suspend fun answer(question: String): Result<SchoolAnswer> =
        withContext(Dispatchers.IO) {
            runCatching {
                val normalizedQuestion = question.trim()
                require(normalizedQuestion.isNotEmpty()) { "학교 안내 질문을 입력해 주세요." }
                require(normalizedQuestion.length <= 300) { "학교 안내 질문은 300자 이하여야 합니다." }
                val requestBody = JSONObject()
                    .put("question", normalizedQuestion)
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
                    parseAnswer(responseBody)
                } finally {
                    connection.disconnect()
                }
            }.onFailure { cause ->
                if (cause is CancellationException) throw cause
            }
        }

    internal fun parseAnswer(responseBody: String): SchoolAnswer {
        val root = JSONObject(responseBody)
        require(root.optBoolean("success")) { "학교 안내 서버 응답이 실패 상태입니다." }
        val data = root.getJSONObject("data")
        val answer = data.getString("answer").trim()
        require(answer.isNotEmpty()) { "학교 안내 답변이 비어 있습니다." }
        val matches = data.optJSONArray("matches") ?: JSONArray()
        return SchoolAnswer(
            answer = answer,
            source = data.getString("source").toSupplyGuideSource(),
            model = data.optNullableString("model"),
            warning = data.optNullableString("warning"),
            matches = buildList {
                for (index in 0 until matches.length()) {
                    val match = matches.getJSONObject(index)
                    add(
                        TeacherMatch(
                            id = match.getString("id"),
                            name = match.getString("name"),
                            title = match.optString("title", "교직원"),
                            department = match.optNullableString("department"),
                            location = match.optNullableString("location"),
                            responsibilities = match.optJSONArray("responsibilities").toStringList(),
                        ),
                    )
                }
            },
        )
    }

    private fun parseServiceError(statusCode: Int, responseBody: String): Exception = runCatching {
        val error = JSONObject(responseBody).getJSONObject("error")
        SupplyGuideServiceException(
            statusCode = statusCode,
            errorCode = error.optString("code", "http_error"),
            message = error.optString("message", "학교 안내 서버 오류가 발생했습니다."),
        )
    }.getOrElse {
        SupplyGuideServiceException(
            statusCode = statusCode,
            errorCode = "http_error",
            message = "학교 안내 서버가 HTTP $statusCode 오류를 반환했습니다.",
        )
    }

    private fun readLimitedUtf8(input: InputStream): String {
        val limit = 128 * 1024
        val buffer = ByteArray(4 * 1024)
        val output = ArrayList<Byte>(buffer.size)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            if (output.size + count > limit) error("학교 안내 서버 응답이 너무 큽니다.")
            repeat(count) { index -> output += buffer[index] }
        }
        return output.toByteArray().toString(Charsets.UTF_8)
    }

    private fun String.toSupplyGuideSource(): SupplyGuideSource = when (this) {
        "nvidia_nim" -> SupplyGuideSource.NVIDIA_NIM
        "luna", "openai" -> SupplyGuideSource.LUNA
        "teacher_fallback" -> SupplyGuideSource.TEACHER_FALLBACK
        else -> error("알 수 없는 학교 안내 출처입니다.")
    }

    private fun JSONObject.optNullableString(name: String): String? {
        if (isNull(name)) return null
        return optString(name, "").trim().ifBlank { null }
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                optString(index, "").trim().takeIf(String::isNotEmpty)?.let(::add)
            }
        }
    }
}
