package kr.hs.gwangyang.temidelivery.basket

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * HTTP/JSON adapter for an ESP32 Arduino basket controller.
 *
 * Each mutating command is sent exactly once. A request timeout is not retried because
 * the controller may have applied the servo action before the response was lost.
 */
class HttpBasketGateway(
    private val config: BasketClientConfig,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) : BasketGateway {
    private val statusEndpoint = URL("${config.baseUrl.trimEnd('/')}/api/v1/basket/status")
    private val commandEndpoint = URL("${config.baseUrl.trimEnd('/')}/api/v1/basket/commands")

    private val _connectionState = MutableStateFlow(BasketConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<BasketConnectionState> = _connectionState.asStateFlow()

    private val _status = MutableStateFlow(BasketSnapshot.unknown(config.expectedDeviceId.ifBlank { "unknown" }))
    override val status: StateFlow<BasketSnapshot> = _status.asStateFlow()

    private val _events = MutableSharedFlow<BasketEvent>(
        extraBufferCapacity = 32,
    )
    override val events: Flow<BasketEvent> = _events.asSharedFlow()

    private var lastStatusAtMillis: Long? = null

    override suspend fun refresh(): Result<BasketSnapshot> = withContext(Dispatchers.IO) {
        _connectionState.value = BasketConnectionState.CONNECTING
        _events.tryEmit(BasketEvent.ConnectionChanged(BasketConnectionState.CONNECTING))
        val result = capture { parseStatusEnvelope(request("GET", statusEndpoint)) }
        result.fold(
            onSuccess = { snapshot ->
                acceptStatus(snapshot)
                Result.success(snapshot)
            },
            onFailure = { cause ->
                markFailure(null, cause)
                Result.failure(cause)
            },
        )
    }

    override suspend fun prepareMission(mission: BasketMission): Result<Unit> = executeCommand(
        command = BasketCommandType.PREPARE_MISSION,
        fields = JSONObject()
            .put("missionId", mission.missionId.trim())
            .put("stopId", mission.stopId.trim())
            .put("expectedQuantity", mission.expectedQuantity)
            .apply {
                mission.expectedWeightGrams?.let { put("expectedWeightGrams", it) }
                mission.weightToleranceGrams?.let { put("weightToleranceGrams", it) }
            },
    )

    override suspend fun unlock(request: BasketUnlockRequest): Result<Unit> {
        val freshness = ensureFreshAndSafeForUnlock()
        if (freshness.isFailure) return freshness
        return executeCommand(
            command = BasketCommandType.UNLOCK,
            fields = JSONObject()
                .put("missionId", request.missionId.trim())
                .put("stopId", request.stopId.trim())
                .put("expectedQuantity", request.expectedQuantity)
                .apply {
                    request.expectedWeightGrams?.let { put("expectedWeightGrams", it) }
                    request.weightToleranceGrams?.let { put("weightToleranceGrams", it) }
                },
        )
    }

    override suspend fun lock(request: BasketLockRequest): Result<Unit> = executeCommand(
        command = BasketCommandType.LOCK,
        fields = JSONObject()
            .put("missionId", request.missionId.trim())
            .put("stopId", request.stopId.trim()),
    )

    override suspend fun safeState(reason: String): Result<Unit> {
        if (reason.length > MAX_REASON_LENGTH) {
            return Result.failure(BasketSafetyException("안전 상태 사유가 너무 깁니다."))
        }
        return executeCommand(
            command = BasketCommandType.SAFE_STATE,
            fields = JSONObject().put("reason", reason.trim()),
        )
    }

    private suspend fun ensureFreshAndSafeForUnlock(): Result<Unit> {
        val state = _connectionState.value
        if (state != BasketConnectionState.READY || !isStatusFresh()) {
            if (state == BasketConnectionState.READY && !isStatusFresh()) {
                _connectionState.value = BasketConnectionState.STALE
                _events.tryEmit(BasketEvent.ConnectionChanged(BasketConnectionState.STALE))
            }
            return Result.failure(
                BasketSafetyException("바구니 상태가 최신이 아니어서 잠금 해제를 차단했습니다."),
            )
        }
        val snapshot = _status.value
        if (snapshot.door != BasketDoorState.CLOSED || snapshot.lock != BasketLockState.LOCKED) {
            return Result.failure(
                BasketSafetyException("바구니 문이 닫히고 잠긴 상태가 아니어서 열지 않습니다."),
            )
        }
        if (snapshot.sensor != BasketSensorState.OK || snapshot.loadState != BasketLoadState.OK) {
            return Result.failure(
                BasketSafetyException("바구니 센서 또는 적재 상태가 안전하지 않아 열지 않습니다."),
            )
        }
        return Result.success(Unit)
    }

    private suspend fun executeCommand(
        command: BasketCommandType,
        fields: JSONObject,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val requestId = UUID.randomUUID().toString()
        val requestBody = JSONObject(fields.toString())
            .put("protocolVersion", BASKET_PROTOCOL_VERSION)
            .put("requestId", requestId)
            .put("command", command.name)
            .toString()
            .toByteArray(Charsets.UTF_8)

        val result = capture {
            val root = request("POST", commandEndpoint, requestBody)
            parseCommandEnvelope(root, requestId, command)
        }
        result.fold(
            onSuccess = { acknowledged ->
                acceptStatus(acknowledged.status)
                _events.tryEmit(
                    BasketEvent.CommandAcknowledged(requestId, command, acknowledged.status),
                )
                Result.success(Unit)
            },
            onFailure = { cause ->
                markFailure(command, cause)
                Result.failure(cause)
            },
        )
    }

    private fun parseStatusEnvelope(root: JSONObject): BasketSnapshot {
        requireSuccess(root)
        val data = root.getJSONObject("data")
        return parseSnapshot(data.getJSONObject("status"))
    }

    private fun parseCommandEnvelope(
        root: JSONObject,
        expectedRequestId: String,
        command: BasketCommandType,
    ): CommandAcknowledgement {
        requireSuccess(root)
        val data = root.getJSONObject("data")
        val actualRequestId = data.getString("requestId")
        if (actualRequestId != expectedRequestId) {
            throw BasketProtocolException("바구니 응답 requestId가 요청과 일치하지 않습니다.")
        }
        val snapshot = parseSnapshot(data.getJSONObject("status"))
        when (command) {
            BasketCommandType.PREPARE_MISSION -> {
                if (!snapshot.isSafeForPreparation()) {
                    throw BasketSafetyException("바구니 준비 후에도 잠금·문·센서 상태가 안전하지 않습니다.")
                }
            }

            BasketCommandType.UNLOCK -> {
                if (snapshot.lock != BasketLockState.UNLOCKED) {
                    throw BasketSafetyException("바구니 잠금 해제 결과를 확인할 수 없습니다.")
                }
            }

            BasketCommandType.LOCK,
            BasketCommandType.SAFE_STATE,
            -> {
                if (snapshot.lock != BasketLockState.LOCKED || snapshot.door != BasketDoorState.CLOSED) {
                    throw BasketSafetyException("바구니 안전 잠금 결과를 확인할 수 없습니다.")
                }
            }
        }
        return CommandAcknowledgement(snapshot)
    }

    private fun parseSnapshot(status: JSONObject): BasketSnapshot {
        val protocolVersion = status.getInt("protocolVersion")
        if (protocolVersion != BASKET_PROTOCOL_VERSION) {
            throw BasketProtocolException("지원하지 않는 바구니 프로토콜 버전입니다: $protocolVersion")
        }
        val deviceId = requiredString(status, "deviceId")
        if (config.expectedDeviceId.isNotBlank() && deviceId != config.expectedDeviceId) {
            throw BasketProtocolException("예상한 바구니 장치가 아닙니다: $deviceId")
        }
        val firmwareVersion = requiredString(status, "firmwareVersion")
        val sequence = status.getLong("sequence")
        val uptimeMs = status.getLong("uptimeMs")
        if (sequence < 0 || uptimeMs < 0) {
            throw BasketProtocolException("바구니 sequence/uptimeMs는 음수일 수 없습니다.")
        }
        val weightGrams = status.optNullableInt("weightGrams")
        if (weightGrams != null && weightGrams < 0) {
            throw BasketProtocolException("바구니 weightGrams는 음수일 수 없습니다.")
        }
        return BasketSnapshot(
            protocolVersion = protocolVersion,
            deviceId = deviceId,
            firmwareVersion = firmwareVersion,
            sequence = sequence,
            uptimeMs = uptimeMs,
            door = parseEnum(status, "door", BasketDoorState.UNKNOWN),
            lock = parseEnum(status, "lock", BasketLockState.UNKNOWN),
            sensor = parseEnum(status, "sensor", BasketSensorState.UNKNOWN),
            weightGrams = weightGrams,
            loadState = parseEnum(status, "loadState", BasketLoadState.UNKNOWN),
        )
    }

    private fun request(
        method: String,
        endpoint: URL,
        requestBody: ByteArray? = null,
    ): JSONObject {
        val connection = (endpoint.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = config.connectTimeoutMillis
            readTimeout = config.readTimeoutMillis
            useCaches = false
            instanceFollowRedirects = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer ${config.deviceToken}")
            if (requestBody != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setFixedLengthStreamingMode(requestBody.size)
            }
        }
        try {
            if (requestBody != null) {
                connection.outputStream.use { it.write(requestBody) }
            }
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val responseBody = stream?.use { readLimitedUtf8(it) }.orEmpty()
            val root = runCatching { JSONObject(responseBody) }.getOrElse { cause ->
                throw BasketProtocolException("바구니 응답이 올바른 JSON이 아닙니다.", cause)
            }
            if (responseCode !in 200..299) throw parseHttpError(responseCode, root)
            return root
        } finally {
            connection.disconnect()
        }
    }

    private fun parseHttpError(statusCode: Int, root: JSONObject): BasketHttpException {
        val error = root.optJSONObject("error")
        return BasketHttpException(
            statusCode = statusCode,
            errorCode = error?.optString("code", "http_error") ?: "http_error",
            message = error?.optString("message", "바구니 서버 오류가 발생했습니다.")
                ?: "바구니 서버가 HTTP $statusCode 오류를 반환했습니다.",
        )
    }

    private fun requireSuccess(root: JSONObject) {
        if (!root.optBoolean("success", false)) {
            throw BasketProtocolException("바구니 응답이 실패 상태입니다.")
        }
    }

    private fun acceptStatus(snapshot: BasketSnapshot) {
        _status.value = snapshot
        lastStatusAtMillis = nowMillis()
        _connectionState.value = BasketConnectionState.READY
        _events.tryEmit(BasketEvent.StatusChanged(snapshot))
        _events.tryEmit(BasketEvent.ConnectionChanged(BasketConnectionState.READY))
    }

    private fun markFailure(command: BasketCommandType?, cause: Throwable) {
        _connectionState.value = if (lastStatusAtMillis == null) {
            BasketConnectionState.ERROR
        } else {
            BasketConnectionState.STALE
        }
        _events.tryEmit(BasketEvent.ConnectionChanged(_connectionState.value, cause.message))
        _events.tryEmit(BasketEvent.Error(command, cause))
    }

    private fun isStatusFresh(): Boolean {
        val last = lastStatusAtMillis ?: return false
        return nowMillis() - last in 0..config.staleAfterMillis
    }

    private fun requiredString(json: JSONObject, name: String): String {
        val value = json.optString(name, "").trim()
        if (value.isBlank()) throw BasketProtocolException("바구니 응답의 $name 값이 비어 있습니다.")
        return value
    }

    private inline fun <reified T : Enum<T>> parseEnum(
        json: JSONObject,
        name: String,
        unknown: T,
    ): T = runCatching {
        enumValueOf<T>(json.getString(name).trim().uppercase())
    }.getOrDefault(unknown)

    private fun JSONObject.optNullableInt(name: String): Int? {
        if (isNull(name) || !has(name)) return null
        return optInt(name, Int.MIN_VALUE).takeIf { it != Int.MIN_VALUE }
    }

    private fun readLimitedUtf8(input: InputStream): String {
        val buffer = ByteArray(4 * 1024)
        var total = 0
        val output = java.io.ByteArrayOutputStream()
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            total += count
            if (total > config.maxResponseBytes) {
                throw BasketProtocolException("바구니 응답이 너무 큽니다.")
            }
            output.write(buffer, 0, count)
        }
        return output.toByteArray().toString(Charsets.UTF_8)
    }

    private fun BasketSnapshot.isSafeForPreparation() =
        door == BasketDoorState.CLOSED && lock == BasketLockState.LOCKED &&
            sensor == BasketSensorState.OK && loadState == BasketLoadState.OK

    private data class CommandAcknowledgement(val status: BasketSnapshot)

    private fun <T> capture(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (cause: Throwable) {
        Result.failure(cause)
    }

    private companion object {
        const val MAX_REASON_LENGTH = 200
    }
}
