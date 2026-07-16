package com.robocore.temisamplecode.ui.composes

import android.annotation.SuppressLint
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun ExitDialog(shouldShowDialog: MutableState<Boolean>, onExit: () -> Unit) {
    if (shouldShowDialog.value) {
        AlertDialog(
            onDismissRequest = {
                shouldShowDialog.value = false
            },
            title = { Text(text = "Exit App") },
            confirmButton = {
                Button(
                    onClick = {
                        shouldShowDialog.value = false
                        onExit()
                    }
                ) {
                    Text(
                        text = "Confirm",
                        color = Color.White
                    )
                }
            }
        )
    }
}

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
fun ExitDialogPreview() {
    val mutableState = mutableStateOf(true)
    ExitDialog(shouldShowDialog = mutableState, onExit = {})
}