package kr.hs.gwangyang.temidelivery.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Executes state-machine commands and serializes temi callbacks into mission state. */
class DeliveryCoordinator(
    private val robotGateway: RobotGateway,
    scope: CoroutineScope,
) {
    private val machine = DeliveryStateMachine()
    private val lock = Any()
    private val _state = MutableStateFlow(machine.state)
    val state: StateFlow<DeliverySnapshot> = _state.asStateFlow()

    init {
        scope.launch {
            robotGateway.navigationEvents.collect { event ->
                applyTransition { machine.onNavigationEvent(event) }
            }
        }
    }

    fun start(route: DeliveryRoute) {
        robotGateway.refreshRobotState()
        applyTransition {
            machine.start(
                route = route,
                robotReady = robotGateway.isReady.value,
                savedLocations = robotGateway.savedLocations.value,
            )
        }
    }

    fun confirmPickup() = applyTransition { machine.confirmPickup() }

    fun retry() = applyTransition { machine.retry() }

    fun skipCurrentStop() = applyTransition { machine.skipCurrentStop() }

    fun returnNow() = applyTransition { machine.returnNow() }

    fun cancel() = applyTransition { machine.cancel() }

    /** Always attempts a stop before an unconditional app-process kill. */
    fun stopForAppKill(): Result<Unit> = runCatching { robotGateway.stopMovement() }

    fun emergencyStop() = applyTransition { machine.emergencyStop() }

    fun reset() = applyTransition { machine.reset() }

    private fun applyTransition(block: () -> DeliveryTransition) {
        val transition = synchronized(lock, block)
        _state.value = transition.state
        for (command in transition.commands) {
            if (execute(command).isFailure) break
        }
    }

    private fun execute(command: RobotCommand): Result<Unit> {
        return runCatching {
            when (command) {
                is RobotCommand.NavigateTo -> robotGateway.navigateTo(
                    destination = command.destination,
                    speed = command.speed,
                    highAccuracyArrival = command.highAccuracyArrival,
                )

                is RobotCommand.Speak -> robotGateway.speak(command.text)
                RobotCommand.StopMovement -> robotGateway.stopMovement()
            }
        }.onFailure { cause ->
            val failed = synchronized(lock) { machine.commandFailed(command, cause) }
            _state.value = failed.state
        }
    }
}
