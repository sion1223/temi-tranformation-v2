package com.robocore.temisamplecode.utils

import android.util.Log
import android.widget.Toast
import com.robotemi.sdk.Robot
import com.robotemi.sdk.SttLanguage
import com.robotemi.sdk.listeners.OnConversationStatusChangedListener
import com.robotemi.sdk.permission.OnRequestPermissionResultListener
import com.robotemi.sdk.permission.Permission
import kotlinx.coroutines.channels.Channel

class RobotAsr :
    Robot.AsrListener,
    Robot.ConversationViewAttachesListener,
    OnConversationStatusChangedListener {

    val robot by lazy {
        Robot.getInstance()
    }


    // singleton
    companion object {
        private const val TAG = "RobotAsrHandler"
    }

    val asrChannel2: Channel<Pair<Int, String>> = Channel()

    fun listenSpeech() {
        robot.wakeup()
    }

    fun start() {
        Robot.getInstance().addAsrListener(this)
        Robot.getInstance().addOnConversationStatusChangedListener(this)
        Robot.getInstance().addConversationViewAttachesListener(this)
    }


    fun stop() {
        Robot.getInstance().removeAsrListener(this)
        Robot.getInstance().removeOnConversationStatusChangedListener(this)
        Robot.getInstance().removeConversationViewAttachesListener(this)
    }


    override fun onAsrResult(asrResult: String, sttLanguage: SttLanguage) {
        Log.d(TAG, "onAsrResutl:: $asrResult, ${sttLanguage.value}")
//        if (asrResult.isNotEmpty()) {
//            asrChannel.trySend(asrResult)
//        }
    }

    override fun onConversationAttaches(isAttached: Boolean) {
        Log.d(TAG, isAttached.toString())
    }

    override fun onConversationStatusChanged(status: Int, text: String) {

        // 0 == idle
        // 1 == listening
        // 2 == thinking
        // 3 == speaking

        Log.d(TAG, "$status -- $text")
        asrChannel2.trySend(Pair(status, text))
    }
}