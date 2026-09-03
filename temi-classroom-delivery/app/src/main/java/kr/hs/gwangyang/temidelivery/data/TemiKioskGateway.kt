package kr.hs.gwangyang.temidelivery.data

import com.robotemi.sdk.Robot
import com.robotemi.sdk.constants.HomeScreenMode
import com.robotemi.sdk.permission.Permission

data class KioskState(
    val sdkReady: Boolean = false,
    val selectedKioskApp: Boolean = false,
    val kioskModeOn: Boolean = false,
    val settingsPermissionGranted: Boolean = false,
    val error: String? = null,
)

interface KioskGateway {
    fun readState(): KioskState

    /** Request one-time administrator setup; never invoke this automatically on boot. */
    fun requestSetup(): Result<Unit> = Result.success(Unit)

    /** Disable Kiosk before an administrator-requested process kill, when possible. */
    fun disableForExit(): Result<Unit>
}

/** Production adapter for temi's Kiosk APIs. */
class TemiKioskGateway(
    private val robot: Robot,
) : KioskGateway {
    override fun readState(): KioskState {
        if (!robot.isReady) return KioskState()

        val selected = runCatching { robot.isSelectedKioskApp() }
            .getOrElse { return KioskState(sdkReady = true, error = it.message) }
        val enabled = runCatching { robot.isKioskModeOn() }
            .getOrElse { return KioskState(sdkReady = true, error = it.message) }
        val settingsPermissionGranted = runCatching {
            robot.checkSelfPermission(Permission.SETTINGS) == Permission.GRANTED
        }.getOrElse { return KioskState(sdkReady = true, error = it.message) }
        return KioskState(
            sdkReady = true,
            selectedKioskApp = selected,
            kioskModeOn = enabled,
            settingsPermissionGranted = settingsPermissionGranted,
        )
    }

    override fun requestSetup(): Result<Unit> {
        if (!robot.isReady) {
            return Result.failure(
                IllegalStateException("temi 제어 서비스가 준비되지 않아 Kiosk 설정을 시작할 수 없습니다."),
            )
        }

        val selected = runCatching { robot.isSelectedKioskApp() }.getOrElse {
            return Result.failure(IllegalStateException("현재 Kiosk 앱을 확인하지 못했습니다.", it))
        }
        if (!selected) {
            return runCatching { robot.requestToBeKioskApp() }
        }

        val permissionGranted = runCatching {
            robot.checkSelfPermission(Permission.SETTINGS) == Permission.GRANTED
        }.getOrElse {
            return Result.failure(IllegalStateException("temi Settings 권한을 확인하지 못했습니다.", it))
        }
        if (permissionGranted) return Result.success(Unit)

        return runCatching {
            robot.requestPermissions(listOf(Permission.SETTINGS), SETTINGS_PERMISSION_REQUEST_CODE)
        }
    }

    override fun disableForExit(): Result<Unit> {
        if (!robot.isReady) {
            return Result.failure(
                IllegalStateException("temi 제어 서비스가 준비되지 않아 Kiosk를 해제할 수 없습니다."),
            )
        }

        val enabled = runCatching { robot.isKioskModeOn() }.getOrElse {
            return Result.failure(
                IllegalStateException("Kiosk 상태를 확인하지 못했습니다.", it),
            )
        }
        if (!enabled) return Result.success(Unit)

        val selected = runCatching { robot.isSelectedKioskApp() }.getOrElse {
            return Result.failure(
                IllegalStateException("현재 Kiosk 앱을 확인하지 못했습니다.", it),
            )
        }
        if (!selected) {
            return Result.failure(
                IllegalStateException("이 앱이 현재 Kiosk 앱으로 선택되지 않았습니다."),
            )
        }

        val settingsPermission = runCatching {
            robot.checkSelfPermission(Permission.SETTINGS)
        }.getOrElse {
            return Result.failure(
                IllegalStateException("temi Settings 권한을 확인하지 못했습니다.", it),
            )
        }
        if (settingsPermission != Permission.GRANTED) {
            return Result.failure(
                SecurityException("temi Settings 권한이 없어 Kiosk를 해제할 수 없습니다."),
            )
        }

        runCatching {
            robot.setKioskModeOn(false, HomeScreenMode.DEFAULT)
        }.onFailure {
            return Result.failure(it)
        }

        val stillEnabled = runCatching { robot.isKioskModeOn() }.getOrElse {
            return Result.failure(
                IllegalStateException("Kiosk 해제 결과를 확인하지 못했습니다.", it),
            )
        }
        return if (stillEnabled) {
            Result.failure(
                IllegalStateException("temi Kiosk 해제가 확인되지 않았습니다."),
            )
        } else {
            Result.success(Unit)
        }
    }

    private companion object {
        const val SETTINGS_PERMISSION_REQUEST_CODE = 1401
    }
}
