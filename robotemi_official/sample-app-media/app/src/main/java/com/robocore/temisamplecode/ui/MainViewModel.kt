package com.robocore.temisamplecode.ui

import androidx.lifecycle.ViewModel
import com.robocore.temisamplecode.ui.composes.RtmpState
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewModel: ViewModel() {

    val rtmpState: MutableStateFlow<RtmpState> = MutableStateFlow(RtmpState.STOPPED)
    val rtmpUrl: MutableStateFlow<String> = MutableStateFlow("rtmp://")
}