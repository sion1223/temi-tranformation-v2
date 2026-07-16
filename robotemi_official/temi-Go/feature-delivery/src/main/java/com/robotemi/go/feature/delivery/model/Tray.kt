package com.robotemi.go.feature.delivery.model

enum class Tray(val trayNumber: Int) {
    TOP(0),
    MIDDLE(1),
    BOTTOM(2);

    companion object {
        fun fromInt(value: Int) = entries.first { it.trayNumber == value }
    }
}