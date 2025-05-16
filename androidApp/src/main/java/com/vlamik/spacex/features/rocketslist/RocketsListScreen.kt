package com.vlamik.spacex.features.rocketslist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vlamik.core.domain.models.RocketListItemModel
import com.vlamik.spacex.R
import com.vlamik.spacex.component.LoadingIndicator
import com.vlamik.spacex.component.appbars.SearchAppBar
import com.vlamik.spacex.component.appbars.models.FilterParameter
import com.vlamik.spacex.component.appbars.models.FilterState
import com.vlamik.spacex.component.drawer.AppDrawer
import com.vlamik.spacex.core.filtering.FilterItem
import com.vlamik.spacex.core.filtering.FilterValue
import com.vlamik.spacex.core.utils.UiText
import com.vlamik.spacex.core.utils.preview.DeviceFormatPreview
import com.vlamik.spacex.core.utils.preview.FontScalePreview
import com.vlamik.spacex.core.utils.preview.ThemeModePreview
import com.vlamik.spacex.navigation.NavRoutes
import com.vlamik.spacex.theme.SoftGray
import com.vlamik.spacex.theme.TemplateTheme
import kotlinx.coroutines.launch

@Composable
fun RocketsListScreen(
    viewModel: RocketsListViewModel,
    openDetailsClicked: (String) -> Unit,
    navigateTo: (NavRoutes) -> Unit,
    currentRoute: NavRoutes = NavRoutes.RocketsList
) {
    val state by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Handling side effects
    LaunchedEffect(key1 = viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RocketsListContract.Effect.OpenRocketDetails -> openDetailsClicked(effect.rocketId)
                is RocketsListContract.Effect.NavigateToRoute -> navigateTo(effect.route)
                is RocketsListContract.Effect.OpenDrawer -> {
                    scope.launch { drawerState.open() }
                }
            }
        }
    }

    AppDrawer(
        currentRoute = currentRoute,
        onItemSelected = { route ->
            viewModel.processIntent(
                RocketsListContract.Intent.NavigateTo(
                    route
                )
            )
        },
        drawerState = drawerState
    ) {
        RocketsListContent(
            state = state,
            onIntent = viewModel::processIntent
        )
    }
}

@Composable
private fun RocketsListContent(
    state: RocketsListContract.State,
    onIntent: (RocketsListContract.Intent) -> Unit
) {
    // Map FilterItem from ViewModel state to UI-specific FilterParameter
    val filterParameters = state.availableFilters.map { filterItem ->
        FilterParameter(
            key = filterItem.key,
            displayName = filterItem.displayName,
            values = filterItem.values
        )
    }

    Scaffold(
        topBar = {
            SearchAppBar(
                title = stringResource(R.string.rockets),
                searchText = state.searchQuery,
                activeFilters = state.activeFilters,
                filters = filterParameters,
                onSearchTextChange = { query ->
                    onIntent(
                        RocketsListContract.Intent.SearchQueryChanged(
                            query
                        )
                    )
                },
                onFilterSelected = { filterState ->
                    onIntent(
                        RocketsListContract.Intent.FilterSelected(
                            filterState
                        )
                    )
                },
                onMenuClick = {
                    onIntent(RocketsListContract.Intent.DrawerMenuClicked)
                }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { onIntent(RocketsListContract.Intent.RefreshRockets) },
            ) {
                when {
                    state.isLoading -> {
                        LoadingIndicator()
                    }

                    state.error != null -> {
                        ErrorState(
                            errorMessage = state.error,
                            onRetry = { onIntent(RocketsListContract.Intent.RetryLoadRockets) }
                        )
                    }

                    else -> {
                        RocketDataContent(
                            rockets = state.filteredRockets,
                            onDetailsClicked = { rocketId ->
                                onIntent(RocketsListContract.Intent.RocketClicked(rocketId))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorState(errorMessage: UiText, onRetry: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = errorMessage.asString(context),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

@Composable
private fun RocketDataContent(
    rockets: List<RocketListItemModel>,
    onDetailsClicked: (String) -> Unit
) {
    if (rockets.isEmpty()) {
        EmptyState()
    } else {
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
                itemsIndexed(rockets) { index, rocket ->
                    RocketsListItem(
                        rocket = rocket,
                        onDetailsClicked = onDetailsClicked
                    )
                    if (index < rockets.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.background, // Or another suitable divider color
                            thickness = 2.dp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_rockets_found),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
            tint = Color.Unspecified // If the icon has its own colors, otherwise consider MaterialTheme.colorScheme.primary
        )
        RocketInfo(rocket = rocket, modifier = Modifier
            .weight(1f)
            .fillMaxWidth())
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = stringResource(
                R.string.cd_navigate_to_details,
                rocket.name
            ),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun RocketInfo(rocket: RocketListItemModel, modifier: Modifier) {
    Column(
        modifier = modifier,
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


// --- Preview Data ---
val previewRockets = listOf(
    RocketListItemModel(
        id = "1", name = "Falcon 9", firstFlight = "2010-06-04",
        height = 70.0, diameter = 3.7, mass = 549054
    ),
    RocketListItemModel(
        id = "2", name = "Falcon Heavy", firstFlight = "2018-02-06",
        height = 70.0, diameter = 12.2, mass = 1420788
    ),
    RocketListItemModel(
        id = "3", name = "Starship", firstFlight = "2023-04-20",
        height = 120.0, diameter = 9.0, mass = 5000000
    )
)

val previewAvailableFilters = listOf(
    FilterItem(
        key = "name",
        displayName = UiText.from(R.string.filter_name),
        values = listOf(
            FilterValue.ExactMatch(displayName = UiText.dynamic("Falcon 9"), value = "Falcon 9"),
            FilterValue.ExactMatch(
                displayName = UiText.dynamic("Falcon Heavy"),
                value = "Falcon Heavy"
            ),
            FilterValue.ExactMatch(displayName = UiText.dynamic("Starship"), value = "Starship")
        )
    ),
    FilterItem(
        key = "first_flight",
        displayName = UiText.from(R.string.filter_first_flight),
        values = listOf(
            FilterValue.YearRange(displayName = UiText.dynamic("Before 2015"), endYear = 2015),
            FilterValue.YearRange(
                displayName = UiText.dynamic("2015-2020"),
                startYear = 2015,
                endYear = 2020
            ),
            FilterValue.YearRange(displayName = UiText.dynamic("After 2020"), startYear = 2020)
        )
    )
)

// --- Preview Composable Functions ---

@ThemeModePreview
@FontScalePreview
@DeviceFormatPreview
@Composable
private fun RocketsListScreenPreview_DataLoaded() {
    TemplateTheme {
        RocketsListContent(
            state = RocketsListContract.State(
                isLoading = false,
                rockets = previewRockets,
                filteredRockets = previewRockets.take(2),
                availableFilters = previewAvailableFilters, // Use updated preview data
                searchQuery = "",
                activeFilters = FilterState(
                    selectedFilters = mapOf(
                        "name" to setOf(
                            FilterValue.ExactMatch(
                                displayName = UiText.dynamic("Falcon 9"),
                                value = "Falcon 9"
                            )
                        )
                    )
                )
            ),
            onIntent = {}
        )
    }
}

@ThemeModePreview
@FontScalePreview
@DeviceFormatPreview
@Composable
private fun RocketsListScreenPreview_Loading() {
    TemplateTheme {
        RocketsListContent(
            state = RocketsListContract.State(isLoading = true),
            onIntent = {}
        )
    }
}

@ThemeModePreview
@FontScalePreview
@DeviceFormatPreview
@Composable
private fun RocketsListScreenPreview_Error() {
    TemplateTheme {
        RocketsListContent(
            state = RocketsListContract.State(
                isLoading = false,
                error = UiText.from(R.string.data_error)
            ),
            onIntent = {}
        )
    }
}

@ThemeModePreview
@FontScalePreview
@DeviceFormatPreview
@Composable
private fun RocketsListScreenPreview_Empty() {
    TemplateTheme {
        RocketsListContent(
            state = RocketsListContract.State(
                isLoading = false,
                rockets = emptyList(),
                filteredRockets = emptyList(),
                availableFilters = previewAvailableFilters,
                searchQuery = "Non-existent rocket"
            ),
            onIntent = {}
        )
    }
}
