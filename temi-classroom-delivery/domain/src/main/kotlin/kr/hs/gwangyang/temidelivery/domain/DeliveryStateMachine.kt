package kr.hs.gwangyang.temidelivery.domain

import java.util.Locale
import kotlin.math.PI

/**
 * Pure, deterministic delivery mission state machine.
 *
 * It never talks to Android or temi directly. Every input returns commands for the adapter,
 * which makes route behavior testable without a physical robot.
 */
class DeliveryStateMachine {
    var state: DeliverySnapshot = DeliverySnapshot()
        private set

    @Synchronized
    fun start(
        route: DeliveryRoute,
        robotReady: Boolean,
        savedLocations: List<String>,
    ): DeliveryTransition {
        if (!state.phase.canStartMission()) {
            return transition(
                commands = listOf(
                    RobotCommand.Speak("현재 작업을 재시도하거나 종료한 뒤 새 배부를 시작해 주세요."),
                ),
            )
        }

        if (!robotReady) {
            return configurationError("temi 제어 서비스가 아직 준비되지 않았습니다.")
        }

        val validation = validateAndResolve(route, savedLocations)
        if (validation is RouteValidation.Invalid) {
            return configurationError(validation.message)
        }

        val resolvedRoute = (validation as RouteValidation.Valid).route
        val firstStop = resolvedRoute.stops.first()
        state = DeliverySnapshot(
            phase = DeliveryPhase.NAVIGATING,
            route = resolvedRoute,
            stopIndex = 0,
            message = "${firstStop.destination.displayName}(으)로 이동 중입니다.",
        )
        return transition(
            RobotCommand.Speak("${resolvedRoute.name}을 시작합니다. ${firstStop.recipient}에게 이동합니다."),
            navigateCommand(resolvedRoute, firstStop.destination),
        )
    }

    @Synchronized
    fun onNavigationEvent(event: NavigationEvent): DeliveryTransition {
        val route = state.route ?: return transition()
        val expectedDestination = when (state.phase) {
            DeliveryPhase.NAVIGATING -> state.currentStop?.destination
            DeliveryPhase.RETURNING -> route.returnDestination
            else -> null
        } ?: return transition()

        if (!event.matches(expectedDestination)) {
            return transition()
        }

        return when (event.status) {
            NavigationStatus.START,
            NavigationStatus.CALCULATING,
            NavigationStatus.GOING,
            NavigationStatus.REPOSING,
            -> navigationProgress(event.status, expectedDestination)

            NavigationStatus.COMPLETE -> navigationCompleted(route)
            NavigationStatus.ABORT -> navigationAborted(event)
            NavigationStatus.UNKNOWN -> transition()
        }
    }

    @Synchronized
    fun confirmPickup(): DeliveryTransition {
        if (state.phase != DeliveryPhase.WAITING_FOR_PICKUP) return transition()
        return moveToNextStopOrReturn("수령을 확인했습니다.")
    }

    @Synchronized
    fun skipCurrentStop(): DeliveryTransition {
        val canSkip = state.phase == DeliveryPhase.WAITING_FOR_PICKUP ||
            (state.phase == DeliveryPhase.FAILED && state.failure?.target == FailureTarget.CURRENT_STOP)
        if (!canSkip) return transition()
        return moveToNextStopOrReturn("현재 배부 지점을 건너뜁니다.")
    }

    @Synchronized
    fun retry(): DeliveryTransition {
        if (state.phase != DeliveryPhase.FAILED) return transition()
        val route = state.route ?: return transition()
        return when (state.failure?.target) {
            FailureTarget.CURRENT_STOP -> {
                val stop = state.currentStop ?: return transition()
                state = state.copy(
                    phase = DeliveryPhase.NAVIGATING,
                    message = "${stop.destination.displayName}(으)로 다시 이동합니다.",
                    failure = null,
                )
                transition(
                    RobotCommand.Speak("이동을 다시 시도합니다."),
                    navigateCommand(route, stop.destination),
                )
            }

            FailureTarget.RETURN_DESTINATION -> {
                state = state.copy(
                    phase = DeliveryPhase.RETURNING,
                    message = "${route.returnDestination.displayName}(으)로 다시 복귀합니다.",
                    failure = null,
                )
                transition(
                    RobotCommand.Speak("복귀를 다시 시도합니다."),
                    navigateCommand(route, route.returnDestination),
                )
            }

            else -> transition()
        }
    }

    @Synchronized
    fun returnNow(): DeliveryTransition {
        val route = state.route ?: return transition()
        if (state.phase == DeliveryPhase.COMPLETED || state.phase == DeliveryPhase.RETURNING) {
            return transition()
        }
        state = state.copy(
            phase = DeliveryPhase.RETURNING,
            stopIndex = route.stops.size,
            message = "배부를 중단하고 ${route.returnDestination.displayName}(으)로 복귀 중입니다.",
            failure = null,
        )
        return transition(
            RobotCommand.StopMovement,
            RobotCommand.Speak("배부를 중단하고 복귀합니다."),
            navigateCommand(route, route.returnDestination),
        )
    }

    @Synchronized
    fun cancel(): DeliveryTransition {
        if (!state.phase.isControllable()) return transition()
        state = state.copy(
            phase = DeliveryPhase.CANCELLED,
            message = "배부 작업이 취소되었습니다.",
            failure = null,
        )
        return transition(
            RobotCommand.StopMovement,
            RobotCommand.Speak("배부 작업을 취소했습니다."),
        )
    }

    @Synchronized
    fun emergencyStop(): DeliveryTransition {
        if (!state.phase.isControllable()) return transition()
        state = state.copy(
            phase = DeliveryPhase.EMERGENCY_STOPPED,
            message = "긴급 정지했습니다. 주변 안전을 확인해 주세요.",
            failure = null,
        )
        return transition(RobotCommand.StopMovement)
    }

    @Synchronized
    fun reset(): DeliveryTransition {
        if (state.phase.isActiveMission()) return transition()
        state = DeliverySnapshot()
        return transition()
    }

    @Synchronized
    fun commandFailed(command: RobotCommand, cause: Throwable): DeliveryTransition {
        if (command == RobotCommand.StopMovement) {
            state = state.copy(
                phase = DeliveryPhase.FAILED,
                message = "temi 정지 명령을 보내지 못했습니다. 앱을 종료하지 말고 안전을 확인해 주세요.",
                failure = DeliveryFailure(
                    target = FailureTarget.NONE,
                    description = cause.message ?: cause.javaClass.simpleName,
                ),
            )
            return transition()
        }
        if (command !is RobotCommand.NavigateTo) return transition()
        val target = when (state.phase) {
            DeliveryPhase.NAVIGATING -> FailureTarget.CURRENT_STOP
            DeliveryPhase.RETURNING -> FailureTarget.RETURN_DESTINATION
            else -> FailureTarget.NONE
        }
        state = state.copy(
            phase = DeliveryPhase.FAILED,
            message = "temi 이동 명령을 보내지 못했습니다.",
            failure = DeliveryFailure(
                target = target,
                description = cause.message ?: cause.javaClass.simpleName,
            ),
        )
        return transition()
    }

    private fun navigationProgress(
        status: NavigationStatus,
        destination: Destination,
    ): DeliveryTransition {
        val detail = when (status) {
            NavigationStatus.START -> "이동을 시작했습니다."
            NavigationStatus.CALCULATING -> "안전한 경로를 계산 중입니다."
            NavigationStatus.GOING -> "이동 중입니다."
            NavigationStatus.REPOSING -> "현재 위치를 다시 확인 중입니다."
            else -> return transition()
        }
        state = state.copy(message = "${destination.displayName}: $detail")
        return transition()
    }

    private fun navigationCompleted(route: DeliveryRoute): DeliveryTransition {
        return if (state.phase == DeliveryPhase.RETURNING) {
            state = state.copy(
                phase = DeliveryPhase.COMPLETED,
                stopIndex = route.stops.size,
                message = "${route.returnDestination.displayName}에 복귀했습니다. 배부 작업이 끝났습니다.",
                failure = null,
            )
            transition(RobotCommand.Speak("복귀했습니다. 수업용품 배부를 마쳤습니다."))
        } else {
            val stop = state.currentStop ?: return transition()
            state = state.copy(
                phase = DeliveryPhase.WAITING_FOR_PICKUP,
                message = "${stop.recipient}의 수령 확인을 기다리는 중입니다.",
                failure = null,
            )
            transition(
                RobotCommand.Speak(
                    "${stop.recipient}, 도착했습니다. 바구니에서 ${stop.supply} ${stop.quantity}개를 꺼내 주세요. " +
                        "꺼낸 뒤 화면의 수령 확인 버튼을 눌러 주세요.",
                ),
            )
        }
    }

    private fun navigationAborted(event: NavigationEvent): DeliveryTransition {
        val target = if (state.phase == DeliveryPhase.RETURNING) {
            FailureTarget.RETURN_DESTINATION
        } else {
            FailureTarget.CURRENT_STOP
        }
        val reason = event.description.ifBlank {
            "원인을 확인할 수 없습니다. 오류 코드 ${event.descriptionId}"
        }
        state = state.copy(
            phase = DeliveryPhase.FAILED,
            message = "이동이 중단되었습니다: $reason",
            failure = DeliveryFailure(
                target = target,
                descriptionId = event.descriptionId,
                description = reason,
            ),
        )
        return transition(
            RobotCommand.Speak("이동이 중단되었습니다. 선생님이 경로를 확인해 주세요."),
        )
    }

    private fun moveToNextStopOrReturn(prefix: String): DeliveryTransition {
        val route = state.route ?: return transition()
        val nextIndex = state.stopIndex + 1
        return if (nextIndex < route.stops.size) {
            val nextStop = route.stops[nextIndex]
            state = state.copy(
                phase = DeliveryPhase.NAVIGATING,
                stopIndex = nextIndex,
                message = "$prefix ${nextStop.destination.displayName}(으)로 이동합니다.",
                failure = null,
            )
            transition(
                RobotCommand.Speak("$prefix 다음 배부 지점으로 이동합니다."),
                navigateCommand(route, nextStop.destination),
            )
        } else {
            state = state.copy(
                phase = DeliveryPhase.RETURNING,
                stopIndex = route.stops.size,
                message = "$prefix 모든 지점을 마쳐 ${route.returnDestination.displayName}(으)로 복귀합니다.",
                failure = null,
            )
            transition(
                RobotCommand.Speak("$prefix 모든 배부 지점을 마쳤습니다. 복귀합니다."),
                navigateCommand(route, route.returnDestination),
            )
        }
    }

    private fun validateAndResolve(
        route: DeliveryRoute,
        savedLocations: List<String>,
    ): RouteValidation {
        if (route.name.isBlank()) return RouteValidation.Invalid("경로 이름이 비어 있습니다.")
        if (route.stops.isEmpty()) return RouteValidation.Invalid("배부 지점이 하나도 없습니다.")
        if (route.stops.map { it.id }.any { it.isBlank() }) {
            return RouteValidation.Invalid("배부 지점 ID는 비워 둘 수 없습니다.")
        }
        if (route.stops.map { it.id }.distinct().size != route.stops.size) {
            return RouteValidation.Invalid("배부 지점 ID가 중복되었습니다.")
        }
        route.stops.forEach { stop ->
            if (stop.recipient.isBlank() || stop.supply.isBlank()) {
                return RouteValidation.Invalid("학생 이름과 수업용품 이름을 입력해 주세요.")
            }
            if (stop.quantity <= 0) {
                return RouteValidation.Invalid("${stop.recipient}의 수량은 1개 이상이어야 합니다.")
            }
        }

        val canonicalLocations = savedLocations.associateBy { it.normalizedLocation() }
        val missing = mutableListOf<String>()

        fun resolve(destination: Destination): Destination = when (destination) {
            is Destination.SavedLocation -> {
                val canonical = canonicalLocations[destination.name.normalizedLocation()]
                if (canonical == null) {
                    missing += destination.name
                    destination
                } else {
                    destination.copy(name = canonical)
                }
            }

            is Destination.Coordinate -> destination
        }

        val resolvedStops = route.stops.map { stop ->
            val destination = resolve(stop.destination)
            val coordinateError = destination.coordinateValidationError()
            if (coordinateError != null) return RouteValidation.Invalid(coordinateError)
            stop.copy(destination = destination)
        }
        val resolvedReturn = resolve(route.returnDestination)
        resolvedReturn.coordinateValidationError()?.let { return RouteValidation.Invalid(it) }

        if (missing.isNotEmpty()) {
            return RouteValidation.Invalid(
                "temi 지도에 저장되지 않은 위치가 있습니다: ${missing.distinct().joinToString()}",
            )
        }
        return RouteValidation.Valid(
            route.copy(stops = resolvedStops, returnDestination = resolvedReturn),
        )
    }

    private fun Destination.coordinateValidationError(): String? {
        if (this !is Destination.Coordinate) return null
        if (!x.isFinite() || !y.isFinite() || !yaw.isFinite()) {
            return "$displayName 좌표에는 유한한 숫자만 사용할 수 있습니다."
        }
        val validYaw = yaw == 999f || yaw in -PI.toFloat()..PI.toFloat()
        return if (validYaw) null else "$displayName yaw는 -π~π 라디안 또는 999여야 합니다."
    }

    private fun NavigationEvent.matches(destination: Destination): Boolean = when (destination) {
        is Destination.SavedLocation -> location.normalizedLocation() == destination.name.normalizedLocation()
        is Destination.Coordinate -> true
    }

    private fun navigateCommand(
        route: DeliveryRoute,
        destination: Destination,
    ) = RobotCommand.NavigateTo(
        destination = destination,
        speed = route.speed,
        highAccuracyArrival = route.highAccuracyArrival,
    )

    private fun configurationError(message: String): DeliveryTransition {
        state = DeliverySnapshot(
            phase = DeliveryPhase.CONFIGURATION_ERROR,
            message = message,
            failure = DeliveryFailure(FailureTarget.NONE, description = message),
        )
        return transition()
    }

    private fun transition(vararg commands: RobotCommand) = transition(commands.toList())

    private fun transition(commands: List<RobotCommand> = emptyList()) =
        DeliveryTransition(state = state, commands = commands)

    private fun String.normalizedLocation() = trim().lowercase(Locale.ROOT)

    private fun DeliveryPhase.isActiveMission() = this == DeliveryPhase.NAVIGATING ||
        this == DeliveryPhase.WAITING_FOR_PICKUP || this == DeliveryPhase.RETURNING

    private fun DeliveryPhase.canStartMission() = this == DeliveryPhase.IDLE ||
        this == DeliveryPhase.COMPLETED || this == DeliveryPhase.CANCELLED ||
        this == DeliveryPhase.CONFIGURATION_ERROR

    private fun DeliveryPhase.isControllable() = isActiveMission() || this == DeliveryPhase.FAILED ||
        this == DeliveryPhase.EMERGENCY_STOPPED

    private sealed interface RouteValidation {
        data class Valid(val route: DeliveryRoute) : RouteValidation
        data class Invalid(val message: String) : RouteValidation
    }
}
