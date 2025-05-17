package com.vlamik.spacex.features.crewlist

import android.content.Intent
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
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.vlamik.core.domain.models.CrewListItemModel
import com.vlamik.spacex.R
import com.vlamik.spacex.common.utils.preview.DeviceFormatPreview
import com.vlamik.spacex.common.utils.preview.FontScalePreview
import com.vlamik.spacex.common.utils.preview.ThemeModePreview
import com.vlamik.spacex.component.appbars.SectionAppBar
import com.vlamik.spacex.component.drawer.AppDrawer
import com.vlamik.spacex.features.crewlist.CrewListViewModel.ListScreenUiState
import com.vlamik.spacex.features.crewlist.CrewListViewModel.ListScreenUiState.DataError
import com.vlamik.spacex.features.crewlist.CrewListViewModel.ListScreenUiState.LoadingData
import com.vlamik.spacex.features.crewlist.CrewListViewModel.ListScreenUiState.UpdateSuccess
import com.vlamik.spacex.navigation.NavRoutes
import com.vlamik.spacex.theme.SoftGray
import com.vlamik.spacex.theme.TemplateTheme
import kotlinx.coroutines.launch


@Composable
fun CrewListScreen(
    viewModel: CrewListViewModel,
    navigateTo: (NavRoutes) -> Unit,
    currentRoute: NavRoutes = NavRoutes.Crew
) {
    val state by viewModel.state.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    AppDrawer(
        currentRoute = currentRoute,
        onItemSelected = navigateTo,
        drawerState = drawerState
    ) {
        CrewListScreenContent(
            state = state,
            onRefresh = viewModel::refresh,
            onMenuClick = {
                scope.launch { drawerState.open() }
            }
        )
    }
}

@Composable
private fun CrewListScreenContent(
    state: ListScreenUiState,
    onRefresh: () -> Unit,
    onMenuClick: () -> Unit
) {
    Scaffold(
        topBar = {
            SectionAppBar(
                title = stringResource(R.string.crew),
                menuButtonClickAction = onMenuClick
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PullToRefreshBox(
                isRefreshing = state is LoadingData,
                onRefresh = onRefresh,
                modifier = Modifier.padding(paddingValues)
            ) {
                when (state) {
                    LoadingData -> {
                    }

                    is UpdateSuccess -> CrewListContent(
                        crew = state.crew,
                        onRefresh = onRefresh
                    )

                    DataError -> ErrorState(onRetry = onRefresh)
                }
            }
        }
    }
}

@Composable
private fun CrewListContent(
    crew: List<CrewListItemModel>,
    onRefresh: () -> Unit
) {
    val openWikipedia = rememberWikipediaOpener()
    if (crew.isEmpty()) {
        EmptyState(onRefresh = onRefresh)
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
                itemsIndexed(crew) { index, crewMember ->
                    CrewListItem(
                        crewMember = crewMember,
                        onClicked = { openWikipedia(crewMember.wikipedia) }
                    )
                    if (index < crew.lastIndex) {
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
private fun CrewListItem(
    crewMember: CrewListItemModel,
    onClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClicked)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .padding(end = 16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        CrewInfo(crewMember = crewMember, modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun CrewInfo(crewMember: CrewListItemModel, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = crewMember.name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = crewMember.agency,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = crewMember.status,
                style = MaterialTheme.typography.bodySmall,
                color = when (crewMember.status.lowercase()) {
                    stringResource(R.string.active) -> Color.Green
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun rememberWikipediaOpener(): (String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { url: String ->
            try {
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                context.startActivity(intent)
            } catch (e: Exception) {
                // Error handling
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
private fun EmptyState(onRefresh: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.People,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_crew_available),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRefresh) {
            Text(text = stringResource(R.string.refresh))
        }
    }
}

@ThemeModePreview
@FontScalePreview
@DeviceFormatPreview
@Composable
private fun CrewListScreenPreview() {
    TemplateTheme {
        CrewListScreenContent(
            state = UpdateSuccess(
                crew = listOf(
                    CrewListItemModel(
                        name = "Robert Behnken",
                        agency = "NASA",
                        wikipedia = "https://en.wikipedia.org/wiki/Robert_L._Behnken",
                        status = "Active"
                    ),
                    CrewListItemModel(
                        name = "Douglas Hurley",
                        agency = "NASA",
                        wikipedia = "https://en.wikipedia.org/wiki/Douglas_G._Hurley",
                        status = "Active"
                    ),
                    CrewListItemModel(
                        name = "Michael Collins",
                        agency = "NASA",
                        wikipedia = "https://en.wikipedia.org/wiki/Michael_Collins_(astronaut)",
                        status = "Retired"
                    )
                )
            ),
            onRefresh = {},
            onMenuClick = {}
        )
    }

}

@ThemeModePreview
@FontScalePreview
@DeviceFormatPreview
@Composable
private fun CrewListLoadingPreview() {
    TemplateTheme {
        CrewListScreenContent(
            state = LoadingData,
            onRefresh = {},
            onMenuClick = {}
        )
    }
}

@ThemeModePreview
@FontScalePreview
@DeviceFormatPreview
@Composable
private fun CrewListErrorPreview() {
    TemplateTheme {
        CrewListScreenContent(
            state = DataError,
            onRefresh = {},
            onMenuClick = {}
        )
    }
}

@ThemeModePreview
@FontScalePreview
@DeviceFormatPreview
@Composable
private fun CrewListEmptyPreview() {
    TemplateTheme {
        CrewListScreenContent(
            state = UpdateSuccess(emptyList()),
            onRefresh = {},
            onMenuClick = {}
        )
    }
}

@ThemeModePreview
@FontScalePreview
@DeviceFormatPreview
@Composable
private fun CrewListItemPreview() {
    TemplateTheme {
        Surface {
            CrewListItem(
                crewMember = CrewListItemModel(
                    name = "Robert Behnken",
                    agency = "NASA",
                    wikipedia = "https://en.wikipedia.org/wiki/Robert_L._Behnken",
                    status = "Active"
                ),
                onClicked = {}
            )
        }
    }
}

@ThemeModePreview
@FontScalePreview
@DeviceFormatPreview
@Composable
private fun CrewListContentPreview() {
    TemplateTheme {
        CrewListContent(
            crew = listOf(
                CrewListItemModel(
                    name = "Robert Behnken",
                    agency = "NASA",
                    wikipedia = "https://en.wikipedia.org/wiki/Robert_L._Behnken",
                    status = "Active"
                ),
                CrewListItemModel(
                    name = "Douglas Hurley",
                    agency = "NASA",
                    wikipedia = "https://en.wikipedia.org/wiki/Douglas_G._Hurley",
                    status = "Active"
                )
            ),
            onRefresh = {}
        )
    }
}

@ThemeModePreview
@FontScalePreview
@DeviceFormatPreview
@Composable
private fun CrewListEmptyContentPreview() {
    TemplateTheme {
        CrewListContent(
            crew = emptyList(),
            onRefresh = {}
        )
    }
}
