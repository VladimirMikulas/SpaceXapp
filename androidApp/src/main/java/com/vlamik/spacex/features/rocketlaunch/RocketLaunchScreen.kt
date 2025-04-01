package com.vlamik.spacex.features.rocketlaunch

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vlamik.spacex.R
import com.vlamik.spacex.component.appbars.SpaceXAppBar
import com.vlamik.spacex.core.utils.preview.DeviceFormatPreview
import com.vlamik.spacex.core.utils.preview.FontScalePreview
import com.vlamik.spacex.core.utils.preview.ThemeModePreview
import com.vlamik.spacex.features.rocketlaunch.RocketLaunchViewModel.LaunchRocketState
import com.vlamik.spacex.theme.TemplateTheme

@Composable
fun RocketLaunchScreen(
    rocketName: String,
    rocketLaunchViewModel: RocketLaunchViewModel,
    onBackClicked: () -> Unit
) {
    val state by rocketLaunchViewModel.launchState.collectAsState()

    Scaffold(
        topBar = { SpaceXAppBar(title = rocketName, backButtonClickAction = onBackClicked) }
    ) { padding ->
        LaunchRocketContent(
            state = state,
            modifier = Modifier.padding(padding)
        )
    }
}

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

@Composable
fun LaunchingRocket() {
    val rocketOffset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        rocketOffset.animateTo(
            targetValue = -1000f,
            animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.offset(y = rocketOffset.value.dp),
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

@Composable
fun LaunchMessage() {
    Text(
        text = stringResource(id = R.string.rocket_launch_message),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
}

@ThemeModePreview
@FontScalePreview
@DeviceFormatPreview
@Composable
private fun RocketLaunchPreview() {
    TemplateTheme {
        LaunchRocketContent(
            state = LaunchRocketState.Ready,
        )
    }
}

