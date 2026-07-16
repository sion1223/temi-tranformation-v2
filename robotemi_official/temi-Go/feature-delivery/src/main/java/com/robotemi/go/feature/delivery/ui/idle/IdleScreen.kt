/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.robotemi.go.feature.delivery.ui.idle


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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.robotemi.go.core.ui.MyApplicationTheme
import com.robotemi.go.feature.delivery.model.Tray
import com.robotemi.go.feature.delivery.ui.others.PasswordDialog
import com.robotemi.go.feature.delivery.ui.others.TemiGo
import com.robotemi.go.feature.mymodel.R
import com.robotemi.go.feature.navigation.GoingScreen
import com.robotemi.go.feature.navigation.SettingsScreen


@Composable
fun IdleScreen(
    modifier: Modifier = Modifier,
    viewModel: IdleViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val goToSettings by viewModel.goToSettings.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = goToSettings) {
        if (goToSettings) {
            navController.navigate(SettingsScreen)
            viewModel.reset()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.initLocation()
        viewModel.initLED()
        viewModel.setLcdText()
        viewModel.reset()
    }

    IdleScreen(
        modifier = modifier,
        locations = uiState.locations,
        map = uiState.tray,
        passwordPopUp = uiState.passwordPopUp,
        passwordInput = uiState.passwordInput,
        currentSelectedTray = uiState.currentSelectedTray,
        setTrayLocation = { location: String ->
            viewModel.setTrayLocation(
                location
            )
        },
        removeTrayLocation = { viewModel.removeTrayLocation(it) },
        setCurrentSelectedTray = { viewModel.setCurrentSelectedTray(it) },
        setPasswordPopUp = { viewModel.setPasswordPopUp(it) },
        setPasswordInput = { viewModel.setPasswordInput(it) },
        checkPassword = { viewModel.checkPassword(it) },
        go = {
//            viewModel.setGoToLocation()
            viewModel.startSpeech()
            navController.navigate(GoingScreen(viewModel.goData))
        },
    )
}


@Composable
internal fun IdleScreen(
    modifier: Modifier,
    locations: List<String>,
    passwordPopUp: Boolean,
    passwordInput: String,
    setTrayLocation: (location: String) -> Unit,
    setCurrentSelectedTray: (tray: Tray?) -> Unit,
    removeTrayLocation: (tray: Tray) -> Unit,
    setPasswordPopUp: (Boolean) -> Unit,
    setPasswordInput: (String) -> Unit,
    checkPassword: (String) -> Unit,
    map: Map<Tray, String?>,
    currentSelectedTray: Tray?,
    go: () -> Unit
) {
    if (passwordPopUp) {
        PasswordDialog(
            modifier = modifier
                .width(500.dp)
                .height(600.dp),
            passwordInput = passwordInput,
            onChange = setPasswordInput,
            onConfirm = checkPassword,
            onDismiss = { setPasswordPopUp(false) }
        )
    }
    Row {
        TemiGo(
            onSelect = setCurrentSelectedTray,
            onCancel = removeTrayLocation,
            map = map,
            currentSelectedTray = currentSelectedTray,
        )


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 140.dp, top = 10.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.settings),
                contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .padding(end = 50.dp)
                    .align(Alignment.End)
                    .clickable(indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        setPasswordPopUp(true)
                    }
            )
            Text(
                text = stringResource(R.string.select_a_tray_then_assign_a_location),
                modifier = Modifier.padding(bottom = 10.dp),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            LocationGrid(
                locations = locations,
                onClick = setTrayLocation,
                map = map
            )

            GoButton(modifier = modifier.align(Alignment.End), enable = map.isNotEmpty(), go = go)
        }
    }
}

@Composable
fun LocationGrid(
    locations: List<String>,
    onClick: (location: String) -> Unit,
    map: Map<Tray, String?>,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF8F8F8))
            .fillMaxWidth(0.9f)
            .fillMaxHeight(0.75f),
    ) {
        items(items = locations, key = { location -> location }) { location ->
            val isSelected = map.containsValue(location)
            Box(
                modifier = Modifier
                    .padding(horizontal = 32.dp, vertical = 16.dp)
                    .width(194.dp)
                    .height(113.dp)
                    .clip(RoundedCornerShape(19.dp))
                    .background(
                        if (isSelected) {
                            Color(0xFF20D199)
                        } else
                            Color(0xFFBABABA)
                    )
                    .clickable {
                        onClick(location)
                    }
                    .padding(10.dp),
            ) {
                Text(
                    text = location,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = when (location.length) {
                        in 1..8 -> 40.sp
                        in 9..20 -> 30.sp
                        else -> 20.sp
                    },
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 40.sp,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight()
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun GoButton(modifier: Modifier, enable: Boolean, go: () -> Unit) {
    val enabledColor = if (enable) Color(0xFF20D199) else Color(0x7520D199)
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

    val offSet = if (enable) animationValue else 0f

    Row(
        modifier = modifier
            .clickable(enabled = enable, indication = null,
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


// Preview
@Preview(showBackground = true, widthDp = 1706, heightDp = 904)
@Composable
private fun PortraitPreview() {
    MyApplicationTheme {
        IdleScreen(
            modifier = Modifier.fillMaxSize(),
            locations = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J"),
            setTrayLocation = {},
            setCurrentSelectedTray = {},
            removeTrayLocation = {},
            map = mapOf(),
            currentSelectedTray = null,
            go = {},
            setPasswordPopUp = {},
            setPasswordInput = {},
            checkPassword = {},
            passwordPopUp = false,
            passwordInput = ""
        )
    }
}

@Preview(showBackground = true, widthDp = 1706, heightDp = 904)
@Composable
private fun LocationGridPreview() {
    LocationGrid(
        locations = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J"),
        onClick = {},
        map = mapOf()
    )
}

@Preview
@Composable
private fun GoButtonPreview() {
    MyApplicationTheme {
        GoButton(modifier = Modifier, enable = true, go = {})
    }
}