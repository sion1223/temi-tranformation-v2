package kr.hs.gwangyang.temidelivery

import android.content.Context
import androidx.core.content.edit
import kr.hs.gwangyang.temidelivery.basket.BasketClientBundle
import kr.hs.gwangyang.temidelivery.basket.BasketClientConfig
import kr.hs.gwangyang.temidelivery.basket.BasketClientFactory
import kr.hs.gwangyang.temidelivery.basket.DisabledBasketGateway
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val MAX_VOICE_RECORDING_MILLIS = 20_000L

enum class VoiceActivationMode {
    TAP_TO_TALK,
    HOLD_TO_TALK,
    TEMI_WAKE_WORD,
    ;

    companion object {
        fun fromStoredValue(value: String?): VoiceActivationMode =
            entries.firstOrNull { it.name == value } ?: TAP_TO_TALK
    }
}

data class FeatureSettings(
    val aiAssistantEnabled: Boolean = true,
    val speechOutputEnabled: Boolean = true,
    val voiceInputEnabled: Boolean = true,
    val voiceActivationMode: VoiceActivationMode = VoiceActivationMode.TAP_TO_TALK,
    val keepScreenOnEnabled: Boolean = true,
    val immersiveModeEnabled: Boolean = true,
    val blockBackEnabled: Boolean = true,
    val deliveryControlsEnabled: Boolean = true,
    val basketIntegrationEnabled: Boolean = false,
    val basketBaseUrl: String = "http://192.168.104.252",
    val basketDeviceId: String = "basket-01",
    val basketDeviceToken: String = "",
) {
    fun toBasketClientConfig() = BasketClientConfig(
        enabled = basketIntegrationEnabled,
        baseUrl = basketBaseUrl.trim().trimEnd('/'),
        expectedDeviceId = basketDeviceId.trim(),
        deviceToken = basketDeviceToken.trim(),
    )
}

class FeatureSettingsStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(read())
    val settings: StateFlow<FeatureSettings> = _settings.asStateFlow()

    fun current(): FeatureSettings = _settings.value

    fun save(settings: FeatureSettings) {
        preferences.edit {
            putBoolean(KEY_AI_ASSISTANT, settings.aiAssistantEnabled)
            putBoolean(KEY_SPEECH_OUTPUT, settings.speechOutputEnabled)
            putBoolean(KEY_VOICE_INPUT, settings.voiceInputEnabled)
            putString(KEY_VOICE_MODE, settings.voiceActivationMode.name)
            putBoolean(KEY_KEEP_SCREEN_ON, settings.keepScreenOnEnabled)
            putBoolean(KEY_IMMERSIVE, settings.immersiveModeEnabled)
            putBoolean(KEY_BLOCK_BACK, settings.blockBackEnabled)
            putBoolean(KEY_DELIVERY_CONTROLS, settings.deliveryControlsEnabled)
            putBoolean(KEY_BASKET_ENABLED, settings.basketIntegrationEnabled)
            putString(KEY_BASKET_BASE_URL, settings.basketBaseUrl.trim())
            putString(KEY_BASKET_DEVICE_ID, settings.basketDeviceId.trim())
            putString(KEY_BASKET_TOKEN, settings.basketDeviceToken.trim())
        }
        _settings.value = settings.copy(
            basketBaseUrl = settings.basketBaseUrl.trim(),
            basketDeviceId = settings.basketDeviceId.trim(),
            basketDeviceToken = settings.basketDeviceToken.trim(),
        )
    }

    private fun read() = FeatureSettings(
        aiAssistantEnabled = preferences.getBoolean(KEY_AI_ASSISTANT, true),
        speechOutputEnabled = preferences.getBoolean(KEY_SPEECH_OUTPUT, true),
        voiceInputEnabled = preferences.getBoolean(KEY_VOICE_INPUT, true),
        voiceActivationMode = VoiceActivationMode.fromStoredValue(
            preferences.getString(KEY_VOICE_MODE, null),
        ),
        keepScreenOnEnabled = preferences.getBoolean(KEY_KEEP_SCREEN_ON, true),
        immersiveModeEnabled = preferences.getBoolean(KEY_IMMERSIVE, true),
        blockBackEnabled = preferences.getBoolean(KEY_BLOCK_BACK, true),
        deliveryControlsEnabled = preferences.getBoolean(KEY_DELIVERY_CONTROLS, true),
        basketIntegrationEnabled = preferences.getBoolean(KEY_BASKET_ENABLED, false),
        basketBaseUrl = preferences.getString(
            KEY_BASKET_BASE_URL,
            "http://192.168.104.252",
        ).orEmpty(),
        basketDeviceId = preferences.getString(KEY_BASKET_DEVICE_ID, "basket-01").orEmpty(),
        basketDeviceToken = preferences.getString(KEY_BASKET_TOKEN, "").orEmpty(),
    )

    private companion object {
        const val PREFERENCES_NAME = "temi_mvp_feature_settings"
        const val KEY_AI_ASSISTANT = "ai_assistant_enabled"
        const val KEY_SPEECH_OUTPUT = "speech_output_enabled"
        const val KEY_VOICE_INPUT = "voice_input_enabled"
        const val KEY_VOICE_MODE = "voice_activation_mode"
        const val KEY_KEEP_SCREEN_ON = "keep_screen_on_enabled"
        const val KEY_IMMERSIVE = "immersive_mode_enabled"
        const val KEY_BLOCK_BACK = "block_back_enabled"
        const val KEY_DELIVERY_CONTROLS = "delivery_controls_enabled"
        const val KEY_BASKET_ENABLED = "basket_integration_enabled"
        const val KEY_BASKET_BASE_URL = "basket_base_url"
        const val KEY_BASKET_DEVICE_ID = "basket_device_id"
        const val KEY_BASKET_TOKEN = "basket_device_token"
    }
}

class RuntimeBasketClient(initialSettings: FeatureSettings) {
    private val _bundle = MutableStateFlow(buildBundle(initialSettings))
    val bundle: StateFlow<BasketClientBundle> = _bundle.asStateFlow()

    fun current(): BasketClientBundle = _bundle.value

    fun validate(settings: FeatureSettings): Result<Unit> = runCatching {
        settings.toBasketClientConfig()
    }.map { Unit }

    fun apply(settings: FeatureSettings) {
        _bundle.value = buildBundle(settings)
    }

    private fun buildBundle(settings: FeatureSettings): BasketClientBundle = runCatching {
        BasketClientFactory.fromConfig(settings.toBasketClientConfig())
    }.getOrElse { error ->
        val message = error.message ?: "Arduino 바구니 설정이 올바르지 않습니다."
        BasketClientBundle(
            gateway = DisabledBasketGateway(message),
            enabled = false,
            statusMessage = message,
        )
    }
}
