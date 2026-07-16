package com.robotemi.go.feature.delivery.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.robotemi.go.feature.mymodel.R
import com.robotemi.go.feature.navigation.IdleScreen


@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    navController: NavController,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        modifier = modifier,
        password = uiState.password,
        idleLocation = uiState.idleLocation,
        arriveTimeout = uiState.arriveTimeout,
        pauseTimeout = uiState.pauseTimeout,
        startSpeech = uiState.startSpeech,
        obstacleSpeech = uiState.obstacleSpeech,
        arriveSpeech = uiState.arriveSpeech,
        nextLocationSpeech = uiState.nextLocationSpeech,
        wrongTraySpeech = uiState.wrongTraySpeech,
        isShowingDialog = uiState.isShowingDialog,
        currentDialog = uiState.currentDialog,
        input = uiState.input,
        setPassword = { viewModel.setPassword(it) },
        setIdleLocation = { viewModel.setIdleLocation(it) },
        setArriveTimeout = { viewModel.setArriveTimeout(it) },
        setPauseTimeout = { viewModel.setPauseTimeout(it) },
        setStartSpeech = { viewModel.setStartSpeech(it) },
        setObstacleSpeech = { viewModel.setObstacleSpeech(it) },
        setArriveSpeech = { viewModel.setArriveSpeech(it) },
        setNextLocationSpeech = { viewModel.setNextLocationSpeech(it) },
        setWrongTraySpeech = { viewModel.setWrongTraySpeech(it) },
        showDialog = { key, show -> viewModel.setShowDialog(key, show) },
        getLocations = { viewModel.getLocations() },
        setInput = { viewModel.setInput(it) },
        tareTrays = { viewModel.tareTrays() },
        goToHomeBase = { viewModel.goToHomeBase() },
        back = { navController.popBackStack(IdleScreen, false) }
    )
}


@Composable
internal fun SettingsScreen(
    modifier: Modifier,
    password: String,
    idleLocation: String,
    arriveTimeout: Int,
    pauseTimeout: Int,
    startSpeech: String,
    obstacleSpeech: String,
    arriveSpeech: String,
    nextLocationSpeech: String,
    wrongTraySpeech: String,
    isShowingDialog: Boolean,
    currentDialog: String,
    input: String,
    setPassword: (String) -> Unit,
    setIdleLocation: (String) -> Unit,
    setArriveTimeout: (Int) -> Unit,
    setPauseTimeout: (Int) -> Unit,
    setStartSpeech: (String) -> Unit,
    setObstacleSpeech: (String) -> Unit,
    setArriveSpeech: (String) -> Unit,
    setNextLocationSpeech: (String) -> Unit,
    setWrongTraySpeech: (String) -> Unit,
    showDialog: (String, Boolean) -> Unit,
    getLocations: () -> List<String>,
    setInput: (String) -> Unit,
    tareTrays: () -> Unit,
    goToHomeBase: () -> Unit,
    back: () -> Unit
) {
    if (isShowingDialog) {
        when (currentDialog) {
            "password" -> {
                NumberInputDialog(
                    modifier = modifier,
                    title = stringResource(R.string.password),
                    value = input,
                    onDismiss = {
                        showDialog("password", false)
                        setInput("")
                    },
                    onConfirm = {
                        setPassword(it)
                        setInput("")
                        showDialog("password", false)
                    },
                    onChange = { setInput(it) }
                )
            }

            "idleLocation" -> {
                ListDialog(
                    modifier = modifier,
                    title = stringResource(R.string.idle_location),
                    value = getLocations(),
                    onDismiss = { showDialog("idleLocation", false) },
                    onConfirm = {
                        setIdleLocation(it)
                        showDialog("idleLocation", false)
                    }
                )
            }

            "arriveTimeout" -> {
                NumberInputDialog(
                    modifier = modifier,
                    title = stringResource(R.string.arrive_timeout),
                    value = input,
                    onDismiss = {
                        showDialog("arriveTimeout", false)
                        setInput("")
                    },
                    onConfirm = {
                        setArriveTimeout(it.toInt())
                        setInput("")
                        showDialog("arriveTimeout", false)
                    },
                    onChange = { setInput(it) }
                )
            }

            "pauseTimeout" -> {
                NumberInputDialog(
                    modifier = modifier,
                    title = stringResource(R.string.pause_timeout),
                    value = input,
                    onDismiss = {
                        showDialog("pauseTimeout", false)
                        setInput("")
                    },
                    onConfirm = {
                        setPauseTimeout(it.toInt())
                        setInput("")
                        showDialog("pauseTimeout", false)
                    },
                    onChange = { setInput(it) }
                )
            }

            "startSpeech" -> {
                StringInputDialog(
                    modifier = modifier,
                    title = stringResource(R.string.start_speech),
                    value = input,
                    onDismiss = {
                        showDialog("startSpeech", false)
                        setInput("")
                    },
                    onConfirm = {
                        setStartSpeech(it)
                        setInput("")
                        showDialog("startSpeech", false)
                    },
                    onChange = { setInput(it) }
                )
            }

            "obstacleSpeech" -> {
                StringInputDialog(
                    modifier = modifier,
                    title = stringResource(R.string.obstacle_speech),
                    value = input,
                    onDismiss = {
                        showDialog("obstacleSpeech", false)
                        setInput("")
                    },
                    onConfirm = {
                        setObstacleSpeech(it)
                        setInput("")
                        showDialog("obstacleSpeech", false)
                    },
                    onChange = { setInput(it) }
                )
            }

            "arriveSpeech" -> {
                StringInputDialog(
                    modifier = modifier,
                    title = stringResource(R.string.arrive_speech),
                    value = input,
                    onDismiss = {
                        showDialog("arriveSpeech", false)
                        setInput("")
                    },
                    onConfirm = {
                        setArriveSpeech(it)
                        setInput("")
                        showDialog("arriveSpeech", false)
                    },
                    onChange = { setInput(it) }
                )
            }

            "nextLocationSpeech" -> {
                StringInputDialog(
                    modifier = modifier,
                    title = stringResource(R.string.next_location_speech),
                    value = input,
                    onDismiss = {
                        showDialog("nextLocationSpeech", false)
                        setInput("")
                    },
                    onConfirm = {
                        setNextLocationSpeech(it)
                        setInput("")
                        showDialog("nextLocationSpeech", false)
                    },
                    onChange = { setInput(it) }
                )
            }

            "wrongTraySpeech" -> {
                StringInputDialog(
                    modifier = modifier,
                    title = stringResource(R.string.wrong_tray_speech),
                    value = input,
                    onDismiss = {
                        showDialog("wrongTraySpeech", false)
                        setInput("")
                    },
                    onConfirm = {
                        setWrongTraySpeech(it)
                        setInput("")
                        showDialog("wrongTraySpeech", false)
                    },
                    onChange = { setInput(it) }
                )
            }
        }
    }
    Row(modifier = modifier.padding(20.dp)) {
        Column(modifier = modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            SettingsItem(
                modifier = modifier,
                title = stringResource(R.string.password),
                value = password,
                key = "password",
                showDialog = showDialog
            )
            SettingsItem(
                modifier = modifier,
                title = stringResource(R.string.idle_location),
                value = idleLocation,
                key = "idleLocation",
                showDialog = showDialog
            )
            SettingsItem(
                modifier = modifier,
                title = stringResource(R.string.arrive_timeout),
                value = arriveTimeout.toString(),
                key = "arriveTimeout",
                showDialog = showDialog
            )
            SettingsItem(
                modifier = modifier,
                title = stringResource(R.string.pause_timeout),
                value = pauseTimeout.toString(),
                key = "pauseTimeout",
                showDialog = showDialog
            )
            SettingsItem(
                modifier = modifier,
                title = stringResource(R.string.start_speech),
                value = startSpeech,
                key = "startSpeech",
                showDialog = showDialog
            )
            SettingsItem(
                modifier = modifier,
                title = stringResource(R.string.obstacle_speech),
                value = obstacleSpeech,
                key = "obstacleSpeech",
                showDialog = showDialog
            )
            SettingsItem(
                modifier = modifier,
                title = stringResource(R.string.arrive_speech),
                value = arriveSpeech,
                key = "arriveSpeech",
                showDialog = showDialog
            )
            SettingsItem(
                modifier = modifier,
                title = stringResource(R.string.next_location_speech),
                value = nextLocationSpeech,
                key = "nextLocationSpeech",
                showDialog = showDialog
            )
            SettingsItem(
                modifier = modifier,
                title = stringResource(R.string.wrong_tray_speech),
                value = wrongTraySpeech,
                key = "wrongTraySpeech",
                showDialog = showDialog
            )
            Button(onClick = { tareTrays() }) {
                Text(stringResource(R.string.tare_trays), fontSize = 40.sp)
            }
            Button(onClick = { goToHomeBase() }) {
                Text(stringResource(R.string.go_to_home_base), fontSize = 40.sp)
            }
        }
        Button(onClick = { back() }, modifier = modifier
            .padding(5.dp)
            .align(Alignment.Top)) {
            Text(stringResource(R.string.back), fontSize = 35.sp)
        }
    }
}

@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    key: String,
    showDialog: (String, Boolean) -> Unit
) {
    Row(modifier = modifier.clickable { showDialog(key, true) }) {
        Text(title, fontSize = 40.sp)
        Text(value, fontSize = 40.sp)
    }
}

@Composable
fun StringInputDialog(
    modifier: Modifier,
    title: String,
    value: String,
    onConfirm: (String) -> Unit,
    onChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier
                .background(color = Color.White)
                .height(200.dp)
                .width(400.dp)
        ) {
            Text(title)
            OutlinedTextField(
                modifier = modifier.padding(10.dp),
                value = value,
                onValueChange = { onChange(it) },
                label = { Text(title) },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onConfirm(value)
                    }
                )
            )
            Row(modifier.align(Alignment.End)) {
                Button(modifier = modifier.padding(horizontal = 10.dp), onClick = {
                    onConfirm(value)
                }) {
                    Text(stringResource(R.string.submit))
                }
                Button(
                    modifier = modifier.padding(horizontal = 10.dp),
                    onClick = { onDismiss() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}

@Composable
fun ListDialog(
    modifier: Modifier,
    title: String,
    value: List<String>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier
                .background(color = Color.White)
                .height(300.dp)
                .width(200.dp)
        ) {
            Text(title)
            LazyColumn(modifier = modifier.padding(10.dp)) {
                items(count = value.size, key = { location -> location }) { location ->
                    Button(
                        onClick = {
                            onConfirm(value[location])
                            onDismiss()
                        },
                        modifier = modifier.padding(5.dp)
                    ) {
                        Text(value[location], fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun NumberInputDialog(
    modifier: Modifier,
    title: String,
    value: String,
    onConfirm: (String) -> Unit,
    onChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val max = 4
    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier
                .background(color = Color.White)
                .height(200.dp)
                .width(400.dp)
        ) {
            Text(title)
            OutlinedTextField(
                modifier = modifier.padding(10.dp),
                value = value,
                onValueChange = {
                    if(it.length <= max) onChange(it)
                },
                label = { Text(title) },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Number
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onConfirm(value)
                    }
                )
            )
            Row(modifier.align(Alignment.End)) {
                Button(modifier = modifier.padding(horizontal = 10.dp), onClick = {
                    onConfirm(value)
                }) {
                    Text(stringResource(R.string.submit))
                }
                Button(
                    modifier = modifier.padding(horizontal = 10.dp),
                    onClick = { onDismiss() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}

