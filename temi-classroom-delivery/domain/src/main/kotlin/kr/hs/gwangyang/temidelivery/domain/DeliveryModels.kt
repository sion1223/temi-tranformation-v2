package kr.hs.gwangyang.temidelivery.domain

/** A destination on temi's currently loaded map. */
sealed interface Destination {
    val displayName: String

    /** A position saved from temi's map UI. This is the preferred, map-safe option. */
    data class SavedLocation(
        val name: String,
        override val displayName: String = name,
    ) : Destination

    /**
     * Raw map coordinates. x/y are map coordinates and yaw is radians in [-PI, PI].
     * yaw=999f asks temi not to rotate at the destination.
     */
    data class Coordinate(
        val x: Float,
        val y: Float,
        val yaw: Float,
        override val displayName: String,
    ) : Destination
}

enum class DeliverySpeed {
    VERY_SLOW,
    SLOW,
    MEDIUM,
}

data class DeliveryStop(
    val id: String,
    val destination: Destination,
    val recipient: String,
    val supply: String,
    val quantity: Int,
    val guideItemId: String? = null,
)

data class DeliveryRoute(
    val name: String,
    val stops: List<DeliveryStop>,
    val returnDestination: Destination,
    val speed: DeliverySpeed = DeliverySpeed.VERY_SLOW,
    val highAccuracyArrival: Boolean = true,
)

enum class DeliveryPhase {
    IDLE,
    NAVIGATING,
    WAITING_FOR_PICKUP,
    RETURNING,
    COMPLETED,
    FAILED,
    CONFIGURATION_ERROR,
    CANCELLED,
    EMERGENCY_STOPPED,
}

enum class FailureTarget {
    CURRENT_STOP,
    RETURN_DESTINATION,
    NONE,
}

data class DeliveryFailure(
    val target: FailureTarget,
    val descriptionId: Int? = null,
    val description: String,
)

data class DeliverySnapshot(
    val phase: DeliveryPhase = DeliveryPhase.IDLE,
    val route: DeliveryRoute? = null,
    val stopIndex: Int = -1,
    val message: String = "배부 경로를 확인한 뒤 시작해 주세요.",
    val failure: DeliveryFailure? = null,
) {
    val currentStop: DeliveryStop?
        get() = route?.stops?.getOrNull(stopIndex)

    val completedStopCount: Int
        get() = when (phase) {
            DeliveryPhase.RETURNING,
            DeliveryPhase.COMPLETED,
            -> route?.stops?.size ?: 0

            DeliveryPhase.NAVIGATING,
            DeliveryPhase.WAITING_FOR_PICKUP,
            DeliveryPhase.FAILED,
            -> stopIndex.coerceAtLeast(0)

            else -> 0
        }
}

enum class NavigationStatus {
    START,
    CALCULATING,
    GOING,
    COMPLETE,
    ABORT,
    REPOSING,
    UNKNOWN,
}

data class NavigationEvent(
    val location: String,
    val status: NavigationStatus,
    val descriptionId: Int,
    val description: String,
)

data class RobotPose(
    val x: Float,
    val y: Float,
    val yaw: Float,
    val isInMapArea: Boolean?,
)

sealed interface RobotCommand {
    data class NavigateTo(
        val destination: Destination,
        val speed: DeliverySpeed,
        val highAccuracyArrival: Boolean,
    ) : RobotCommand

    data class Speak(val text: String) : RobotCommand

    data object StopMovement : RobotCommand
}

data class DeliveryTransition(
    val state: DeliverySnapshot,
    val commands: List<RobotCommand> = emptyList(),
)
