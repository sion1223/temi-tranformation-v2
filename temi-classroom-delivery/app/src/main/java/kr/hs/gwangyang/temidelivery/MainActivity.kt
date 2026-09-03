package kr.hs.gwangyang.temidelivery

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.robotemi.sdk.Robot
import kr.hs.gwangyang.temidelivery.aiguide.SupplyGuideSource
import kr.hs.gwangyang.temidelivery.basket.BasketConnectionState
import kr.hs.gwangyang.temidelivery.databinding.ActivityMainBinding
import kr.hs.gwangyang.temidelivery.domain.DeliveryPhase
import kr.hs.gwangyang.temidelivery.domain.FailureTarget
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var kioskBackCallback: OnBackPressedCallback
    private lateinit var temiVoiceController: TemiVoiceActivationController
    private val recorder by lazy { Pcm16WavRecorder() }
    private var currentSettings = FeatureSettings()
    private var settingsFormInitialized = false
    private var activityStarted = false
    private var startAfterPermission = false
    private var lastRenderedTranscript: String? = null

    private val microphonePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted && startAfterPermission) startMicrophoneRecording()
        if (!granted) {
            viewModel.reportVoiceError(IllegalStateException(getString(R.string.voice_permission_denied)))
        }
        startAfterPermission = false
    }

    private val viewModel: DeliveryViewModel by viewModels {
        DeliveryViewModel.Factory((application as TemiDeliveryApplication).container)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        kioskBackCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // temi Kiosk owns the home screen. Consume Back while it is active.
            }
        }
        onBackPressedDispatcher.addCallback(this, kioskBackCallback)

        temiVoiceController = TemiVoiceActivationController(
            robot = Robot.getInstance(),
            onTranscript = viewModel::acceptTemiVoiceTranscript,
            onStatus = viewModel::updateVoiceStatus,
        )
        binding.spinnerVoiceMode.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf(
                getString(R.string.voice_mode_tap),
                getString(R.string.voice_mode_hold),
                getString(R.string.voice_mode_temi),
            ),
        )

        binding.btnRefresh.setOnClickListener { viewModel.refreshRobotInfo() }
        binding.btnFeatureSettings.setOnClickListener {
            binding.groupFeatureSettings.isVisible = !binding.groupFeatureSettings.isVisible
        }
        binding.btnSaveFeatureSettings.setOnClickListener { saveFeatureSettings() }
        binding.btnKioskSetup.setOnClickListener {
            viewModel.requestKioskSetup()
                .onFailure { error -> showKioskSetupFailure(error) }
                .onSuccess { viewModel.refreshRobotInfo() }
        }
        binding.btnKillApp.setOnClickListener { killAppImmediately() }
        binding.btnAskSchool.setOnClickListener {
            viewModel.askSchoolQuestion(binding.etSchoolQuestion.text?.toString().orEmpty())
        }
        binding.btnRefreshBasket.setOnClickListener { viewModel.refreshBasket() }
        binding.btnBasketSafeLock.setOnClickListener { viewModel.requestBasketSafeLock() }
        binding.btnVoiceQuestion.setOnClickListener {
            when (currentSettings.voiceActivationMode) {
                VoiceActivationMode.TAP_TO_TALK -> {
                    if (recorder.isRecording) stopMicrophoneRecording() else requestMicrophoneStart(true)
                }
                VoiceActivationMode.HOLD_TO_TALK -> Unit
                VoiceActivationMode.TEMI_WAKE_WORD -> temiVoiceController.askNow()
                    .onFailure(viewModel::reportVoiceError)
            }
        }
        binding.btnVoiceQuestion.setOnTouchListener { view, event ->
            if (currentSettings.voiceActivationMode != VoiceActivationMode.HOLD_TO_TALK) {
                return@setOnTouchListener false
            }
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    requestMicrophoneStart(false)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (recorder.isRecording) stopMicrophoneRecording()
                    view.performClick()
                    true
                }
                else -> true
            }
        }
        binding.btnStart.setOnClickListener { viewModel.startDelivery() }
        binding.btnExplainSupply.setOnClickListener { viewModel.requestSupplyGuide() }
        binding.btnConfirmPickup.setOnClickListener { viewModel.confirmPickup() }
        binding.btnRetry.setOnClickListener { viewModel.retry() }
        binding.btnSkip.setOnClickListener { viewModel.skipCurrentStop() }
        binding.btnReturn.setOnClickListener { viewModel.returnNow() }
        binding.btnCancel.setOnClickListener { viewModel.cancel() }
        binding.btnEmergencyStop.setOnClickListener { viewModel.emergencyStop() }
        binding.btnReset.setOnClickListener { viewModel.reset() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activityStarted = true
        applyFeatureSettings(currentSettings)
        viewModel.refreshRobotInfo()
    }

    override fun onStop() {
        activityStarted = false
        val wasRecording = recorder.isRecording
        recorder.cancel()
        if (wasRecording) viewModel.updateVoiceStatus("녹음을 취소했습니다.")
        temiVoiceController.stop()
        super.onStop()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) applyScreenPolicy(currentSettings)
    }

    @Suppress("DEPRECATION")
    private fun applyScreenPolicy(settings: FeatureSettings) {
        if (settings.keepScreenOnEnabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        window.decorView.systemUiVisibility = if (settings.immersiveModeEnabled) {
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        } else {
            View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    private fun applyFeatureSettings(settings: FeatureSettings) {
        if (
            recorder.isRecording &&
            (!settings.voiceInputEnabled || settings.voiceActivationMode != currentSettings.voiceActivationMode)
        ) {
            recorder.cancel()
            viewModel.updateVoiceStatus("음성 입력 방식이 바뀌어 녹음을 취소했습니다.")
        }
        currentSettings = settings
        applyScreenPolicy(settings)
        kioskBackCallback.isEnabled = settings.blockBackEnabled
        if (
            activityStarted && settings.voiceInputEnabled &&
            settings.voiceActivationMode == VoiceActivationMode.TEMI_WAKE_WORD
        ) {
            temiVoiceController.start()
        } else {
            temiVoiceController.stop()
        }
    }

    private fun populateSettingsForm(settings: FeatureSettings) = with(binding) {
        switchAiAssistant.isChecked = settings.aiAssistantEnabled
        switchSpeechOutput.isChecked = settings.speechOutputEnabled
        switchVoiceInput.isChecked = settings.voiceInputEnabled
        switchKeepScreenOn.isChecked = settings.keepScreenOnEnabled
        switchImmersive.isChecked = settings.immersiveModeEnabled
        switchBlockBack.isChecked = settings.blockBackEnabled
        switchDeliveryControls.isChecked = settings.deliveryControlsEnabled
        switchBasketIntegration.isChecked = settings.basketIntegrationEnabled
        spinnerVoiceMode.setSelection(settings.voiceActivationMode.ordinal)
        etBasketUrl.setText(settings.basketBaseUrl)
        etBasketDeviceId.setText(settings.basketDeviceId)
        etBasketToken.setText(settings.basketDeviceToken)
        settingsFormInitialized = true
    }

    private fun saveFeatureSettings() {
        val mode = VoiceActivationMode.entries.getOrElse(binding.spinnerVoiceMode.selectedItemPosition) {
            VoiceActivationMode.TAP_TO_TALK
        }
        val settings = FeatureSettings(
            aiAssistantEnabled = binding.switchAiAssistant.isChecked,
            speechOutputEnabled = binding.switchSpeechOutput.isChecked,
            voiceInputEnabled = binding.switchVoiceInput.isChecked,
            voiceActivationMode = mode,
            keepScreenOnEnabled = binding.switchKeepScreenOn.isChecked,
            immersiveModeEnabled = binding.switchImmersive.isChecked,
            blockBackEnabled = binding.switchBlockBack.isChecked,
            deliveryControlsEnabled = binding.switchDeliveryControls.isChecked,
            basketIntegrationEnabled = binding.switchBasketIntegration.isChecked,
            basketBaseUrl = binding.etBasketUrl.text?.toString().orEmpty(),
            basketDeviceId = binding.etBasketDeviceId.text?.toString().orEmpty(),
            basketDeviceToken = binding.etBasketToken.text?.toString().orEmpty(),
        )
        viewModel.saveFeatureSettings(settings)
            .onSuccess {
                applyFeatureSettings(settings)
                Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show()
            }
            .onFailure { error ->
                AlertDialog.Builder(this)
                    .setTitle(R.string.feature_settings)
                    .setMessage(getString(R.string.settings_failed, error.message ?: error.javaClass.simpleName))
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
    }

    private fun requestMicrophoneStart(resumeAfterPermission: Boolean) {
        if (!currentSettings.voiceInputEnabled || !currentSettings.aiAssistantEnabled) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            startAfterPermission = resumeAfterPermission
            microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        startMicrophoneRecording()
    }

    private fun startMicrophoneRecording() {
        recorder.start { result ->
            result.onSuccess(viewModel::transcribeVoice)
                .onFailure(viewModel::reportVoiceError)
        }.onSuccess {
            viewModel.updateVoiceStatus(getString(R.string.voice_recording), recording = true)
        }.onFailure(viewModel::reportVoiceError)
    }

    private fun stopMicrophoneRecording() {
        recorder.stop()
            .onSuccess(viewModel::transcribeVoice)
            .onFailure(viewModel::reportVoiceError)
    }

    private fun killAppImmediately() {
        binding.btnKillApp.isEnabled = false

        // Robot stop and Kiosk release are best-effort. Neither is allowed to keep
        // the administrator inside the app when an immediate process kill was requested.
        runCatching { viewModel.prepareForAppKill() }
        runCatching { finishAndRemoveTask() }

        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(0)
    }

    private fun showKioskSetupFailure(error: Throwable) {
        AlertDialog.Builder(this)
            .setTitle(R.string.kiosk_setup_failed_title)
            .setMessage(
                getString(
                    R.string.kiosk_setup_failed_message,
                    error.message ?: error.javaClass.simpleName,
                ),
            )
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun render(ui: DeliveryUiState) = with(binding) {
        applyFeatureSettings(ui.featureSettings)
        if (!settingsFormInitialized) populateSettingsForm(ui.featureSettings)

        tvRobotConnection.text = if (ui.robotReady) {
            getString(R.string.robot_connected)
        } else {
            getString(R.string.robot_waiting)
        }
        tvRobotConnection.setTextColor(
            ContextCompat.getColor(
                this@MainActivity,
                if (ui.robotReady) R.color.status_ok else R.color.status_warning,
            ),
        )

        tvMissionPhase.text = ui.mission.phase.toKoreanLabel()
        tvMissionMessage.text = ui.routeError ?: ui.mission.message

        tvKioskStatus.text = when {
            ui.kiosk.error != null -> getString(R.string.kiosk_status_error, ui.kiosk.error)
            !ui.kiosk.sdkReady -> getString(R.string.kiosk_status_waiting)
            !ui.kiosk.selectedKioskApp -> getString(R.string.kiosk_setup_required)
            !ui.kiosk.settingsPermissionGranted -> getString(R.string.kiosk_permission_required)
            !ui.kiosk.kioskModeOn -> getString(R.string.kiosk_disabled)
            else -> getString(R.string.kiosk_enabled)
        }
        btnKioskSetup.isVisible = ui.kiosk.sdkReady &&
            (!ui.kiosk.selectedKioskApp || !ui.kiosk.settingsPermissionGranted)

        val aiAssistantVisible = ui.featureSettings.aiAssistantEnabled
        groupSchoolAssistant.isVisible = aiAssistantVisible || ui.featureSettings.voiceInputEnabled
        tvSchoolAssistantTitle.isVisible = aiAssistantVisible
        tvSchoolAssistantDescription.isVisible = aiAssistantVisible
        groupSchoolQuestion.isVisible = aiAssistantVisible
        tvSchoolAnswer.isVisible = aiAssistantVisible
        etSchoolQuestion.isEnabled = ui.guideFeatureEnabled && !ui.schoolAnswer.isLoading
        btnAskSchool.isEnabled = ui.guideFeatureEnabled && !ui.schoolAnswer.isLoading
        tvSchoolAnswer.text = when {
            !ui.guideFeatureEnabled -> ui.guideFeatureStatus
            ui.schoolAnswer.isLoading -> getString(R.string.school_answer_loading)
            ui.schoolAnswer.error != null ->
                getString(R.string.school_answer_error_format, ui.schoolAnswer.error)
            ui.schoolAnswer.answer != null -> formatSchoolAnswer(ui.schoolAnswer.answer)
            else -> getString(R.string.school_answer_ready)
        }

        groupVoiceInput.isVisible = ui.featureSettings.voiceInputEnabled
        val usesTemiAsr = ui.featureSettings.voiceActivationMode == VoiceActivationMode.TEMI_WAKE_WORD
        val selectedVoiceInputAvailable = if (usesTemiAsr) {
            ui.robotReady
        } else {
            ui.guideFeatureEnabled && !ui.schoolAnswer.isLoading
        }
        btnVoiceQuestion.isEnabled = selectedVoiceInputAvailable && !ui.voiceInput.isTranscribing
        btnVoiceQuestion.text = when {
            ui.voiceInput.isRecording -> getString(R.string.voice_question_stop)
            ui.featureSettings.voiceActivationMode == VoiceActivationMode.HOLD_TO_TALK ->
                getString(R.string.voice_question_hold)
            ui.featureSettings.voiceActivationMode == VoiceActivationMode.TEMI_WAKE_WORD ->
                getString(R.string.voice_question_temi)
            else -> getString(R.string.voice_question_start)
        }
        tvVoiceStatus.text = when {
            ui.voiceInput.error != null -> "음성 입력 오류: ${ui.voiceInput.error}"
            ui.voiceInput.warning != null -> "${ui.voiceInput.status}\n주의: ${ui.voiceInput.warning}"
            else -> ui.voiceInput.status
        }
        ui.voiceInput.transcript
            ?.takeIf { !ui.voiceInput.handledAsCommand && it != lastRenderedTranscript }
            ?.let { transcript ->
            lastRenderedTranscript = transcript
            etSchoolQuestion.setText(transcript)
            etSchoolQuestion.setSelection(transcript.length)
        }

        btnRefreshBasket.isEnabled = ui.basket.enabled && !ui.basket.isRefreshing && !ui.basket.isActing
        btnBasketSafeLock.isEnabled = ui.basket.enabled && !ui.basket.isRefreshing && !ui.basket.isActing
        tvBasketStatus.text = when {
            !ui.basket.enabled -> ui.basket.featureStatus
            ui.basket.isRefreshing -> getString(R.string.basket_loading)
            ui.basket.error != null -> getString(R.string.basket_error_format, ui.basket.error)
            ui.basket.actionMessage != null -> ui.basket.actionMessage
            ui.basket.connection == BasketConnectionState.READY -> buildString {
                appendLine(getString(R.string.basket_connected, ui.basket.snapshot.deviceId))
                append(
                    getString(
                        R.string.basket_state_format,
                        ui.basket.snapshot.door.name,
                        ui.basket.snapshot.lock.name,
                        ui.basket.snapshot.sensor.name,
                        ui.basket.snapshot.loadState.name,
                    ),
                )
                ui.basket.snapshot.weightGrams?.let {
                    append(" · ${getString(R.string.basket_weight_format, it)}")
                }
            }
            else -> getString(R.string.basket_connection_format, ui.basket.connection.name)
        }

        val route = ui.route
        tvRoute.text = when {
            ui.isLoadingRoute -> "배부 경로를 읽는 중입니다."
            route == null -> "배부 경로를 불러오지 못했습니다."
            else -> buildString {
                appendLine(route.name)
                route.stops.forEachIndexed { index, stop ->
                    append("${index + 1}. ${stop.destination.displayName} · ${stop.recipient} · ")
                    appendLine("${stop.supply} ${stop.quantity}개")
                }
                append("복귀: ${route.returnDestination.displayName} · 속도: ${route.speed}")
            }
        }

        val total = ui.mission.route?.stops?.size ?: route?.stops?.size ?: 0
        val completed = ui.mission.completedStopCount.coerceAtMost(total)
        tvProgress.text = getString(R.string.progress_format, completed, total)

        tvSavedLocations.text = if (ui.savedLocations.isEmpty()) {
            "저장 위치 없음 (또는 로봇 연결 대기 중)"
        } else {
            ui.savedLocations.joinToString(separator = " · ")
        }

        tvCurrentPose.text = ui.currentPose?.let { pose ->
            String.format(
                Locale.US,
                "현재 좌표  x=%.3f, y=%.3f, yaw=%.3f rad, 지도 내부=%s",
                pose.x,
                pose.y,
                pose.yaw,
                pose.isInMapArea?.toString() ?: "unknown",
            )
        } ?: "현재 좌표를 읽으려면 temi 연결 후 새로고침을 누르세요."

        val phase = ui.mission.phase
        val currentStop = ui.mission.currentStop
        val hasGuideItem = phase == DeliveryPhase.WAITING_FOR_PICKUP &&
            !currentStop?.guideItemId.isNullOrBlank()
        groupAiGuide.isVisible = hasGuideItem && ui.featureSettings.aiAssistantEnabled
        if (hasGuideItem) {
            tvGuideItemName.text = getString(R.string.ai_guide_item_format, currentStop.supply)
            val guide = ui.guide.takeIf { it.stopId == currentStop.id }
            btnExplainSupply.isEnabled = ui.guideFeatureEnabled && guide?.isLoading != true
            btnExplainSupply.text = if (guide?.explanation == null) {
                getString(R.string.explain_supply)
            } else {
                getString(R.string.explain_supply_again)
            }
            tvAiGuideStatus.text = when {
                !ui.guideFeatureEnabled -> ui.guideFeatureStatus
                guide == null -> getString(R.string.ai_guide_ready)
                guide.isLoading -> getString(R.string.ai_guide_loading)
                guide.error != null -> getString(R.string.ai_guide_error_format, guide.error)
                guide.explanation != null -> buildString {
                    val sourceLabel = when (guide.source) {
                        SupplyGuideSource.NVIDIA_NIM ->
                            guide.model ?: "NVIDIA NIM · DeepSeek V4 Flash"
                        SupplyGuideSource.LUNA -> guide.model ?: "Luna"
                        SupplyGuideSource.TEACHER_FALLBACK -> "선생님 입력 대체 안내"
                        null -> "안내"
                    }
                    appendLine(sourceLabel)
                    append(guide.explanation)
                    guide.warning?.let { append("\n\n주의: $it") }
                }
                else -> getString(R.string.ai_guide_ready)
            }
        }

        val deliveryEnabled = ui.featureSettings.deliveryControlsEnabled
        groupMissionOverview.isVisible = deliveryEnabled
        groupPrimaryActions.isVisible = deliveryEnabled
        groupSecondaryActions.isVisible = deliveryEnabled
        val canStart = deliveryEnabled && ui.robotReady && route != null && phase.canStartMission()
        btnStart.isEnabled = canStart
        btnConfirmPickup.isVisible = phase == DeliveryPhase.WAITING_FOR_PICKUP
        btnRetry.isVisible = phase == DeliveryPhase.FAILED &&
            ui.mission.failure?.target != FailureTarget.NONE
        btnSkip.isVisible = phase == DeliveryPhase.WAITING_FOR_PICKUP ||
            (phase == DeliveryPhase.FAILED &&
                ui.mission.failure?.target == FailureTarget.CURRENT_STOP)
        btnReturn.isVisible = phase.isControllable() && ui.mission.route != null
        btnCancel.isVisible = phase.isControllable()
        btnEmergencyStop.isEnabled = phase.isControllable()
        btnReset.isVisible = phase in setOf(
            DeliveryPhase.COMPLETED,
            DeliveryPhase.CONFIGURATION_ERROR,
            DeliveryPhase.CANCELLED,
            DeliveryPhase.EMERGENCY_STOPPED,
        )

        groupFailure.isVisible = phase == DeliveryPhase.FAILED ||
            phase == DeliveryPhase.CONFIGURATION_ERROR
        tvFailure.text = ui.mission.failure?.let { failure ->
            buildString {
                append(failure.description)
                failure.descriptionId?.let { append(" (코드 $it)") }
            }
        }.orEmpty()
    }

    private fun DeliveryPhase.toKoreanLabel() = when (this) {
        DeliveryPhase.IDLE -> "대기"
        DeliveryPhase.NAVIGATING -> "학생에게 이동 중"
        DeliveryPhase.WAITING_FOR_PICKUP -> "수령 확인 대기"
        DeliveryPhase.RETURNING -> "복귀 중"
        DeliveryPhase.COMPLETED -> "배부 완료"
        DeliveryPhase.FAILED -> "이동 실패"
        DeliveryPhase.CONFIGURATION_ERROR -> "설정 확인 필요"
        DeliveryPhase.CANCELLED -> "작업 취소됨"
        DeliveryPhase.EMERGENCY_STOPPED -> "긴급 정지"
    }

    private fun DeliveryPhase.isActive() = this == DeliveryPhase.NAVIGATING ||
        this == DeliveryPhase.WAITING_FOR_PICKUP || this == DeliveryPhase.RETURNING

    private fun DeliveryPhase.canStartMission() = this == DeliveryPhase.IDLE ||
        this == DeliveryPhase.COMPLETED || this == DeliveryPhase.CANCELLED ||
        this == DeliveryPhase.CONFIGURATION_ERROR

    private fun DeliveryPhase.isControllable() = isActive() || this == DeliveryPhase.FAILED ||
        this == DeliveryPhase.EMERGENCY_STOPPED
}
