package kr.hs.gwangyang.temidelivery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.robotemi.sdk.Robot
import com.robotemi.sdk.listeners.OnRobotReadyListener
import java.util.concurrent.atomic.AtomicBoolean

/**
 * temi sends this broadcast after boot. The selected Kiosk app is relaunched by
 * temi's launcher; this receiver only waits for SDK readiness and never starts an
 * Activity or runs a watchdog from the background.
 */
class TemiBootCompleteReceiver : BroadcastReceiver(), OnRobotReadyListener {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != ACTION_BOOT_COMPLETED) return
        if (listenerRegistered.compareAndSet(false, true)) {
            Robot.getInstance().addOnRobotReadyListener(this)
        }
    }

    override fun onRobotReady(isReady: Boolean) {
        if (isReady) {
            Robot.getInstance().removeOnRobotReadyListener(this)
            listenerRegistered.set(false)
        }
    }

    private companion object {
        const val ACTION_BOOT_COMPLETED = "com.robotemi.intent.action.BOOT_COMPLETED"
        val listenerRegistered = AtomicBoolean(false)
    }
}
