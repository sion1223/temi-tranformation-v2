package com.robocore.temisamplecode

//import com.robotemi.sdk.constants.HomeScreenMode
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.robocore.temisamplecode.ui.MainViewModel
import com.robocore.temisamplecode.ui.composes.CameraCompose
import com.robocore.temisamplecode.ui.composes.ExitDialog
import com.robocore.temisamplecode.ui.composes.MicCompose
import com.robocore.temisamplecode.ui.composes.RtmpStatus
import com.robocore.temisamplecode.ui.composes.StreamingCompose
import com.robocore.temisamplecode.ui.theme.TemiSampleCodeTheme
import com.robotemi.sdk.Robot
import com.robotemi.sdk.constants.HomeScreenMode
import com.robotemi.sdk.listeners.OnRobotReadyListener
import com.robotemi.sdk.permission.OnRequestPermissionResultListener
import com.robotemi.sdk.permission.Permission

enum class Screen(val route: String) {
    MAIN("main"),
    CAMERA("camera"),
    MIC("mic"),
    STREAM("stream")
}

class MainActivity : ComponentActivity(), OnRobotReadyListener, OnRequestPermissionResultListener {

    private val robot by lazy {
        Robot.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onStart() {
        super.onStart()
        robot.addOnRobotReadyListener(this)
        robot.addOnRequestPermissionResultListener(this)
    }

    private fun onExit(){
        robot.setKioskModeOn(false, HomeScreenMode.DEFAULT)
        finish()
    }

    override fun onRobotReady(isReady: Boolean) {
        if (isReady) {
            robot.setKioskModeOn(true, mode = HomeScreenMode.DEFAULT)
            robot.requestToBeKioskApp() // cannot remove this line, otherwise the app will not be able to set kiosk mode, and the mic won't works
            if (robot?.checkSelfPermission(Permission.SETTINGS) != Permission.GRANTED) {
                robot?.requestPermissions(listOf(Permission.SETTINGS), 0)
            }

            setContent {

                AppContent(
                    onExit = ::onExit
                )
            }
        }
    }

    override fun onRequestPermissionResult(
        permission: Permission,
        grantResult: Int,
        requestCode: Int
    ) {
        Log.d("MainActivity", "onRequestPermissionResult: $permission, $grantResult, $requestCode")

        if(grantResult != Permission.GRANTED){
            robot.requestPermissions(listOf(Permission.SETTINGS), 0)
        }
    }
}


@Composable
fun AppContent(
    robot: Robot? = rememberRobot(),
    onExit: () -> Unit = {}
) {
    val mainViewModel = MainViewModel()

    val showExitDialog = remember {
        mutableStateOf(false)
    }


    TemiSampleCodeTheme {
        // A surface container using the 'background' color from the theme
        val context = LocalContext.current
        val navController = rememberNavController()
        val snackbarHostState = remember { SnackbarHostState() }


        Scaffold(
            floatingActionButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(30.dp, 0.dp, 0.dp, 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Row {
                        Button(onClick = {
                            showExitDialog.value = true
                        }) {
                            Text(
                                text = "Exit"
                            )
                        }

                        Button(
                            enabled = robot?.isSelectedKioskApp() == false || robot?.isKioskModeOn() == false,
                            onClick = {
                                robot?.setKioskModeOn(true, HomeScreenMode.DEFAULT)
                                robot?.requestToBeKioskApp()
                            }) {
                            Text(
                                text = "Request Kiosk"
                            )
                        }
                    }


                    Button(onClick = {
                        if (navController.currentDestination?.route != Screen.MAIN.route)
                            navController.navigate(Screen.MAIN.route)
                        else
                            Toast.makeText(
                                context,
                                "Already on Home",
                                Toast.LENGTH_SHORT
                            ).show()
                    }) {
                        Text("Home")
                    }

                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
        ) {

            Surface {


                ExitDialog(shouldShowDialog = showExitDialog,
                    onExit = {
                        onExit()
                    }
                )




                NavHost(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it),
                    navController = navController,
                    startDestination = Screen.MAIN.route
                ) {

                    composable(Screen.MAIN.route) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                modifier = Modifier.align(Alignment.TopStart),
                                text = "application: ${BuildConfig.APPLICATION_ID} \n" +
                                        "Kiosk mode: ${robot?.isKioskModeOn()}\n" +
                                        "Has requested to be Kiosk app: ${robot?.isSelectedKioskApp()}\n" +
                                        "Temi Launcher: ${robot?.launcherVersion ?: "0-Local"} \n" +
                                        "Robox Version: ${robot?.roboxVersion ?: "1.134.1"} \n" +
                                        "version: ${BuildConfig.VERSION_NAME} - ${BuildConfig.BUILD_TYPE}"
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center

                            ) {

                                Button(onClick = { navController.navigate(Screen.CAMERA.route) }) {
                                    Text("Camera")
                                }

                                Button(onClick = { navController.navigate(Screen.MIC.route) }) {
                                    Text("Mic")
                                }

                                Button(onClick = { navController.navigate(Screen.STREAM.route) }) {
                                    Text("Stream")
                                }
                            }
                        }
                    }

                    composable(Screen.CAMERA.route) {
                        CameraCompose()
                    }
                    composable(Screen.MIC.route) {
                        MicCompose()
                    }

                    composable(Screen.STREAM.route) {

                        val rtmpState by mainViewModel.rtmpState.collectAsState()
                        val rtmpUrl by mainViewModel.rtmpUrl.collectAsState()


                        StreamingCompose(
                            RtmpStatus(
                                state = rtmpState,
                                url = if (rtmpUrl.startsWith("rtmp://")) rtmpUrl.removePrefix(
                                    "rtmp://"
                                ) else rtmpUrl
                            ),
                            onUrlChange = { mainViewModel.rtmpUrl.value = it },
                            onPlayStateChanged = { mainViewModel.rtmpState.value = it }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
fun DefaultPreview() {
    AppContent(
        null
    )
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview2() {
    AppContent(
        null
    )
}


@Composable
fun rememberRobot(): Robot {
    return remember {
        Robot.getInstance()
    }
}