package com.robocore.temisamplecode.ui.composes

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import cn.nodemedia.NodePlayer
import cn.nodemedia.NodePublisher


private const val TAG = "PlaybackScreen"

enum class RtmpState {
    STARTED,
    STOPPED,
    ERROR
}

data class RtmpStatus(
    val state: RtmpState,
    var url: String,
)


@Composable
fun RtmpUrlCompose(
    modifier: Modifier = Modifier,
    rtmpStatus: RtmpStatus,
    onUrlChange: (String) -> Unit,
    onPlayStateChanged: (RtmpState) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    Column(
        modifier = modifier
    ) {

        // url input
        TextField(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .hideKeyboardOnOutsideClick(),
            enabled = (rtmpStatus.state == RtmpState.STOPPED || rtmpStatus.state == RtmpState.ERROR),
            value = rtmpStatus.url,
            singleLine = true,
            onValueChange = onUrlChange,
            prefix = {
                Text("rtmp://")
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
        )

        Button(
            modifier = Modifier
                .padding(8.dp),
            shape = CircleShape,
            onClick = {
                val state =
                    if (rtmpStatus.state == RtmpState.STOPPED || rtmpStatus.state == RtmpState.ERROR)
                        RtmpState.STARTED
                    else RtmpState.STOPPED
                onPlayStateChanged(state)
                Log.d("status", "updated state: $state")
            }) {

            Icon(
                imageVector = if (rtmpStatus.state == RtmpState.STOPPED || rtmpStatus.state == RtmpState.ERROR)
                    Icons.Filled.PlayArrow else Icons.Filled.Close,
                contentDescription = "Start"
            )
        }
    }
}

@Composable
fun StreamingCompose(
    status: RtmpStatus,
    onUrlChange: (String) -> Unit,
    onPlayStateChanged: (RtmpState) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        // video view

        Column(
            modifier = Modifier.fillMaxWidth(0.5f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            StreamPublisher(
                modifier = Modifier.fillMaxHeight(0.5f).padding(8.dp),
                status,
                onPlayStateChanged
            )
            // Stream playback player
            StreamPlaybackCompose(
                modifier = Modifier.fillMaxHeight().padding(8.dp),
                status
            )
        }

        RtmpUrlCompose(
            modifier = Modifier.fillMaxWidth(),
            rtmpStatus = status,
            onUrlChange = onUrlChange,
            onPlayStateChanged = onPlayStateChanged
        )
    }
}

@Composable
fun StreamPublisher(
    modifier: Modifier,
    status: RtmpStatus,
    setRtmpState: (RtmpState) -> Unit
) {
    val context = LocalContext.current

    val frameLayout = remember {
        FrameLayout(context)
    }

    val nodePublisher = remember {
        NodePublisher(context, "").apply {
            setVideoCodecParam(
                NodePublisher.NMC_CODEC_ID_H264,
                NodePublisher.NMC_PROFILE_AUTO,
                1280,
                720,
                30,
                2_500_000
            )
            setCameraFrontMirror(true)
        }
    }
    var hasPermission by remember {
        mutableStateOf(false)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasPermission = isGranted
            if (!isGranted) {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )


    fun launchPublisher() {
        if (hasPermission) {
            nodePublisher.openCamera(true)
            nodePublisher.start("rtmp://" + status.url)
            setRtmpState(RtmpState.STARTED)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(status.state) {
        if (status.state == RtmpState.STARTED) {
            launchPublisher()
        } else {
            nodePublisher.closeCamera()
            nodePublisher.stop()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "NodePlayerTest: dispose")
            nodePublisher.closeCamera()
            nodePublisher.stop()
            setRtmpState(RtmpState.STOPPED)
        }
    }

    Box(modifier) {
        AndroidView(
            factory = { ctx ->
                frameLayout.apply {
                    nodePublisher.attachView(this)
                }
            },
            modifier = Modifier
                .align(Alignment.Center)
        )
    }

}


@Composable
fun StreamPlaybackCompose(
    modifier: Modifier,
    status: RtmpStatus
) {
    // Stream playback player

    val context = LocalContext.current

    val frameLayout = remember {
        FrameLayout(context)
    }

    val np = remember {
        NodePlayer(context, "")
    }
    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "NodePlayerTest: dispose")
            np.stop()
        }
    }

    LaunchedEffect(status.state) {
        val url = "rtmp://" + status.url
        if (status.state == RtmpState.STARTED)// relaunch when url changes{
        {
            np.start(url)
            Log.d(TAG, "NodePlayerTest: start $url")
            Toast.makeText(context, "Playing", Toast.LENGTH_SHORT).show()
        } else {
            if (np.isPlaying) {
                np.stop()
                Log.d(TAG, "NodePlayerTest: stop $url")
                Toast.makeText(context, "Stopped", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(modifier) {
        AndroidView(
            factory = { ctx ->
                frameLayout.apply {
                    np.attachView(this)
                }
            },
            modifier = Modifier
                .background(Color.Black)

        )
    }
}


@Preview
@Composable
fun UrlPreview() {
    val status = RtmpStatus(
        RtmpState.STOPPED,
        "rtmp://192.168.200.1",
    )

    RtmpUrlCompose(
        modifier = Modifier.background(Color.White),
        rtmpStatus = status.trimRtmpUrl(),
        onUrlChange = {},
        onPlayStateChanged = {}
    )
}

fun RtmpStatus.trimRtmpUrl(): RtmpStatus {
    url = if (url.startsWith("rtmp://")) url.removePrefix("rtmp://") else url
    return this
}


fun Modifier.hideKeyboardOnOutsideClick(): Modifier = composed {
    val controller = LocalSoftwareKeyboardController.current
    this then Modifier.noRippleClickable {
        controller?.hide()
    }
}

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    this then Modifier.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick
    )
}
