package com.vlamik.spacex.features.list

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
import androidx.compose.material3.DrawerState
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import com.vlamik.spacex.component.appbars.SearchAppBar
import com.vlamik.spacex.component.appbars.models.FilterParameter
import com.vlamik.spacex.component.appbars.models.FilterState
import com.vlamik.spacex.component.drawer.AppDrawer
import com.vlamik.spacex.core.filtering.FilterItem
import com.vlamik.spacex.core.utils.preview.DeviceFormatPreview
import com.vlamik.spacex.core.utils.preview.FontScalePreview
import com.vlamik.spacex.core.utils.preview.ThemeModePreview
import com.vlamik.spacex.features.list.RocketsListViewModel.ListScreenUiState.DataError
import com.vlamik.spacex.features.list.RocketsListViewModel.ListScreenUiState.LoadingData
import com.vlamik.spacex.features.list.RocketsListViewModel.ListScreenUiState.UpdateSuccess
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
    val state by viewModel.state.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeFilters by viewModel.activeFilters.collectAsState()
    val availableFilters by viewModel.availableFilters.collectAsState()

    AppDrawer(
        currentRoute = currentRoute,
        onItemSelected = navigateTo,
        drawerState = drawerState
    ) {
    RocketsListContent(
        state = state,
        drawerState = drawerState,
        searchQuery = searchQuery,
        activeFilters = activeFilters,
        availableFilters = availableFilters,
        onDetailsClicked = openDetailsClicked,
        onRefresh = viewModel::refresh,
        onSearchTextChange = viewModel::updateSearchQuery,
        onFilterSelected = viewModel::updateFilters
    )
    }
}

@Composable
private fun RocketsListContent(
    state: RocketsListViewModel.ListScreenUiState,
    drawerState: DrawerState,
    searchQuery: String,
    activeFilters: FilterState,
    availableFilters: List<FilterItem>,
    onDetailsClicked: (String) -> Unit,
    onRefresh: () -> Unit,
    onSearchTextChange: (String) -> Unit,
    onFilterSelected: (FilterState) -> Unit
) {
    val filterParameters = availableFilters.map { filterItem ->
        FilterParameter(
            key = filterItem.key,
            displayName = filterItem.displayName,
            values = filterItem.values
        )
    }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            SearchAppBar(
                title = stringResource(R.string.rockets),
                searchText = searchQuery,
                activeFilters = activeFilters,
                filters = filterParameters,
                onSearchTextChange = onSearchTextChange,
                onFilterSelected = onFilterSelected,
                onMenuClick = {
                    scope.launch { drawerState.open() }
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
                isRefreshing = state is LoadingData,
                onRefresh = onRefresh
            ) {
                when (state) {
                    is LoadingData -> {
                    }

                    is UpdateSuccess -> RocketListContent(
                        rockets = state.filteredRockets,
                        onDetailsClicked = onDetailsClicked
                    )

                    is DataError -> ErrorState(onRetry = onRefresh)
                }
            }
        }
    }
}

@Composable
private fun ErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.data_error),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

@Composable
private fun RocketListContent(
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
                            color = MaterialTheme.colorScheme.background,
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
        RocketsListContent(
            state = UpdateSuccess(
                rockets = listOf(
                    RocketListItemModel(
                        id = "1",
                        name = "Falcon 9",
                        firstFlight = "2010-06-04",
                        height = 70.0,
                        diameter = 3.7,
                        mass = 549054
                    ),
                    RocketListItemModel(
                        id = "2",
                        name = "Falcon Heavy",
                        firstFlight = "2018-02-06",
                        height = 70.0,
                        diameter = 12.2,
                        mass = 1420788
                    ),
                    RocketListItemModel(
                        id = "3",
                        name = "Starship",
                        firstFlight = "2023-04-20",
                        height = 120.0,
                        diameter = 9.0,
                        mass = 5000000
                    )
                ),
                filteredRockets = listOf(
                    RocketListItemModel(
                        id = "1",
                        name = "Falcon 9",
                        firstFlight = "2010-06-04",
                        height = 70.0,
                        diameter = 3.7,
                        mass = 549054
                    ),
                    RocketListItemModel(
                        id = "2",
                        name = "Falcon Heavy",
                        firstFlight = "2018-02-06",
                        height = 70.0,
                        diameter = 12.2,
                        mass = 1420788
                    )
                )
            ),
            drawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
            searchQuery = "",
            activeFilters = FilterState(),
            availableFilters = listOf(
                FilterItem(
                    key = "name",
                    displayName = "Name",
                    values = listOf("Falcon 9", "Falcon Heavy", "Starship")
                ),
                FilterItem(
                    key = "first_flight",
                    displayName = "First Flight",
                    values = listOf("Before 2015", "2015-2020", "After 2020")
                )
            ),
            onDetailsClicked = {},
            onRefresh = {},
            onSearchTextChange = {},
            onFilterSelected = {}
        )
    }
}

@ThemeModePreview
@FontScalePreview
@DeviceFormatPreview
@Composable
private fun LoadingStatePreview() {
    TemplateTheme {
        RocketsListContent(
            state = LoadingData,
            drawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
            searchQuery = "",
            activeFilters = FilterState(),
            availableFilters = emptyList(),
            onDetailsClicked = {},
            onRefresh = {},
            onSearchTextChange = {},
            onFilterSelected = {}
        )
    }
}

@ThemeModePreview
@FontScalePreview
@DeviceFormatPreview
@Composable
private fun ErrorStatePreview() {
    TemplateTheme {
        RocketsListContent(
            state = DataError,
            drawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
            searchQuery = "",
            activeFilters = FilterState(),
            availableFilters = emptyList(),
            onDetailsClicked = {},
            onRefresh = {},
            onSearchTextChange = {},
            onFilterSelected = {}
        )
    }
}

@ThemeModePreview
@FontScalePreview
@DeviceFormatPreview
@Composable
private fun EmptyStatePreview() {
    TemplateTheme {
        RocketsListContent(
            state = UpdateSuccess(
                rockets = emptyList(),
                filteredRockets = emptyList()
            ),
            drawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
            searchQuery = "Non-existent rocket",
            activeFilters = FilterState(),
            availableFilters = emptyList(),
            onDetailsClicked = {},
            onRefresh = {},
            onSearchTextChange = {},
            onFilterSelected = {}
        )
    }
}
