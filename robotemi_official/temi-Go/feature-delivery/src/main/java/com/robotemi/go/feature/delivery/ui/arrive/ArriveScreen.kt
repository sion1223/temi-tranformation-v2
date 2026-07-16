package com.robotemi.go.feature.delivery.ui.arrive

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.robotemi.go.feature.delivery.model.Tray
import com.robotemi.go.feature.delivery.ui.going.OptionDialog
import com.robotemi.go.feature.delivery.ui.others.PasswordDialog
import com.robotemi.go.feature.delivery.ui.others.TemiGo
import com.robotemi.go.feature.mymodel.R
import com.robotemi.go.feature.navigation.GoingScreen
import com.robotemi.go.feature.navigation.IdleScreen

@Composable
fun ArriveScreen(
    modifier: Modifier = Modifier,
    viewModel: ArriveViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val back by viewModel.back.collectAsStateWithLifecycle()
    if (back) {
        navController.popBackStack(IdleScreen, false)
    }

    val goToGoing by viewModel.goToGoing.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = goToGoing) {
        if (goToGoing) {
            navController.navigate(GoingScreen(viewModel.goData)) {
//                popUpTo(IdleScreen) { inclusive = false }
            }
            viewModel.reset()
        }
    }

    ArriveScreen(
        modifier = modifier,
        location = uiState.location,
        tray = uiState.tray,
        countdown = uiState.remainingTime,
        showWrongTrayPopUp = uiState.showWrongTrayPopUp,
        passwordPopup = uiState.passwordPopUp,
        passwordInput = uiState.passwordInput,
        optionsPopUp = uiState.optionsPopUp,
        go = {
            viewModel.stopTimer()
            viewModel.nextAction()
        },
        pause = {
            viewModel.pause()
        },
        setPasswordInput = { viewModel.setPasswordInput(it) },
        checkPassword = { viewModel.checkPassword(it) },
        selectOption = { viewModel.selectOption(it) },
        resume = { viewModel.resume() }
    )
}

@Composable
internal fun ArriveScreen(
    modifier: Modifier = Modifier,
    location: String,
    tray: Int,
    countdown: Long,
    showWrongTrayPopUp: Boolean,
    passwordPopup: Boolean,
    passwordInput: String,
    optionsPopUp: Boolean,
    go: () -> Unit,
    pause: () -> Unit,
    setPasswordInput: (String) -> Unit,
    checkPassword: (String) -> Unit,
    selectOption: (Int) -> Unit,
    resume: () -> Unit
) {
    if (passwordPopup) {
        PasswordDialog(
            modifier = modifier
                .width(500.dp)
                .height(600.dp),
            passwordInput = passwordInput,
            onChange = setPasswordInput,
            onConfirm = checkPassword,
            onDismiss = resume
        )
    }

    if (optionsPopUp) {
        OptionDialog(
            modifier = modifier,
            onSelect = selectOption,
            onDismiss = resume
        )
    }


    if (showWrongTrayPopUp) {
        Dialog(onDismissRequest = { }) {
            Column(
                modifier = modifier
                    .size(400.dp)
                    .background(Color.White),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.wrong_tray),
                    modifier = Modifier.padding(16.dp),
                    fontSize = 50.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.please_put_the_tray_back_and_take_the_correct_tray),
                    modifier = Modifier.padding(16.dp),
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    Row {
        TemiGo(
            onSelect = {},
            onCancel = {},
            map = mapOf(),
            currentSelectedTray = Tray.fromInt(tray)
        )
        InfoDisplay(
            modifier = modifier,
            location = location,
            countdown = countdown,
            go = go,
            pause = pause
        )
    }
}

@Composable
fun InfoDisplay(
    modifier: Modifier,
    location: String,
    countdown: Long,
    go: () -> Unit,
    pause: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 60.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = modifier.padding(bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.order_arrived),
                fontSize = 50.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.take_order),
                fontSize = 30.sp,
                textAlign = TextAlign.Center
            )
        }
        Box(
            modifier = modifier
                .clip(shape = RoundedCornerShape(20.dp))
                .background(Color.White)
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.6f)
        ) {
            Text(
                text = location,
                modifier = Modifier
                    .padding(horizontal = 30.dp, vertical = 10.dp)
                    .align(Alignment.Center),
                color = Color(0xFF20D199),
                fontSize = when (location.length) {
                    in 1..5 -> 300.sp
                    in 6..10 -> 150.sp
                    else -> 100.sp
                },
                fontWeight = FontWeight.Bold,
                lineHeight = 200.sp,
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = stringResource(R.string.remaining_time, countdown / 1000),
            fontSize = 20.sp,
            modifier = modifier.padding(top = 30.dp)
        )
        PauseButton(stop = { pause() })
        GoButton(modifier = modifier.align(Alignment.End), go = { go() })
    }
}

@Composable
private fun PauseButton(modifier: Modifier = Modifier, stop: () -> Unit) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable { stop() }) {
        Text(
            text = stringResource(R.string.pause),
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4A4A4A),
            fontSize = 70.sp,
            modifier = modifier.padding(10.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.pause),
            contentDescription = "pause",
            modifier
                .width(70.dp)
                .height(70.dp)
                .align(Alignment.CenterVertically)
        )
    }
}

@Composable
fun GoButton(modifier: Modifier, go: () -> Unit) {
    val enabledColor = Color(0xFF20D199)
    val infiniteTransition = rememberInfiniteTransition(label = "Go Button")
    val animationValue by infiniteTransition.animateValue(
        initialValue = -5f,
        targetValue = 5f,
        typeConverter = Float.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "Go Button"
    )

    val offSet = animationValue

    Row(
        modifier = modifier
            .clickable(indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                go()
            }
            .background(Color(0xFFEDEDED))
            .padding(end = 50.dp)
    ) {
        Text(
            text = stringResource(R.string.go),
            textAlign = TextAlign.Center,
            fontSize = 100.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp),
            color = enabledColor
        )
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(100.dp)
                .align(Alignment.CenterVertically)
                .clip(CircleShape)
                .background(enabledColor)
        ) {
            Image(
                painter = painterResource(id = R.drawable.go_button_arrow),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = offSet.dp)
                    .size(50.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 1706, heightDp = 904)
@Composable
fun ArriveScreenPreview() {
    ArriveScreen(
        location = "1",
        tray = 1,
        countdown = 10000,
        go = {},
        showWrongTrayPopUp = true,
        pause = {},
        passwordPopup = false,
        passwordInput = "",
        setPasswordInput = {},
        checkPassword = {},
        resume = {},
        optionsPopUp = false,
        selectOption = {}
    )
}