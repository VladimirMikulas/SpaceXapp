package com.vlamik.spacex.features.rocketlaunch

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vlamik.spacex.R
import com.vlamik.spacex.common.utils.preview.DeviceFormatPreview
import com.vlamik.spacex.common.utils.preview.FontScalePreview
import com.vlamik.spacex.common.utils.preview.ThemeModePreview
import com.vlamik.spacex.component.appbars.SpaceXAppBar
import com.vlamik.spacex.features.rocketlaunch.RocketLaunchViewModel.LaunchRocketState
import com.vlamik.spacex.theme.TemplateTheme

/**
 * Screen composable for the Rocket Launch feature.
 * Manages sensor listening and updates the ViewModel state based on sensor data.
 *
 * @param rocketName The name of the rocket to display.
 * @param rocketLaunchViewModel The ViewModel managing the launch state.
 * @param onBackClicked Action to perform when the back button is clicked.
 */
@Composable
fun RocketLaunchScreen(
    rocketName: String,
    rocketLaunchViewModel: RocketLaunchViewModel = viewModel(), // Obtain ViewModel using viewModel() composable
    onBackClicked: () -> Unit
) {
    // Collect the current launch state from the ViewModel
    val state by rocketLaunchViewModel.launchState.collectAsState()

    // Get the local context to access system services
    val context = LocalContext.current

    // Encapsulate sensor logic in a remembered helper
    // This helper manages the sensor listener lifecycle and exposes the pitch state
    val sensorPitchState = rememberSensorPitchState(context)

    // LaunchedEffect to react to changes in sensor pitch state
    // This effect will run whenever sensorPitchState.value changes
    LaunchedEffect(sensorPitchState.value) {
        // Check if the current ViewModel state is Ready and the pitch exceeds the threshold
        if (state is LaunchRocketState.Ready && sensorPitchState.value > 5f) {
            // Update the ViewModel state to Launching
            rocketLaunchViewModel.setLaunchState(LaunchRocketState.Launching)
        }
        // Optional: Add logic here to reset state if tilted backwards, etc.
        // else if (state is LaunchRocketState.Launching && sensorPitchState.value < -5f) {
        //     rocketLaunchViewModel.setLaunchState(LaunchRocketState.Ready)
        // }
    }

    Scaffold(
        topBar = {
            // Assuming SpaceXAppBar exists and takes a title and back button action
            SpaceXAppBar(title = rocketName, backButtonClickAction = onBackClicked)
        }
    ) { padding ->
        // Display the main content based on the ViewModel state
        LaunchRocketContent(
            state = state,
            modifier = Modifier.padding(padding)
        )
    }
}

/**
 * A remembered helper composable that manages the accelerometer sensor listener
 * and exposes the pitch (rotation around X-axis) as a State.
 *
 * @param context The Android Context to access SensorManager.
 * @return A State object holding the current pitch value.
 */
@Composable
private fun rememberSensorPitchState(context: Context): State<Float> {
    // State to hold the latest pitch value from the sensor
    var pitch: Float by remember { mutableFloatStateOf(0f) }

    // Get SensorManager and Accelerometer Sensor
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometerSensor = remember(sensorManager) {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    // SensorEventListener created and remembered within this helper
    val sensorEventListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                // Update the pitch state when sensor data changes
                // event.values[1] corresponds to the pitch (rotation around X-axis)
                pitch = event.values[1]
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Handle sensor accuracy changes if needed
            }
        }
    }

    // DisposableEffect to manage the sensor listener registration lifecycle
    DisposableEffect(sensorManager, accelerometerSensor) {
        if (accelerometerSensor != null) {
            // Register the listener when the composable enters composition
            sensorManager.registerListener(
                sensorEventListener,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_UI // Or another appropriate rate (e.g., SENSOR_DELAY_NORMAL)
            )
        }

        // Cleanup function: unregister the listener when the composable leaves composition
        onDispose {
            if (accelerometerSensor != null) {
                sensorManager.unregisterListener(sensorEventListener)
            }
        }
    }

    // Return the state holding the pitch value
    return remember(pitch) { mutableFloatStateOf(pitch) }
}


/**
 * Displays the core content of the rocket launch screen based on the launch state.
 *
 * @param state The current LaunchRocketState.
 * @param modifier Modifier for the layout.
 */
@Composable
fun LaunchRocketContent(state: LaunchRocketState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is LaunchRocketState.Ready -> ReadyRocket()
            is LaunchRocketState.Launching -> LaunchingRocket()
        }
    }
}

/**
 * Displays the rocket in its ready state.
 */
@Composable
fun ReadyRocket() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.rocket_idle),
            contentDescription = null,
            modifier = Modifier.size(180.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        ReadyMessage()
    }
}

/**
 * Displays the message for the ready state.
 */
@Composable
fun ReadyMessage() {
    Text(
        text = stringResource(id = R.string.rocket_idle_message),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(200.dp),
        maxLines = 2
    )
}

/**
 * Displays the rocket in its launching state with animation.
 */
@Composable
fun LaunchingRocket() {
    // Animatable to control the vertical offset of the rocket
    val rocketOffset = remember { Animatable(0f) }

    // LaunchedEffect to start the animation when this Composable becomes active
    LaunchedEffect(Unit) {
        rocketOffset.animateTo(
            targetValue = -1000f, // Value the rocket will move upwards to (adjust as needed)
            animationSpec = tween(
                durationMillis = 2000,
                easing = LinearEasing
            ) // Animation duration and easing
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.offset {
                // Calculate the offset in pixels based on the animated float value converted to Dp
                IntOffset(x = 0, y = rocketOffset.value.dp.roundToPx())
            },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.rocket_flying),
                contentDescription = null,
                modifier = Modifier.size(180.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        LaunchMessage()
    }
}

/**
 * Displays the message for the launching state.
 */
@Composable
fun LaunchMessage() {
    Text(
        text = stringResource(id = R.string.rocket_launch_message),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
}

// --- Preview Functions ---

@ThemeModePreview
@FontScalePreview
@DeviceFormatPreview
@Composable
private fun RocketLaunchPreview_Ready() {
    TemplateTheme {
        LaunchRocketContent(
            state = LaunchRocketState.Ready,
        )
    }
}

@ThemeModePreview
@FontScalePreview
@DeviceFormatPreview
@Composable
private fun RocketLaunchPreview_Launching() { // Added preview for Launching state
    TemplateTheme {
        LaunchRocketContent(
            state = LaunchRocketState.Launching,
        )
    }
}
