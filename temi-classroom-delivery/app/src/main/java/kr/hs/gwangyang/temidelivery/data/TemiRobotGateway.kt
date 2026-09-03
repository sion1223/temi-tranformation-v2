package kr.hs.gwangyang.temidelivery.data

import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener
import com.robotemi.sdk.listeners.OnLocationsUpdatedListener
import com.robotemi.sdk.listeners.OnRobotReadyListener
import com.robotemi.sdk.navigation.model.Position
import com.robotemi.sdk.navigation.model.SpeedLevel
import kr.hs.gwangyang.temidelivery.domain.DeliverySpeed
import kr.hs.gwangyang.temidelivery.domain.Destination
import kr.hs.gwangyang.temidelivery.domain.NavigationEvent
import kr.hs.gwangyang.temidelivery.domain.NavigationStatus
import kr.hs.gwangyang.temidelivery.domain.RobotGateway
import kr.hs.gwangyang.temidelivery.domain.RobotPose
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/** Production adapter for the robotemi Android SDK. Register it once from Application.onCreate(). */
class TemiRobotGateway(
    private val robot: Robot,
    private val speechEnabled: () -> Boolean = { true },
) : RobotGateway,
    OnRobotReadyListener,
    OnGoToLocationStatusChangedListener,
    OnLocationsUpdatedListener {

    private val _isReady = MutableStateFlow(robot.isReady)
    override val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _savedLocations = MutableStateFlow<List<String>>(emptyList())
    override val savedLocations: StateFlow<List<String>> = _savedLocations.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>(
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val navigationEvents: Flow<NavigationEvent> = _navigationEvents.asSharedFlow()

    private var registered = false
    private val startupSafetyStop = StartupSafetyStop()

    fun start() {
        if (registered) return
        registered = true
        robot.addOnRobotReadyListener(this)
        robot.addOnGoToLocationStatusChangedListener(this)
        robot.addOnLocationsUpdatedListener(this)
        applyStartupSafetyStop()
        refreshRobotState()
    }

    fun stop() {
        if (!registered) return
        robot.removeOnRobotReadyListener(this)
        robot.removeOnGoToLocationStatusChangedListener(this)
        robot.removeOnLocationsUpdateListener(this)
        registered = false
    }

    override fun onRobotReady(isReady: Boolean) {
        _isReady.value = isReady
        applyStartupSafetyStop()
        refreshRobotState()
    }

    override fun onLocationsUpdated(locations: List<String>) {
        _savedLocations.value = locations.sortedWith(String.CASE_INSENSITIVE_ORDER)
    }

    override fun onGoToLocationStatusChanged(
        location: String,
        status: String,
        descriptionId: Int,
        description: String,
    ) {
        _navigationEvents.tryEmit(
            NavigationEvent(
                location = location,
                status = status.toDomainStatus(),
                descriptionId = descriptionId,
                description = description,
            ),
        )
    }

    override fun refreshRobotState() {
        val ready = robot.isReady
        if (ready) applyStartupSafetyStop()
        _isReady.value = ready
        _savedLocations.value = if (ready) {
            robot.locations.sortedWith(String.CASE_INSENSITIVE_ORDER)
        } else {
            emptyList()
        }
    }

    override fun currentPose(): RobotPose? {
        if (!robot.isReady) return null
        return robot.getPosition().let { position ->
            RobotPose(
                x = position.x,
                y = position.y,
                yaw = position.yaw,
                isInMapArea = position.isInMapArea,
            )
        }
    }

    override fun navigateTo(
        destination: Destination,
        speed: DeliverySpeed,
        highAccuracyArrival: Boolean,
    ) {
        val sdkSpeed = speed.toSdkSpeed()
        when (destination) {
            is Destination.SavedLocation -> robot.goTo(
                location = destination.name,
                backwards = false,
                noBypass = false,
                speedLevel = sdkSpeed,
                highAccuracyArrival = highAccuracyArrival,
                noRotationAtEnd = false,
            )

            is Destination.Coordinate -> robot.goToPosition(
                position = Position(destination.x, destination.y, destination.yaw),
                backwards = false,
                noBypass = false,
                speedLevel = sdkSpeed,
                highAccuracyArrival = highAccuracyArrival,
            )
        }
    }

    override fun stopMovement() {
        robot.stopMovement()
    }

    override fun followMe(speed: DeliverySpeed) {
        robot.beWithMe(speed.toSdkSpeed())
    }

    override fun speak(text: String) {
        if (!speechEnabled()) return
        robot.speak(
            TtsRequest.create(
                speech = text,
                isShowOnConversationLayer = false,
                language = TtsRequest.Language.KO_KR,
                cached = true,
            ),
        )
    }

    /**
     * A process restart loses the in-memory mission state while temi may still be moving.
     * Stop once on the first ready SDK callback; a failed SDK call remains retryable.
     */
    private fun applyStartupSafetyStop() {
        runCatching {
            startupSafetyStop.attempt(robot.isReady) { robot.stopMovement() }
        }
    }

    private fun DeliverySpeed.toSdkSpeed() = when (this) {
        DeliverySpeed.VERY_SLOW -> SpeedLevel.VERY_SLOW
        DeliverySpeed.SLOW -> SpeedLevel.SLOW
        DeliverySpeed.MEDIUM -> SpeedLevel.MEDIUM
    }

    private fun String.toDomainStatus() = when (this) {
        OnGoToLocationStatusChangedListener.START -> NavigationStatus.START
        OnGoToLocationStatusChangedListener.CALCULATING -> NavigationStatus.CALCULATING
        OnGoToLocationStatusChangedListener.GOING -> NavigationStatus.GOING
        OnGoToLocationStatusChangedListener.COMPLETE -> NavigationStatus.COMPLETE
        OnGoToLocationStatusChangedListener.ABORT -> NavigationStatus.ABORT
        OnGoToLocationStatusChangedListener.REPOSING -> NavigationStatus.REPOSING
        else -> NavigationStatus.UNKNOWN
    }
}

internal class StartupSafetyStop {
    private var completed = false

    fun attempt(isReady: Boolean, stopMovement: () -> Unit): Boolean {
        if (!isReady || completed) return false
        stopMovement()
        completed = true
        return true
    }
}
