package com.vlamik.spacex.component.simplesnackbar

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun SimpleSnackbar(
    state: SimpleSnackbarState,
    modifier: Modifier = Modifier
) {
    state.message?.let { message ->
        Snackbar(
            modifier = modifier.padding(16.dp),
            action = {
                message.actionLabel?.let { label ->
                    TextButton(
                        onClick = {
                            message.onAction?.invoke()
                            state.hide()
                        }
                    ) {
                        Text(label)
                    }
                }
            }
        ) {
            Text(message.text)
        }

        LaunchedEffect(message) {
            when (message.duration) {
                SnackbarDuration.Short -> delay(4000)
                SnackbarDuration.Long -> delay(7000)
                SnackbarDuration.Indefinite -> return@LaunchedEffect
            }
            state.hide()
        }
    }
}