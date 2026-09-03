package kr.hs.gwangyang.temidelivery.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** Framework-free boundary between delivery business logic and the temi Android SDK. */
interface RobotGateway {
    val isReady: StateFlow<Boolean>
    val savedLocations: StateFlow<List<String>>
    val navigationEvents: Flow<NavigationEvent>

    fun refreshRobotState()

    fun currentPose(): RobotPose?

    fun navigateTo(
        destination: Destination,
        speed: DeliverySpeed,
        highAccuracyArrival: Boolean,
    )

    fun stopMovement()

    fun followMe(speed: DeliverySpeed)

    fun speak(text: String)
}

interface DeliveryRouteRepository {
    suspend fun loadConfiguredRoute(): Result<DeliveryRoute>
}
