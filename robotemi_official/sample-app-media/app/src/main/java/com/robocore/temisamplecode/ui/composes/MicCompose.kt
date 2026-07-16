package com.robocore.temisamplecode.ui.composes

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.robocore.temisamplecode.R
import com.robocore.temisamplecode.utils.RobotAsr
import com.robotemi.sdk.Robot
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


@Composable
fun MicCompose() {

    val context = LocalContext.current

    var asrResult by remember {
        mutableStateOf("")
    }
    var asrState by remember {
        mutableStateOf("IDLE")
    }
    val asrHandler = remember {
        RobotAsr()
    }


    LaunchedEffect(asrHandler) {
        asrHandler.start()
        launch {
            asrHandler.asrChannel2.receiveAsFlow().collect {
                Log.d("MicCompose", "Received2: $it")
                asrState = when(it.first){
                    0 -> "IDLE"
                    1 -> "LISTENING"
                    2 -> "THINKING"
                    3 -> "SPEAKING"
                    else -> "IDLE"
                }

                if (asrState == "THINKING"){ // when it thinking, it is the final sentence of the speech
                    asrResult += it.second + " \n"
                }
            }
        }
    }

    DisposableEffect(Unit){
        onDispose {
            asrHandler.stop()
        }
    }

    fun checkAsrCondition(): Boolean {
        val isKiosk = Robot.getInstance().isKioskModeOn()
        val isAppKiosked = Robot.getInstance().isSelectedKioskApp()
        return isKiosk && isAppKiosked
    }


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Text(
            modifier = Modifier.fillMaxSize(),
            text = asrResult
        )
        Column(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.BottomCenter)
                .padding(0.dp, 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                Log.d("MicCompose", "Speak Button clicked")
                if (!checkAsrCondition()){
                    Toast.makeText(context, "Kiosk mode not enabled", Toast.LENGTH_SHORT).show()
                }


                if (asrState != "IDLE") {
                    asrHandler.stop()
                    return@Button
                }else{
                    asrHandler.listenSpeech()
                }

            }) {
                Icon(
                    painterResource(id = R.drawable.baseline_mic_24),
                    contentDescription = null
                )
                Text(
                    if (asrState == "IDLE") "Speak" else "..."
                )
            }
        }
    }
}


@Preview
@Composable
fun MicPreview() {
//    val robotAsr = RobotAsr()
    MicCompose()
}