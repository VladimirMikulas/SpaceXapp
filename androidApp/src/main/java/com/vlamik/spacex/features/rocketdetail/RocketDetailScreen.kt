package com.vlamik.spacex.features.rocketdetail

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vlamik.core.domain.models.RocketDetailModel
import com.vlamik.core.domain.models.StageDetailModel
import com.vlamik.spacex.R
import com.vlamik.spacex.common.utils.preview.DeviceFormatPreview
import com.vlamik.spacex.common.utils.preview.FontScalePreview
import com.vlamik.spacex.common.utils.preview.ThemeModePreview
import com.vlamik.spacex.component.ErrorMessage
import com.vlamik.spacex.component.LoadingIndicator
import com.vlamik.spacex.component.appbars.SpaceXAppBar
import com.vlamik.spacex.theme.PinkShade
import com.vlamik.spacex.theme.Shapes
import com.vlamik.spacex.theme.SoftGray
import com.vlamik.spacex.theme.TemplateTheme

@Composable
fun RocketDetailScreen(
    detailsViewModel: RocketDetailViewModel,
    onLaunchClicked: (String) -> Unit,
    onBackClicked: () -> Unit
) {
    val rocketDetailState by detailsViewModel.updateState.collectAsState()

    when (val state = rocketDetailState) {
        RocketDetailViewModel.UiState.DataError -> ErrorMessage(errorMessage = stringResource(id = R.string.data_error))
        RocketDetailViewModel.UiState.LoadingData -> LoadingIndicator()
        is RocketDetailViewModel.UiState.Success -> {
            RocketDetailComposable(
                state = state,
                onBackClicked = onBackClicked,
                onLaunchClicked = onLaunchClicked
            )
        }
    }
}

@Composable
private fun RocketDetailComposable(
    state: RocketDetailViewModel.UiState,
    onBackClicked: () -> Unit,
    onLaunchClicked: (String) -> Unit

) {
    val rocketDetail: RocketDetailModel
    (state as? RocketDetailViewModel.UiState.Success)?.let {
        rocketDetail = it.rocket
        Scaffold(
            topBar = {
                SpaceXAppBar(
                    title = rocketDetail.name,
                    backButtonText = stringResource(id = R.string.rockets),
                    backButtonClickAction = onBackClicked,
                    addLaunchButton = true,
                    launchButtonClickAction = onLaunchClicked
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(id = R.string.overview),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                Text(
                    text = rocketDetail.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = stringResource(id = R.string.parameters),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ParameterCard(
                        "${rocketDetail.height}${stringResource(id = R.string.meters)}",
                        stringResource(id = R.string.height)
                    )
                    ParameterCard(
                        "${rocketDetail.diameter}${stringResource(id = R.string.meters)}",
                        stringResource(id = R.string.diameter)
                    )
                    ParameterCard(
                        "${rocketDetail.mass}${stringResource(id = R.string.kilograms)}",
                        stringResource(id = R.string.mass)
                    )
                }

                StageInfo(stringResource(id = R.string.first_stage), rocketDetail.firstStage)
                StageInfo(stringResource(id = R.string.second_stage), rocketDetail.secondStage)

                RocketPhotos(rocketDetail.images)

            }
        }
    }
}

@Composable
fun ParameterCard(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(color = PinkShade, shape = Shapes.medium)
            .padding(16.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.White)
    }
}

@Composable
fun StageInfo(title: String, stage: StageDetailModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(color = SoftGray, shape = Shapes.medium)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )
        StageInfoRow(
            drawableId = R.drawable.reusable,
            value = if (stage.reusable) stringResource(id = R.string.reusable) else stringResource(
                id = R.string.not_reusable
            )
        )
        StageInfoRow(
            drawableId = R.drawable.engine,
            value = "${stage.engines} ${stringResource(id = R.string.engines)}"
        )
        StageInfoRow(
            drawableId = R.drawable.fuel,
            value = "${stage.fuelAmountTons} ${stringResource(id = R.string.tons_of_fuel)}"
        )
        StageInfoRow(
            drawableId = R.drawable.burn,
            value = "${stage.burnTimeSEC} ${stringResource(id = R.string.seconds_burn_time)}"
        )
    }
}

@Composable
fun StageInfoRow(@DrawableRes drawableId: Int, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = drawableId),
            contentDescription = null,
            tint = Color.Unspecified
        )
        Text(
            text = value,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun RocketPhotos(rocketImageUrls: List<String>) {
    Text(
        text = "Photos",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(16.dp)
    )
    rocketImageUrls.forEach { image ->
        AsyncImage(
            model = image,
            contentDescription = stringResource(id = R.string.rocket_image),
            modifier = Modifier
                .padding(16.dp)
                .clip(Shapes.medium)
                .fillMaxWidth(),
            contentScale = ContentScale.Crop
        )
    }

}

@ThemeModePreview
@FontScalePreview
@DeviceFormatPreview
@Composable
private fun RocketDetailPreview() {
    TemplateTheme {
        RocketDetailComposable(
            state = RocketDetailViewModel.UiState.Success(
                RocketDetailModel(
                    name = "Falcon Heavy",
                    description = "With the ability to lift into orbit over 54 metric tons (119,000 lb)--a mass equivalent to a 737 jetliner loaded with passengers, crew, luggage and fuel--Falcon Heavy can lift more than twice the payload of the next closest operational vehicle, the Delta IV Heavy, at one-third the cost.",
                    height = 70.0,
                    diameter = 12.2,
                    mass = 1420788,
                    firstStage = StageDetailModel(
                        reusable = true,
                        engines = 27,
                        fuelAmountTons = 11505.0,
                        burnTimeSEC = 162
                    ),
                    secondStage = StageDetailModel(
                        reusable = true,
                        engines = 1,
                        fuelAmountTons = 90.0,
                        burnTimeSEC = 397
                    ),
                    images = listOf(
                        "https://farm5.staticflickr.com/4599/38583829295_581f34dd84_b.jpg",
                        "https://farm5.staticflickr.com/4645/38583830575_3f0f7215e6_b.jpg",
                        "https://farm5.staticflickr.com/4696/40126460511_b15bf84c85_b.jpg",
                        "https://farm5.staticflickr.com/4711/40126461411_aabc643fd8_b.jpg"
                    )
                )
            ),
            onBackClicked = {},
            onLaunchClicked = {}
        )
    }
}

