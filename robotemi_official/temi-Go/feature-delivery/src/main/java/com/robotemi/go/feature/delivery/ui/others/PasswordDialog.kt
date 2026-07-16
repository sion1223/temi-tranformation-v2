package com.robotemi.go.feature.delivery.ui.others

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun PasswordDialog(
    modifier: Modifier,
    passwordInput: String,
    onChange: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Box(modifier = modifier.fillMaxSize().clip(RoundedCornerShape(19.dp))) {
            Numpad(
                modifier = modifier,
                onClickButton = {
                    when (it) {
                        "DEL" -> onChange(passwordInput.dropLast(1))
                        "GO" -> onConfirm(passwordInput)
                        else -> if (passwordInput.length < 4) onChange(passwordInput + it)
                    }
                },
                passwordInput = passwordInput,
            )
        }
    }
}

@Composable
fun Numpad(
    modifier: Modifier,
    content: List<String> = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "DEL", "0", "GO"),
    onClickButton: (String) -> Unit,
    passwordInput: String
) {
    Column(
        modifier = Modifier
            .padding(10.dp)
            .width(700.dp)
            .height(600.dp)
            .background(Color.White)
            .clip(RoundedCornerShape(19.dp)),
        verticalArrangement = Arrangement.Center
    ) {
        val maxLength = 4
        val displayChars = passwordInput.padEnd(maxLength, '_').take(maxLength).map {
            if (it == '_') "_" else "*"
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.White)
        ) {
            Row(
                modifier = modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                displayChars.forEach { char ->
                    Text(
                        text = char,
                        fontSize = 40.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items = content, key = { button -> button }) { button ->
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .width(150.dp)
                        .height(100.dp)
                        .clip(RoundedCornerShape(19.dp))
                        .background(
                            when (button) {
                                "DEL" -> Color.Red
                                "GO" -> Color(0xFF20D199)
                                else -> Color(0xFFBABABA)
                            }
                        )
                        .clickable { onClickButton(button) }
                ) {
                    Text(
                        text = button,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentHeight()
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun PasswordDialogPreview() {
    PasswordDialog(
        modifier = Modifier,
        passwordInput = "123",
        onChange = {},
        onConfirm = {},
        onDismiss = {}
    )
}