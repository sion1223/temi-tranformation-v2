package kr.hs.gwangyang.temidelivery

import com.robotemi.sdk.Robot
import com.robotemi.sdk.SttLanguage
import com.robotemi.sdk.voice.WakeupOrigin

/**
 * Uses temi's built-in wake word and ASR as an optional, no-audio-upload activation path.
 * Registering a listener does not change the robot-wide wakeup setting.
 */
class TemiVoiceActivationController(
    private val robot: Robot,
    private val onTranscript: (String) -> Unit,
    private val onStatus: (String) -> Unit,
) : Robot.WakeupWordListener, Robot.AsrListener {
    private var registered = false
    private var awaitingAnswer = false

    fun start() {
        if (registered) return
        registered = true
        robot.addWakeupWordListener(this)
        robot.addAsrListener(this)
        onStatus("'Hey temi' 호출을 기다리는 중입니다. 이동 명령이나 질문을 말할 수 있습니다.")
    }

    fun stop() {
        if (!registered) return
        robot.removeWakeupWordListener(this)
        robot.removeAsrListener(this)
        if (awaitingAnswer) runCatching { robot.finishConversation() }
        awaitingAnswer = false
        registered = false
    }

    fun askNow(): Result<Unit> = runCatching {
        check(registered) { "temi 호출어 모드가 활성화되지 않았습니다." }
        check(robot.isReady) { "temi SDK 연결을 기다리는 중입니다." }
        awaitingAnswer = true
        onStatus("temi가 명령 또는 질문을 듣고 있습니다.")
        robot.askQuestion("무엇을 도와드릴까요?")
    }.onFailure { error ->
        awaitingAnswer = false
        onStatus(error.message ?: "temi 음성 질문을 시작하지 못했습니다.")
    }

    override fun onWakeupWord(wakeupWord: String, direction: Int, origin: WakeupOrigin) {
        if (!registered || awaitingAnswer) return
        askNow()
    }

    override fun onAsrResult(asrResult: String, sttLanguage: SttLanguage) {
        if (!registered || !awaitingAnswer) return
        awaitingAnswer = false
        val normalized = asrResult.trim()
        runCatching { robot.finishConversation() }
        if (normalized.isEmpty()) {
            onStatus("음성을 알아듣지 못했습니다. 다시 불러 주세요.")
            return
        }
        onStatus("temi 내장 음성 인식 완료")
        onTranscript(normalized)
    }
}
