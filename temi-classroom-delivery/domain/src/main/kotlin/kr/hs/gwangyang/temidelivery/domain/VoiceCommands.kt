package kr.hs.gwangyang.temidelivery.domain

import java.util.Locale

/** Robot actions that may be triggered by an explicitly matched voice command. */
sealed interface VoiceRobotCommand {
    data object GoToHomeBase : VoiceRobotCommand

    data class GoToSavedLocation(val location: String) : VoiceRobotCommand

    data object FollowMe : VoiceRobotCommand

    data object StopMovement : VoiceRobotCommand
}

sealed interface VoiceCommandInterpretation {
    data class Matched(val command: VoiceRobotCommand) : VoiceCommandInterpretation

    data class UnknownDestination(val requestedName: String) : VoiceCommandInterpretation

    data object NotACommand : VoiceCommandInterpretation
}

sealed interface VoiceCommandResult {
    data object NotACommand : VoiceCommandResult

    data class Executed(
        val command: VoiceRobotCommand,
        val message: String,
        val warning: String? = null,
    ) : VoiceCommandResult

    data class Rejected(val message: String) : VoiceCommandResult

    data class Failed(val message: String) : VoiceCommandResult
}

/**
 * Deterministic command parser. Only explicit imperative phrases are accepted so a normal
 * school-assistant question cannot accidentally move the robot.
 */
class VoiceCommandInterpreter {
    fun interpret(
        transcript: String,
        savedLocations: List<String>,
    ): VoiceCommandInterpretation {
        val normalized = transcript.normalizedSpeech()
        if (normalized.isEmpty()) return VoiceCommandInterpretation.NotACommand

        if (normalized in STOP_COMMANDS) {
            return VoiceCommandInterpretation.Matched(VoiceRobotCommand.StopMovement)
        }
        if (normalized in FOLLOW_COMMANDS) {
            return VoiceCommandInterpretation.Matched(VoiceRobotCommand.FollowMe)
        }
        if (normalized in DIRECT_HOME_COMMANDS) {
            return VoiceCommandInterpretation.Matched(VoiceRobotCommand.GoToHomeBase)
        }

        val requestedLocation = extractNavigationTarget(normalized)
            ?: return VoiceCommandInterpretation.NotACommand
        if (requestedLocation in HOME_BASE_ALIASES) {
            return VoiceCommandInterpretation.Matched(VoiceRobotCommand.GoToHomeBase)
        }

        val canonicalLocation = savedLocations.firstOrNull {
            it.normalizedSpeech() == requestedLocation
        }
        return if (canonicalLocation != null) {
            VoiceCommandInterpretation.Matched(
                VoiceRobotCommand.GoToSavedLocation(canonicalLocation),
            )
        } else {
            VoiceCommandInterpretation.UnknownDestination(requestedLocation)
        }
    }

    private fun extractNavigationTarget(normalized: String): String? {
        val englishPrefix = ENGLISH_NAVIGATION_PREFIXES.firstOrNull(normalized::startsWith)
        if (englishPrefix != null) {
            return normalized.removePrefix(englishPrefix).removePrefix("the").ifBlank { null }
        }

        val suffix = KOREAN_NAVIGATION_SUFFIXES.firstOrNull(normalized::endsWith) ?: return null
        return normalized.dropLast(suffix.length).ifBlank { null }
    }

    private fun String.normalizedSpeech(): String =
        lowercase(Locale.ROOT).filter(Char::isLetterOrDigit)

    private companion object {
        val STOP_COMMANDS = setOf(
            "멈춰",
            "멈춰줘",
            "멈춰주세요",
            "멈추세요",
            "정지",
            "정지해",
            "정지해줘",
            "정지해주세요",
            "정지하세요",
            "이동멈춰",
            "이동을멈춰",
            "주행정지",
            "그만",
            "스톱",
            "stop",
            "stopmoving",
            "halt",
        )

        val FOLLOW_COMMANDS = setOf(
            "따라와",
            "따라와줘",
            "따라와주세요",
            "따라오세요",
            "나를따라와",
            "나를따라와줘",
            "나를따라오세요",
            "저를따라와",
            "저를따라오세요",
            "따라가기시작",
            "followme",
            "startfollowingme",
        )

        val DIRECT_HOME_COMMANDS = setOf(
            "충전해",
            "충전해줘",
            "충전해주세요",
            "충전하러가",
            "충전하러가줘",
            "충전하러가주세요",
            "충전하러가세요",
            "홈베이스로복귀해",
            "홈베이스로복귀해줘",
            "홈베이스로돌아가",
            "홈베이스로돌아가줘",
            "returnhome",
            "returntohomebase",
            "gobacktohomebase",
        )

        val HOME_BASE_ALIASES = setOf(
            "홈베이스",
            "충전기",
            "충전소",
            "충전도크",
            "충전스테이션",
            "도킹스테이션",
            "homebase",
        )

        val ENGLISH_NAVIGATION_PREFIXES = listOf(
            "pleasemoveto",
            "pleasegoto",
            "moveto",
            "goto",
        )

        // Longest suffixes must be checked first.
        val KOREAN_NAVIGATION_SUFFIXES = listOf(
            "으로이동해주세요",
            "까지이동해주세요",
            "로이동해주세요",
            "에이동해주세요",
            "으로이동하세요",
            "까지이동하세요",
            "로이동하세요",
            "에이동하세요",
            "으로이동해줘",
            "까지이동해줘",
            "로이동해줘",
            "에이동해줘",
            "으로가주세요",
            "까지가주세요",
            "로가주세요",
            "에가주세요",
            "으로가세요",
            "까지가세요",
            "로가세요",
            "에가세요",
            "으로돌아가줘",
            "로돌아가줘",
            "으로복귀해줘",
            "로복귀해줘",
            "으로돌아가세요",
            "로돌아가세요",
            "으로복귀하세요",
            "로복귀하세요",
            "으로이동해",
            "까지이동해",
            "로이동해",
            "에이동해",
            "으로가줘",
            "까지가줘",
            "로가줘",
            "에가줘",
            "으로돌아가",
            "로돌아가",
            "으로복귀해",
            "로복귀해",
            "으로가",
            "까지가",
            "로가",
            "에가",
            "이동해주세요",
            "이동하세요",
            "이동해줘",
            "가주세요",
            "가세요",
            "이동해",
            "가줘",
        )
    }
}

/** Executes a parsed command while keeping the delivery mission state consistent. */
class HandleVoiceCommandUseCase(
    private val interpreter: VoiceCommandInterpreter,
    private val deliveryCoordinator: DeliveryCoordinator,
    private val robotGateway: RobotGateway,
) {
    operator fun invoke(
        transcript: String,
        movementControlsEnabled: Boolean,
    ): VoiceCommandResult {
        var interpretation = interpreter.interpret(transcript, robotGateway.savedLocations.value)
        if (interpretation is VoiceCommandInterpretation.NotACommand) {
            return VoiceCommandResult.NotACommand
        }

        val initiallyMatched = interpretation as? VoiceCommandInterpretation.Matched
        val isSafetyStop = initiallyMatched?.command == VoiceRobotCommand.StopMovement
        if (!isSafetyStop && !movementControlsEnabled) {
            return VoiceCommandResult.Rejected(
                "음성 주행 명령을 사용하려면 기능 설정에서 배부·주행 제어 기능을 켜 주세요.",
            )
        }

        if (interpretation is VoiceCommandInterpretation.UnknownDestination) {
            val refreshFailure = runCatching { robotGateway.refreshRobotState() }.exceptionOrNull()
            if (refreshFailure != null) {
                return VoiceCommandResult.Failed(
                    "temi 저장 위치를 확인하지 못했습니다: ${refreshFailure.safeMessage()}",
                )
            }
            if (!robotGateway.isReady.value) {
                return VoiceCommandResult.Rejected("temi 연결이 준비된 뒤 다시 말씀해 주세요.")
            }
            interpretation = interpreter.interpret(transcript, robotGateway.savedLocations.value)
            if (interpretation is VoiceCommandInterpretation.UnknownDestination) {
                return VoiceCommandResult.Rejected(
                    "말씀하신 목적지가 temi 지도에 저장되어 있지 않습니다.",
                )
            }
        }

        val command = (interpretation as? VoiceCommandInterpretation.Matched)?.command
            ?: return VoiceCommandResult.NotACommand
        if (command == VoiceRobotCommand.StopMovement) return stopMovement()

        val refreshFailure = runCatching { robotGateway.refreshRobotState() }.exceptionOrNull()
        if (refreshFailure != null) {
            return VoiceCommandResult.Failed(
                "temi 상태를 확인하지 못했습니다: ${refreshFailure.safeMessage()}",
            )
        }
        if (!robotGateway.isReady.value) {
            return VoiceCommandResult.Rejected("temi 연결이 준비된 뒤 다시 말씀해 주세요.")
        }
        if (deliveryCoordinator.state.value.phase == DeliveryPhase.EMERGENCY_STOPPED) {
            return VoiceCommandResult.Rejected(
                "긴급 정지 상태입니다. 주변 안전을 확인하고 화면에서 새 작업 준비를 누른 뒤 이동해 주세요.",
            )
        }

        return executeMotion(command)
    }

    private fun stopMovement(): VoiceCommandResult {
        val stopFailure = runCatching {
            if (deliveryCoordinator.state.value.phase.isVoiceControllableMission()) {
                deliveryCoordinator.emergencyStop()
                val stateAfterStop = deliveryCoordinator.state.value
                check(stateAfterStop.phase == DeliveryPhase.EMERGENCY_STOPPED) {
                    stateAfterStop.failure?.description ?: "temi 정지 명령을 처리하지 못했습니다."
                }
            } else {
                robotGateway.stopMovement()
            }
        }.exceptionOrNull()

        if (stopFailure != null) {
            return VoiceCommandResult.Failed(
                "temi를 정지하지 못했습니다. 주변 안전을 확인해 주세요: ${stopFailure.safeMessage()}",
            )
        }

        val warning = speakWarning("이동을 정지했습니다.")
        return VoiceCommandResult.Executed(
            command = VoiceRobotCommand.StopMovement,
            message = "음성 정지 명령을 실행했습니다.",
            warning = warning,
        )
    }

    private fun executeMotion(command: VoiceRobotCommand): VoiceCommandResult {
        if (deliveryCoordinator.state.value.phase.requiresCancellationForVoiceMotion()) {
            deliveryCoordinator.cancel()
            val stateAfterCancel = deliveryCoordinator.state.value
            if (stateAfterCancel.phase != DeliveryPhase.CANCELLED) {
                val reason = stateAfterCancel.failure?.description
                    ?: "기존 배부 작업을 정지하지 못했습니다."
                return VoiceCommandResult.Failed(
                    "기존 작업을 안전하게 정지하지 못해 음성 이동 명령을 실행하지 않았습니다: $reason",
                )
            }
        } else {
            val stopFailure = runCatching { robotGateway.stopMovement() }.exceptionOrNull()
            if (stopFailure != null) {
                return VoiceCommandResult.Failed(
                    "기존 움직임을 안전하게 정지하지 못해 새 음성 명령을 실행하지 않았습니다: " +
                        stopFailure.safeMessage(),
                )
            }
        }

        val confirmation = when (command) {
            VoiceRobotCommand.GoToHomeBase -> "홈베이스로 이동합니다."
            is VoiceRobotCommand.GoToSavedLocation -> "${command.location} 위치로 이동합니다."
            VoiceRobotCommand.FollowMe -> "따라가기 모드를 시작합니다."
            VoiceRobotCommand.StopMovement -> error("Stop is handled before motion execution")
        }
        val speechWarning = speakWarning(confirmation)
        val movementFailure = runCatching {
            when (command) {
                VoiceRobotCommand.GoToHomeBase -> robotGateway.navigateTo(
                    destination = Destination.SavedLocation(
                        name = HOME_BASE_LOCATION,
                        displayName = "홈베이스",
                    ),
                    speed = DeliverySpeed.VERY_SLOW,
                    highAccuracyArrival = true,
                )

                is VoiceRobotCommand.GoToSavedLocation -> robotGateway.navigateTo(
                    destination = Destination.SavedLocation(command.location),
                    speed = DeliverySpeed.VERY_SLOW,
                    highAccuracyArrival = true,
                )

                VoiceRobotCommand.FollowMe -> robotGateway.followMe(DeliverySpeed.VERY_SLOW)
                VoiceRobotCommand.StopMovement -> error("Stop is handled before motion execution")
            }
        }.exceptionOrNull()

        if (movementFailure != null) {
            return VoiceCommandResult.Failed(
                "음성 명령을 temi에 보내지 못했습니다: ${movementFailure.safeMessage()}",
            )
        }
        return VoiceCommandResult.Executed(
            command = command,
            message = "음성 명령을 보냈습니다: $confirmation",
            warning = speechWarning,
        )
    }

    private fun speakWarning(message: String): String? =
        runCatching { robotGateway.speak(message) }
            .exceptionOrNull()
            ?.let { "명령은 실행했지만 음성 확인을 출력하지 못했습니다: ${it.safeMessage()}" }

    private fun DeliveryPhase.requiresCancellationForVoiceMotion(): Boolean =
        this == DeliveryPhase.NAVIGATING ||
            this == DeliveryPhase.WAITING_FOR_PICKUP ||
            this == DeliveryPhase.RETURNING ||
            this == DeliveryPhase.FAILED

    private fun DeliveryPhase.isVoiceControllableMission(): Boolean =
        requiresCancellationForVoiceMotion() || this == DeliveryPhase.EMERGENCY_STOPPED

    private fun Throwable.safeMessage(): String = message ?: javaClass.simpleName

    private companion object {
        const val HOME_BASE_LOCATION = "home base"
    }
}
