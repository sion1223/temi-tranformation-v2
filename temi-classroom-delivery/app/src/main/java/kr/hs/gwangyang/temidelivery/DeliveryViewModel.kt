package kr.hs.gwangyang.temidelivery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kr.hs.gwangyang.temidelivery.aiguide.SchoolAnswer
import kr.hs.gwangyang.temidelivery.aiguide.SupplyGuideClientBundle
import kr.hs.gwangyang.temidelivery.aiguide.SupplyGuideSource
import kr.hs.gwangyang.temidelivery.basket.BasketConnectionState
import kr.hs.gwangyang.temidelivery.basket.BasketSnapshot
import kr.hs.gwangyang.temidelivery.domain.DeliveryCoordinator
import kr.hs.gwangyang.temidelivery.domain.DeliveryPhase
import kr.hs.gwangyang.temidelivery.domain.DeliveryRoute
import kr.hs.gwangyang.temidelivery.domain.DeliveryRouteRepository
import kr.hs.gwangyang.temidelivery.domain.DeliverySnapshot
import kr.hs.gwangyang.temidelivery.domain.HandleVoiceCommandUseCase
import kr.hs.gwangyang.temidelivery.domain.RobotGateway
import kr.hs.gwangyang.temidelivery.domain.RobotPose
import kr.hs.gwangyang.temidelivery.domain.VoiceCommandResult
import kr.hs.gwangyang.temidelivery.data.KioskGateway
import kr.hs.gwangyang.temidelivery.data.KioskState
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeliveryViewModel(
    private val coordinator: DeliveryCoordinator,
    private val routeRepository: DeliveryRouteRepository,
    private val robotGateway: RobotGateway,
    private val supplyGuideClient: SupplyGuideClientBundle,
    private val kioskGateway: KioskGateway,
    private val basketClient: RuntimeBasketClient,
    private val featureSettingsStore: FeatureSettingsStore,
    private val handleVoiceCommand: HandleVoiceCommandUseCase,
) : ViewModel() {
    private val routeLoad = MutableStateFlow(RouteLoadState(isLoading = true))
    private val currentPose = MutableStateFlow<RobotPose?>(null)
    private val kioskState = MutableStateFlow(KioskState())
    private val guideLoad = MutableStateFlow(SupplyGuideUiState())
    private val schoolAnswerLoad = MutableStateFlow(SchoolAnswerUiState())
    private val basketRefresh = MutableStateFlow(BasketRefreshState())
    private val basketAction = MutableStateFlow(BasketActionState())
    private val voiceInput = MutableStateFlow(VoiceInputUiState())
    private var guideRequestJob: Job? = null
    private var schoolAnswerRequestJob: Job? = null
    private var basketRefreshJob: Job? = null
    private var basketActionJob: Job? = null
    private var voiceTranscriptionJob: Job? = null
    private var guideRequestGeneration = 0L
    private var schoolAnswerRequestGeneration = 0L
    private var voiceTranscriptionGeneration = 0L

    private val coreUiState = combine(
        routeLoad,
        coordinator.state,
        robotGateway.isReady,
        robotGateway.savedLocations,
        currentPose,
    ) { route, mission, robotReady, locations, pose ->
        DeliveryUiState(
            isLoadingRoute = route.isLoading,
            route = route.route,
            routeError = route.error,
            mission = mission,
            robotReady = robotReady,
            savedLocations = locations,
            currentPose = pose,
            guideFeatureEnabled = supplyGuideClient.enabled,
            guideFeatureStatus = supplyGuideClient.statusMessage,
        )
    }

    private val baseUiState = combine(
        coreUiState,
        kioskState,
        featureSettingsStore.settings,
    ) { core, kiosk, settings ->
        val aiEnabled = settings.aiAssistantEnabled && supplyGuideClient.enabled
        core.copy(
            kiosk = kiosk,
            featureSettings = settings,
            guideFeatureEnabled = aiEnabled,
            guideFeatureStatus = when {
                !settings.aiAssistantEnabled -> "AI 담당자·물품 안내가 기능 설정에서 꺼져 있습니다."
                else -> supplyGuideClient.statusMessage
            },
        )
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val basketUiState = basketClient.bundle.flatMapLatest { activeClient ->
        combine(
            activeClient.gateway.connectionState,
            activeClient.gateway.status,
            basketRefresh,
            basketAction,
        ) { connection, snapshot, refresh, action ->
            BasketUiState(
                enabled = activeClient.enabled,
                featureStatus = activeClient.statusMessage,
                connection = connection,
                snapshot = snapshot,
                isRefreshing = refresh.isLoading,
                isActing = action.isLoading,
                actionMessage = action.message,
                error = action.error ?: refresh.error,
            )
        }
    }

    val uiState: StateFlow<DeliveryUiState> = combine(
        baseUiState,
        guideLoad,
        schoolAnswerLoad,
        basketUiState,
        voiceInput,
    ) { base, guide, schoolAnswer, basket, voice ->
        base.copy(
            guide = guide,
            schoolAnswer = schoolAnswer,
            basket = basket,
            voiceInput = voice,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DeliveryUiState(
            guideFeatureEnabled = supplyGuideClient.enabled &&
                featureSettingsStore.current().aiAssistantEnabled,
            guideFeatureStatus = supplyGuideClient.statusMessage,
            featureSettings = featureSettingsStore.current(),
        ),
    )

    init {
        reloadRoute()
        refreshRobotInfo()
        viewModelScope.launch {
            robotGateway.isReady.collect {
                kioskState.value = withContext(Dispatchers.IO) { kioskGateway.readState() }
            }
        }
    }

    fun reloadRoute() {
        viewModelScope.launch {
            routeLoad.value = RouteLoadState(isLoading = true)
            routeRepository.loadConfiguredRoute()
                .onSuccess { routeLoad.value = RouteLoadState(route = it) }
                .onFailure {
                    routeLoad.value = RouteLoadState(
                        error = it.message ?: "배부 경로 파일을 읽지 못했습니다.",
                    )
                }
        }
    }

    fun refreshRobotInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            robotGateway.refreshRobotState()
            currentPose.value = robotGateway.currentPose()
            kioskState.value = kioskGateway.readState()
        }
        refreshBasket()
    }

    fun requestKioskSetup(): Result<Unit> {
        val result = kioskGateway.requestSetup()
        refreshRobotInfo()
        return result
    }

    fun startDelivery() {
        if (!featureSettingsStore.current().deliveryControlsEnabled) return
        clearGuide()
        val route = routeLoad.value.route ?: return
        coordinator.start(route)
    }

    fun requestSupplyGuide() {
        val snapshot = coordinator.state.value
        val stop = snapshot.currentStop
        if (snapshot.phase != DeliveryPhase.WAITING_FOR_PICKUP || stop == null) return
        val itemId = stop.guideItemId
        if (itemId.isNullOrBlank()) {
            guideLoad.value = SupplyGuideUiState(
                stopId = stop.id,
                error = "이 배부 물품에는 guideItemId가 설정되지 않았습니다.",
            )
            return
        }
        if (!isAiAssistantAvailable()) {
            guideLoad.value = SupplyGuideUiState(
                stopId = stop.id,
                error = supplyGuideClient.statusMessage,
            )
            return
        }

        guideRequestJob?.cancel()
        val generation = ++guideRequestGeneration
        guideLoad.value = SupplyGuideUiState(stopId = stop.id, isLoading = true)
        guideRequestJob = viewModelScope.launch {
            supplyGuideClient.gateway.explain(itemId).fold(
                onSuccess = { guide ->
                    if (generation != guideRequestGeneration || !isStillWaitingAt(stop.id)) return@fold
                    val ttsError = speakIfEnabled(guide.explanation)
                    guideLoad.value = SupplyGuideUiState(
                        stopId = stop.id,
                        explanation = guide.explanation,
                        source = guide.source,
                        model = guide.model,
                        warning = listOfNotNull(
                            guide.warning,
                            ttsError?.let { "화면에는 표시했지만 temi 음성 출력에 실패했습니다." },
                        ).joinToString(" ").ifBlank { null },
                    )
                },
                onFailure = { error ->
                    if (generation != guideRequestGeneration || !isStillWaitingAt(stop.id)) return@fold
                    guideLoad.value = SupplyGuideUiState(
                        stopId = stop.id,
                        error = error.message ?: "AI 사용법 설명을 가져오지 못했습니다.",
                    )
                },
            )
        }
    }

    fun askSchoolQuestion(question: String) {
        schoolAnswerRequestJob?.cancel()
        schoolAnswerRequestJob = null
        val generation = ++schoolAnswerRequestGeneration
        val normalizedQuestion = question.trim()
        if (normalizedQuestion.isEmpty()) {
            schoolAnswerLoad.value = SchoolAnswerUiState(error = "찾고 싶은 선생님의 업무나 과목을 입력해 주세요.")
            return
        }
        if (normalizedQuestion.length > 300) {
            schoolAnswerLoad.value = SchoolAnswerUiState(error = "질문은 300자 이하여야 합니다.")
            return
        }
        if (!isAiAssistantAvailable()) {
            schoolAnswerLoad.value = SchoolAnswerUiState(error = supplyGuideClient.statusMessage)
            return
        }

        schoolAnswerLoad.value = SchoolAnswerUiState(isLoading = true)
        schoolAnswerRequestJob = viewModelScope.launch {
            supplyGuideClient.schoolAnswerGateway.answer(normalizedQuestion).fold(
                onSuccess = { answer ->
                    if (generation != schoolAnswerRequestGeneration) return@fold
                    val ttsError = speakIfEnabled(answer.answer)
                    schoolAnswerLoad.value = SchoolAnswerUiState(
                        answer = answer.copy(
                            warning = listOfNotNull(
                                answer.warning,
                                ttsError?.let { "화면에는 표시했지만 temi 음성 출력에 실패했습니다." },
                            ).joinToString(" ").ifBlank { null },
                        ),
                    )
                },
                onFailure = { error ->
                    if (generation != schoolAnswerRequestGeneration) return@fold
                    schoolAnswerLoad.value = SchoolAnswerUiState(
                        error = error.message ?: "학교 담당자 안내를 가져오지 못했습니다.",
                    )
                },
            )
        }
    }

    fun refreshBasket() {
        val activeClient = basketClient.current()
        if (!activeClient.enabled) {
            basketRefresh.value = BasketRefreshState()
            return
        }
        basketRefreshJob?.cancel()
        basketRefresh.value = BasketRefreshState(isLoading = true)
        basketRefreshJob = viewModelScope.launch {
            activeClient.gateway.refresh().fold(
                onSuccess = { basketRefresh.value = BasketRefreshState() },
                onFailure = { error ->
                    basketRefresh.value = BasketRefreshState(
                        error = error.message ?: "바구니 상태를 가져오지 못했습니다.",
                    )
                },
            )
        }
    }

    fun transcribeVoice(wavBytes: ByteArray) {
        voiceTranscriptionJob?.cancel()
        voiceTranscriptionJob = null
        val generation = ++voiceTranscriptionGeneration
        val settings = featureSettingsStore.current()
        if (!settings.voiceInputEnabled) {
            voiceInput.value = VoiceInputUiState(error = "음성 입력이 기능 설정에서 꺼져 있습니다.")
            return
        }
        if (!isAiAssistantAvailable()) {
            voiceInput.value = VoiceInputUiState(error = supplyGuideClient.statusMessage)
            return
        }
        voiceInput.value = VoiceInputUiState(isTranscribing = true, status = "음성을 글자로 바꾸는 중입니다…")
        voiceTranscriptionJob = viewModelScope.launch {
            supplyGuideClient.speechTranscriptionGateway.transcribeWav(wavBytes).fold(
                onSuccess = { transcript ->
                    if (generation != voiceTranscriptionGeneration) return@fold
                    handleRecognizedVoice(
                        transcript = transcript.text,
                        model = transcript.model,
                        recognitionStatus = "음성 인식 완료 · ${transcript.durationMs}ms",
                        recognitionWarning = transcript.warning,
                    )
                },
                onFailure = { error ->
                    if (generation != voiceTranscriptionGeneration) return@fold
                    voiceInput.value = VoiceInputUiState(
                        error = error.message ?: "음성을 글자로 바꾸지 못했습니다.",
                    )
                },
            )
        }
    }

    fun acceptTemiVoiceTranscript(transcript: String) {
        val settings = featureSettingsStore.current()
        if (!settings.voiceInputEnabled) return
        val normalized = transcript.trim()
        if (normalized.isEmpty()) return
        handleRecognizedVoice(
            transcript = normalized,
            model = "temi 내장 ASR",
            recognitionStatus = "temi 내장 음성 인식 완료",
        )
    }

    fun updateVoiceStatus(status: String, recording: Boolean = false) {
        voiceInput.value = voiceInput.value.copy(
            isRecording = recording,
            isTranscribing = false,
            status = status,
            error = null,
        )
    }

    fun reportVoiceError(error: Throwable) {
        voiceInput.value = VoiceInputUiState(
            error = error.message ?: "음성 입력을 시작하지 못했습니다.",
        )
    }

    fun saveFeatureSettings(settings: FeatureSettings): Result<Unit> {
        val validation = basketClient.validate(settings)
        if (validation.isFailure) return validation
        basketRefreshJob?.cancel()
        basketActionJob?.cancel()
        basketRefresh.value = BasketRefreshState()
        basketAction.value = BasketActionState()
        featureSettingsStore.save(settings)
        basketClient.apply(settings)
        if (!settings.aiAssistantEnabled) {
            guideRequestJob?.cancel()
            schoolAnswerRequestJob?.cancel()
            voiceTranscriptionJob?.cancel()
            voiceTranscriptionGeneration += 1
            clearGuide()
            schoolAnswerLoad.value = SchoolAnswerUiState()
            voiceInput.value = VoiceInputUiState(
                status = if (
                    settings.voiceInputEnabled &&
                    settings.voiceActivationMode == VoiceActivationMode.TEMI_WAKE_WORD
                ) {
                    "AI 질문은 꺼져 있지만 'Hey temi' 음성 로봇 명령은 사용할 수 있습니다."
                } else {
                    "AI 질문이 꺼져 있어 외부 음성 변환을 사용할 수 없습니다."
                },
            )
        }
        if (!settings.voiceInputEnabled) {
            voiceTranscriptionJob?.cancel()
            voiceTranscriptionGeneration += 1
            voiceInput.value = VoiceInputUiState(status = "음성 입력이 꺼져 있습니다.")
        }
        if (settings.basketIntegrationEnabled) {
            refreshBasket()
        }
        return Result.success(Unit)
    }

    fun requestBasketSafeLock() {
        val activeClient = basketClient.current()
        if (!activeClient.enabled || basketAction.value.isLoading) return
        basketActionJob?.cancel()
        basketAction.value = BasketActionState(isLoading = true)
        basketActionJob = viewModelScope.launch {
            activeClient.gateway.safeState("temi 앱에서 요청한 수동 안전 잠금").fold(
                onSuccess = {
                    basketAction.value = BasketActionState(message = "Arduino 바구니를 안전 잠금 상태로 전환했습니다.")
                },
                onFailure = { error ->
                    basketAction.value = BasketActionState(
                        error = error.message ?: "Arduino 바구니 안전 잠금에 실패했습니다.",
                    )
                },
            )
        }
    }

    fun confirmPickup() {
        clearGuide()
        coordinator.confirmPickup()
    }

    fun retry() {
        clearGuide()
        coordinator.retry()
    }

    fun skipCurrentStop() {
        clearGuide()
        coordinator.skipCurrentStop()
    }

    fun returnNow() {
        clearGuide()
        coordinator.returnNow()
    }

    fun cancel() {
        clearGuide()
        coordinator.cancel()
    }

    fun prepareForAppKill() {
        clearGuide()
        schoolAnswerRequestJob?.cancel()
        schoolAnswerRequestJob = null
        schoolAnswerRequestGeneration += 1
        voiceTranscriptionJob?.cancel()
        voiceTranscriptionJob = null
        voiceTranscriptionGeneration += 1
        AppKillCoordinator(
            stopRobot = coordinator::stopForAppKill,
            kioskGateway = kioskGateway,
        ).prepareForAppKill()
    }

    fun emergencyStop() {
        clearGuide()
        coordinator.emergencyStop()
    }

    fun reset() {
        clearGuide()
        coordinator.reset()
    }

    override fun onCleared() {
        guideRequestJob?.cancel()
        schoolAnswerRequestJob?.cancel()
        basketRefreshJob?.cancel()
        basketActionJob?.cancel()
        voiceTranscriptionJob?.cancel()
        super.onCleared()
    }

    private fun isStillWaitingAt(stopId: String): Boolean {
        val current = coordinator.state.value
        return current.phase == DeliveryPhase.WAITING_FOR_PICKUP && current.currentStop?.id == stopId
    }

    private fun clearGuide() {
        guideRequestJob?.cancel()
        guideRequestJob = null
        guideRequestGeneration += 1
        guideLoad.value = SupplyGuideUiState()
    }

    private fun isAiAssistantAvailable(): Boolean =
        featureSettingsStore.current().aiAssistantEnabled && supplyGuideClient.enabled

    private fun handleRecognizedVoice(
        transcript: String,
        model: String,
        recognitionStatus: String,
        recognitionWarning: String? = null,
    ) {
        val normalized = transcript.trim()
        if (normalized.isEmpty()) return
        val recognizedState = VoiceInputUiState(
            transcript = normalized,
            model = model,
            warning = recognitionWarning,
            status = recognitionStatus,
        )

        when (
            val result = handleVoiceCommand(
                transcript = normalized,
                movementControlsEnabled = featureSettingsStore.current().deliveryControlsEnabled,
            )
        ) {
            VoiceCommandResult.NotACommand -> {
                if (isAiAssistantAvailable()) {
                    voiceInput.value = recognizedState
                    askSchoolQuestion(normalized)
                } else {
                    voiceInput.value = recognizedState.copy(
                        status = "음성 명령으로 인식되지 않았습니다. AI 질문 기능도 현재 사용할 수 없습니다.",
                        warning = combineWarnings(recognitionWarning, supplyGuideClient.statusMessage),
                    )
                }
            }

            is VoiceCommandResult.Executed -> {
                voiceInput.value = recognizedState.copy(
                    handledAsCommand = true,
                    status = result.message,
                    warning = combineWarnings(recognitionWarning, result.warning),
                )
            }

            is VoiceCommandResult.Rejected -> {
                val speechError = speakIfEnabled(result.message)
                voiceInput.value = recognizedState.copy(
                    handledAsCommand = true,
                    status = result.message,
                    warning = combineWarnings(
                        recognitionWarning,
                        speechError?.let { "거절 안내를 음성으로 출력하지 못했습니다." },
                    ),
                )
            }

            is VoiceCommandResult.Failed -> {
                voiceInput.value = recognizedState.copy(
                    handledAsCommand = true,
                    error = result.message,
                )
            }
        }
    }

    private fun combineWarnings(vararg warnings: String?): String? =
        warnings.filterNotNull().filter(String::isNotBlank).joinToString(" ").ifBlank { null }

    private fun speakIfEnabled(text: String): Throwable? {
        if (!featureSettingsStore.current().speechOutputEnabled) return null
        return runCatching { robotGateway.speak(text) }.exceptionOrNull()
    }

    data class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(DeliveryViewModel::class.java))
            return DeliveryViewModel(
                coordinator = container.deliveryCoordinator,
                routeRepository = container.routeRepository,
                robotGateway = container.robotGateway,
                supplyGuideClient = container.supplyGuideClient,
                kioskGateway = container.kioskGateway,
                basketClient = container.basketClient,
                featureSettingsStore = container.featureSettingsStore,
                handleVoiceCommand = container.handleVoiceCommand,
            ) as T
        }
    }
}

private data class RouteLoadState(
    val isLoading: Boolean = false,
    val route: DeliveryRoute? = null,
    val error: String? = null,
)

data class SupplyGuideUiState(
    val stopId: String? = null,
    val isLoading: Boolean = false,
    val explanation: String? = null,
    val source: SupplyGuideSource? = null,
    val model: String? = null,
    val warning: String? = null,
    val error: String? = null,
)

data class SchoolAnswerUiState(
    val isLoading: Boolean = false,
    val answer: SchoolAnswer? = null,
    val error: String? = null,
)

private data class BasketRefreshState(
    val isLoading: Boolean = false,
    val error: String? = null,
)

private data class BasketActionState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

data class VoiceInputUiState(
    val isRecording: Boolean = false,
    val isTranscribing: Boolean = false,
    val transcript: String? = null,
    val model: String? = null,
    val warning: String? = null,
    val handledAsCommand: Boolean = false,
    val status: String = "음성 명령 예: '홈베이스로 가', '멈춰', '나를 따라와', '<저장 위치>로 가'",
    val error: String? = null,
)

data class BasketUiState(
    val enabled: Boolean = false,
    val featureStatus: String = "바구니 설정을 확인해 주세요.",
    val connection: BasketConnectionState = BasketConnectionState.DISABLED,
    val snapshot: BasketSnapshot = BasketSnapshot.unknown(),
    val isRefreshing: Boolean = false,
    val isActing: Boolean = false,
    val actionMessage: String? = null,
    val error: String? = null,
)

data class DeliveryUiState(
    val isLoadingRoute: Boolean = true,
    val route: DeliveryRoute? = null,
    val routeError: String? = null,
    val mission: DeliverySnapshot = DeliverySnapshot(),
    val robotReady: Boolean = false,
    val savedLocations: List<String> = emptyList(),
    val currentPose: RobotPose? = null,
    val kiosk: KioskState = KioskState(),
    val guideFeatureEnabled: Boolean = false,
    val guideFeatureStatus: String = "AI 안내 설정을 확인해 주세요.",
    val guide: SupplyGuideUiState = SupplyGuideUiState(),
    val schoolAnswer: SchoolAnswerUiState = SchoolAnswerUiState(),
    val basket: BasketUiState = BasketUiState(),
    val voiceInput: VoiceInputUiState = VoiceInputUiState(),
    val featureSettings: FeatureSettings = FeatureSettings(),
)
