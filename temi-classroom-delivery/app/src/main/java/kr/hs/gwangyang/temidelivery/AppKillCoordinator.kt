package kr.hs.gwangyang.temidelivery

import kr.hs.gwangyang.temidelivery.data.KioskGateway

/** Runs independent, best-effort cleanup immediately before killing the app process. */
class AppKillCoordinator(
    private val stopRobot: () -> Result<Unit>,
    private val kioskGateway: KioskGateway,
) {
    fun prepareForAppKill() {
        runCatching { stopRobot() }
        runCatching { kioskGateway.disableForExit() }
    }
}
