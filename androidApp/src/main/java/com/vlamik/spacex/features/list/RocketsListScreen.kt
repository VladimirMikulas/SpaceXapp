package com.vlamik.spacex.features.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vlamik.core.domain.models.RocketListItemModel
import com.vlamik.spacex.R
import com.vlamik.spacex.component.ErrorMessage
import com.vlamik.spacex.component.appbars.SectionAppBar
import com.vlamik.spacex.features.list.RocketsListViewModel.ListScreenUiState
import com.vlamik.spacex.features.list.RocketsListViewModel.ListScreenUiState.ErrorFromAPI
import com.vlamik.spacex.features.list.RocketsListViewModel.ListScreenUiState.LoadingFromAPI
import com.vlamik.spacex.features.list.RocketsListViewModel.ListScreenUiState.UpdateSuccess
import com.vlamik.spacex.theme.SoftGray
import com.vlamik.spacex.theme.TemplateTheme
import com.vlamik.spacex.utils.preview.DeviceFormatPreview
import com.vlamik.spacex.utils.preview.FontScalePreview
import com.vlamik.spacex.utils.preview.ThemeModePreview


@Composable
fun RocketsListScreen(
    viewModel: RocketsListViewModel,
    openDetailsClicked: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    RocketsListComposable(
        state = state,
        onDetailsClicked = openDetailsClicked,
        onRefresh = viewModel::refresh
    )
}

@Composable
private fun RocketsListComposable(
    state: ListScreenUiState,
    onDetailsClicked: (String) -> Unit,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = { SectionAppBar(title = stringResource(id = R.string.rockets)) }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PullToRefreshBox(
                isRefreshing = state is LoadingFromAPI,
                onRefresh = onRefresh
            ) {
                when (state) {
                    is LoadingFromAPI -> {
                    }

                    is UpdateSuccess -> {
                        Surface(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp)),
                            color = SoftGray
                        ) {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                itemsIndexed(state.rockets) { index, rocket ->
                                    RocketsListItem(
                                        rocket = rocket,
                                        onDetailsClicked = onDetailsClicked
                                    )
                                    if (index < state.rockets.lastIndex) {
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.background,
                                            thickness = 2.dp,
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    is ErrorFromAPI -> {
                        ErrorMessage(errorMessage = stringResource(id = R.string.api_error))
                    }
                }
            }
        }
    }
}

@Composable
private fun RocketsListItem(
    rocket: RocketListItemModel,
    onDetailsClicked: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetailsClicked(rocket.id) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.rocket),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .padding(end = 16.dp),
            tint = Color.Unspecified
        )
        RocketInfo(rocket = rocket, modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun RocketInfo(rocket: RocketListItemModel, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = rocket.name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${stringResource(id = R.string.label_first_flight)} ${rocket.firstFlight}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@ThemeModePreview
@FontScalePreview
@DeviceFormatPreview
@Composable
private fun RocketsListScreenPreview() {
    TemplateTheme {
        RocketsListComposable(
            state = UpdateSuccess(
                rockets = (1..10).map {
                    RocketListItemModel(
                        "5e9d0d95eda69974db09d1ed",
                        "Falcon Heavy",
                        "6.2.2018",
                    )
                },

                ),
            onDetailsClicked = {},
            onRefresh = {})
    }
}
