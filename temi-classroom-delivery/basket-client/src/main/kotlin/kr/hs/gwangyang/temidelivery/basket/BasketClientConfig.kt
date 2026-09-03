package kr.hs.gwangyang.temidelivery.basket

import android.content.Context
import org.json.JSONObject
import java.net.URL

data class BasketClientConfig(
    val enabled: Boolean,
    val baseUrl: String,
    val expectedDeviceId: String = "",
    val deviceToken: String = "",
    val connectTimeoutMillis: Int = 1_500,
    val readTimeoutMillis: Int = 2_000,
    val staleAfterMillis: Long = 3_000,
    val maxResponseBytes: Int = 64 * 1024,
) {
    init {
        if (enabled) {
            require(baseUrl.isNotBlank()) { "바구니 서버 주소가 비어 있습니다." }
            val parsed = runCatching { URL(baseUrl) }
                .getOrElse { error -> throw IllegalArgumentException("바구니 서버 주소가 올바르지 않습니다.", error) }
            require(parsed.protocol == "http" || parsed.protocol == "https") {
                "바구니 서버 주소는 http:// 또는 https://로 시작해야 합니다."
            }
            require(parsed.host.isNotBlank()) { "바구니 서버 주소에 호스트가 필요합니다." }
            require(parsed.userInfo == null) { "바구니 서버 주소에 사용자 정보는 넣을 수 없습니다." }
            require(parsed.query == null && parsed.ref == null) {
                "바구니 서버 기본 주소에는 query/fragment를 넣을 수 없습니다."
            }
            require(deviceToken.isNotBlank()) {
                "바구니 장치 토큰이 비어 있습니다. NVIDIA NIM 키와 별도로 설정해야 합니다."
            }
            require(expectedDeviceId.isNotBlank()) {
                "활성화된 바구니 설정에는 예상 장치 ID가 필요합니다."
            }
            require(deviceToken.none { it == '\r' || it == '\n' }) {
                "바구니 장치 토큰에 줄바꿈을 넣을 수 없습니다."
            }
            require(expectedDeviceId.none { it == '\r' || it == '\n' }) {
                "바구니 장치 ID에 줄바꿈을 넣을 수 없습니다."
            }
            require(connectTimeoutMillis in 100..60_000) {
                "connectTimeoutMillis는 100~60000 사이여야 합니다."
            }
            require(readTimeoutMillis in 100..60_000) {
                "readTimeoutMillis는 100~60000 사이여야 합니다."
            }
            require(staleAfterMillis in 250..300_000) {
                "staleAfterMillis는 250~300000 사이여야 합니다."
            }
            require(maxResponseBytes in 1_024..1_048_576) {
                "maxResponseBytes는 1024~1048576 사이여야 합니다."
            }
        }
    }
}

data class BasketClientBundle(
    val gateway: BasketGateway,
    val enabled: Boolean,
    val statusMessage: String,
)

object BasketClientFactory {
    fun fromAsset(
        context: Context,
        assetName: String = "basket_config.json",
    ): BasketClientBundle {
        val config = runCatching {
            val json = context.assets.open(assetName).bufferedReader(Charsets.UTF_8).use { it.readText() }
            parseConfig(JSONObject(json))
        }.getOrElse { error ->
            return disabledBundle(error.message ?: "바구니 설정을 읽지 못했습니다.")
        }
        return runCatching { fromConfig(config) }
            .getOrElse { error ->
                disabledBundle(error.message ?: "바구니 설정이 올바르지 않습니다.")
            }
    }

    fun fromConfig(config: BasketClientConfig): BasketClientBundle {
        if (!config.enabled) {
            return disabledBundle("바구니 연동이 꺼져 있습니다. basket_config.json을 설정해 주세요.")
        }
        return BasketClientBundle(
            gateway = HttpBasketGateway(config),
            enabled = true,
            statusMessage = "바구니 서버 연결 대기 중",
        )
    }

    fun parseConfig(json: JSONObject): BasketClientConfig {
        require(json.getInt("schemaVersion") == BASKET_PROTOCOL_VERSION) {
            "지원하지 않는 바구니 설정 스키마입니다."
        }
        val enabled = json.optBoolean("enabled", false)
        val baseUrl = json.optString("baseUrl", "").trim().trimEnd('/')
        val deviceId = json.optString("deviceId", "").trim()
        val deviceToken = json.optString("deviceToken", "").trim()
        return BasketClientConfig(
            enabled = enabled,
            baseUrl = baseUrl,
            expectedDeviceId = deviceId,
            deviceToken = deviceToken,
            connectTimeoutMillis = json.optInt("connectTimeoutMillis", 1_500),
            readTimeoutMillis = json.optInt("readTimeoutMillis", 2_000),
            staleAfterMillis = json.optLong("staleAfterMillis", 3_000),
            maxResponseBytes = json.optInt("maxResponseBytes", 64 * 1024),
        )
    }

    private fun disabledBundle(message: String) = BasketClientBundle(
        gateway = DisabledBasketGateway(message),
        enabled = false,
        statusMessage = message,
    )
}
