package kr.hs.gwangyang.temidelivery.aiguide

import android.content.Context
import org.json.JSONObject

object SupplyGuideClientFactory {
    fun fromAsset(
        context: Context,
        assetName: String = "ai_guide_config.json",
        localAssetName: String = "ai_guide_config.local.json",
    ): SupplyGuideClientBundle {
        val config = runCatching {
            val selectedAsset = context.assets.list("")
                ?.firstOrNull { it == localAssetName }
                ?: assetName
            val json = context.assets.open(selectedAsset).bufferedReader(Charsets.UTF_8).use { it.readText() }
            parseConfig(JSONObject(json))
        }.getOrElse { error ->
            val message = error.message ?: "AI 안내 설정을 읽지 못했습니다."
            return disabledBundle(message)
        }

        if (!config.enabled) {
            return disabledBundle("AI 안내 서버가 꺼져 있습니다. ai_guide_config.json을 설정해 주세요.")
        }
        return runCatching {
            SupplyGuideClientBundle(
                gateway = HttpSupplyGuideGateway(config),
                schoolAnswerGateway = HttpSchoolAnswerGateway(config),
                speechTranscriptionGateway = HttpSpeechTranscriptionGateway(config),
                enabled = true,
                statusMessage = "AI 안내 서버 사용 가능",
            )
        }.getOrElse { error ->
            disabledBundle(error.message ?: "AI 안내 서버 주소가 올바르지 않습니다.")
        }
    }

    internal fun parseConfig(json: JSONObject): SupplyGuideClientConfig {
        require(json.getInt("schemaVersion") == 1) { "지원하지 않는 AI 안내 설정입니다." }
        val baseUrl = json.getString("baseUrl").trim()
        require(baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) {
            "AI 안내 서버 주소는 http:// 또는 https://로 시작해야 합니다."
        }
        return SupplyGuideClientConfig(
            enabled = json.optBoolean("enabled", false),
            baseUrl = baseUrl,
            clientToken = json.optString("clientToken", "").trim(),
        )
    }

    private fun disabledBundle(message: String) = SupplyGuideClientBundle(
        gateway = DisabledSupplyGuideGateway(message),
        schoolAnswerGateway = DisabledSchoolAnswerGateway(message),
        speechTranscriptionGateway = DisabledSpeechTranscriptionGateway(message),
        enabled = false,
        statusMessage = message,
    )
}

private class DisabledSupplyGuideGateway(
    private val message: String,
) : SupplyGuideGateway {
    override suspend fun explain(itemId: String, question: String?): Result<SupplyGuide> =
        Result.failure(IllegalStateException(message))
}

private class DisabledSchoolAnswerGateway(
    private val message: String,
) : SchoolAnswerGateway {
    override suspend fun answer(question: String): Result<SchoolAnswer> =
        Result.failure(IllegalStateException(message))
}

private class DisabledSpeechTranscriptionGateway(
    private val message: String,
) : SpeechTranscriptionGateway {
    override suspend fun transcribeWav(wavBytes: ByteArray): Result<SpeechTranscript> =
        Result.failure(IllegalStateException(message))
}
