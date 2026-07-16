package com.example.switchgo.usb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log

class UsbPermissionHandler(
    private val onPermissionResult: (granted: Boolean) -> Unit,
) {
    companion object {
        private const val TAG = "UsbPermissionHandler"
        private const val ACTION_USB_PERMISSION = "com.example.switchgo.USB_PERMISSION"
    }

    private var permissionReceiver: BroadcastReceiver? = null

    fun requestPermission(context: Context) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager ?: run {
            Log.e(TAG, "USB service not available")
            onPermissionResult(false)
            return
        }

        val device = findSwitchGoDevice(usbManager)
        if (device == null) {
            Log.w(TAG, "No SwitchGo USB device found")
            onPermissionResult(true)
            return
        }

        if (usbManager.hasPermission(device)) {
            Log.d(TAG, "USB permission already granted for ${device.deviceName}")
            onPermissionResult(true)
            return
        }

        Log.d(TAG, "Requesting USB permission for ${device.deviceName}")
        val permissionIntent = PendingIntent.getBroadcast(
            context, 0, Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        usbManager.requestPermission(device, permissionIntent)
    }

    fun register(context: Context) {
        val receiver = createReceiver()
        permissionReceiver = receiver
        context.registerReceiver(receiver, IntentFilter(ACTION_USB_PERMISSION))
    }

    fun unregister(context: Context) {
        permissionReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (e: IllegalArgumentException) {
            }
        }
        permissionReceiver = null
    }

    private fun createReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != ACTION_USB_PERMISSION) return
                @Suppress("DEPRECATION")
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                Log.d(TAG, "USB permission result: device=${device?.deviceName}, granted=$granted")
                onPermissionResult(granted)
            }
        }
    }

    private fun findSwitchGoDevice(usbManager: UsbManager): UsbDevice? {
        return usbManager.deviceList.values.firstOrNull { device ->
            device.interfaceCount > 0
        }
    }
}
