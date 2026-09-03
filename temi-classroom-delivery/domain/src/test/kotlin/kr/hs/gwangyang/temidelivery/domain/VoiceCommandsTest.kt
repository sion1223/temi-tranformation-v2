package kr.hs.gwangyang.temidelivery.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceCommandInterpreterTest {
    private val interpreter = VoiceCommandInterpreter()

    @Test
    fun `Korean home base commands are matched explicitly`() {
        val phrases = listOf(
            "홈베이스로 가",
            "홈 베이스로 이동해 줘",
            "충전기로 가 주세요",
            "충전하러 가",
            "홈베이스로 가세요",
        )

        phrases.forEach { phrase ->
            assertEquals(
                VoiceCommandInterpretation.Matched(VoiceRobotCommand.GoToHomeBase),
                interpreter.interpret(phrase, emptyList()),
            )
        }
    }

    @Test
    fun `English home base command is matched ignoring punctuation and case`() {
        assertEquals(
            VoiceCommandInterpretation.Matched(VoiceRobotCommand.GoToHomeBase),
            interpreter.interpret("Go to Home Base!", emptyList()),
        )
    }

    @Test
    fun `saved location command resolves to the canonical temi name`() {
        assertEquals(
            VoiceCommandInterpretation.Matched(
                VoiceRobotCommand.GoToSavedLocation("과학실-A"),
            ),
            interpreter.interpret("과학실 A로 이동해 주세요", listOf("과학실-A", "교탁")),
        )
    }

    @Test
    fun `unknown navigation destination is rejected instead of becoming a question`() {
        assertTrue(
            interpreter.interpret("교무실로 가", listOf("교탁")) is
                VoiceCommandInterpretation.UnknownDestination,
        )
    }

    @Test
    fun `ordinary questions mentioning home base do not move the robot`() {
        assertEquals(
            VoiceCommandInterpretation.NotACommand,
            interpreter.interpret("홈베이스가 어디에 있나요?", emptyList()),
        )
    }

    @Test
    fun `ordinary Korean text ending in ga particle is not treated as navigation`() {
        assertEquals(
            VoiceCommandInterpretation.NotACommand,
            interpreter.interpret("진로 상담 담당자가?", listOf("상담실")),
        )
    }

    @Test
    fun `stop and follow commands are matched`() {
        assertEquals(
            VoiceCommandInterpretation.Matched(VoiceRobotCommand.StopMovement),
            interpreter.interpret("멈춰 주세요", emptyList()),
        )
        assertEquals(
            VoiceCommandInterpretation.Matched(VoiceRobotCommand.FollowMe),
            interpreter.interpret("나를 따라와", emptyList()),
        )
    }
}

class HandleVoiceCommandUseCaseTest {
    @Test
    fun `home command cancels an active delivery before navigating home slowly`() {
        val gateway = FakeRobotGateway(locations = listOf("배부-1", "교탁"))
        val coordinator = coordinatorFor(gateway)
        coordinator.start(route())
        val useCase = HandleVoiceCommandUseCase(VoiceCommandInterpreter(), coordinator, gateway)

        val result = useCase("홈베이스로 가", movementControlsEnabled = true)

        assertTrue(result is VoiceCommandResult.Executed)
        assertEquals(DeliveryPhase.CANCELLED, coordinator.state.value.phase)
        assertEquals(1, gateway.stopMovementCalls)
        assertEquals(
            Destination.SavedLocation("home base", "홈베이스"),
            gateway.navigationRequests.last().destination,
        )
        assertEquals(DeliverySpeed.VERY_SLOW, gateway.navigationRequests.last().speed)
    }

    @Test
    fun `movement commands respect the delivery controls setting`() {
        val gateway = FakeRobotGateway()
        val useCase = useCaseFor(gateway)

        val result = useCase("홈베이스로 가", movementControlsEnabled = false)

        assertTrue(result is VoiceCommandResult.Rejected)
        assertTrue(gateway.navigationRequests.isEmpty())
    }

    @Test
    fun `voice stop remains available when movement controls are disabled`() {
        val gateway = FakeRobotGateway()
        val useCase = useCaseFor(gateway)

        val result = useCase("멈춰", movementControlsEnabled = false)

        assertTrue(result is VoiceCommandResult.Executed)
        assertEquals(1, gateway.stopMovementCalls)
    }

    @Test
    fun `saved location and follow commands call the robot gateway`() {
        val gateway = FakeRobotGateway(locations = listOf("도서관"))
        val useCase = useCaseFor(gateway)

        val locationResult = useCase("도서관으로 가", movementControlsEnabled = true)
        val followResult = useCase("나를 따라와", movementControlsEnabled = true)

        assertTrue(locationResult is VoiceCommandResult.Executed)
        assertEquals(Destination.SavedLocation("도서관"), gateway.navigationRequests.single().destination)
        assertTrue(followResult is VoiceCommandResult.Executed)
        assertEquals(listOf(DeliverySpeed.VERY_SLOW), gateway.followRequests)
    }

    @Test
    fun `movement is not restarted by voice while emergency stopped`() {
        val gateway = FakeRobotGateway(locations = listOf("배부-1", "교탁"))
        val coordinator = coordinatorFor(gateway)
        coordinator.start(route())
        coordinator.emergencyStop()
        val requestsBeforeCommand = gateway.navigationRequests.size
        val useCase = HandleVoiceCommandUseCase(VoiceCommandInterpreter(), coordinator, gateway)

        val result = useCase("홈베이스로 가", movementControlsEnabled = true)

        assertTrue(result is VoiceCommandResult.Rejected)
        assertEquals(requestsBeforeCommand, gateway.navigationRequests.size)
    }

    private fun useCaseFor(gateway: FakeRobotGateway): HandleVoiceCommandUseCase {
        val coordinator = coordinatorFor(gateway)
        return HandleVoiceCommandUseCase(VoiceCommandInterpreter(), coordinator, gateway)
    }

    private fun coordinatorFor(gateway: FakeRobotGateway) = DeliveryCoordinator(
        robotGateway = gateway,
        scope = CoroutineScope(Dispatchers.Unconfined),
    )

    private fun route() = DeliveryRoute(
        name = "테스트 배부",
        stops = listOf(
            DeliveryStop(
                id = "one",
                destination = Destination.SavedLocation("배부-1"),
                recipient = "1모둠",
                supply = "교재",
                quantity = 1,
            ),
        ),
        returnDestination = Destination.SavedLocation("교탁"),
    )

    private data class NavigationRequest(
        val destination: Destination,
        val speed: DeliverySpeed,
        val highAccuracyArrival: Boolean,
    )

    private class FakeRobotGateway(
        ready: Boolean = true,
        locations: List<String> = emptyList(),
    ) : RobotGateway {
        override val isReady = MutableStateFlow(ready)
        override val savedLocations = MutableStateFlow(locations)
        override val navigationEvents: Flow<NavigationEvent> = emptyFlow()
        val navigationRequests = mutableListOf<NavigationRequest>()
        val followRequests = mutableListOf<DeliverySpeed>()
        var stopMovementCalls = 0
            private set

        override fun refreshRobotState() = Unit

        override fun currentPose(): RobotPose? = null

        override fun navigateTo(
            destination: Destination,
            speed: DeliverySpeed,
            highAccuracyArrival: Boolean,
        ) {
            navigationRequests += NavigationRequest(destination, speed, highAccuracyArrival)
        }

        override fun stopMovement() {
            stopMovementCalls += 1
        }

        override fun followMe(speed: DeliverySpeed) {
            followRequests += speed
        }

        override fun speak(text: String) = Unit
    }
}
