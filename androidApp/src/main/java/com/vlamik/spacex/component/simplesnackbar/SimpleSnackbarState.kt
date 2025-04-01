package com.vlamik.spacex.component.simplesnackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class SimpleSnackbarState {
    private var _message by mutableStateOf<SnackbarMessage?>(null)
    val message: SnackbarMessage? get() = _message

    fun show(
        text: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
        onAction: (() -> Unit)? = null
    ) {
        _message = SnackbarMessage(text, actionLabel, duration, onAction)
    }

    fun hide() {
        _message = null
    }
}

data class SnackbarMessage(
    val text: String,
    val actionLabel: String?,
    val duration: SnackbarDuration,
    val onAction: (() -> Unit)?
)