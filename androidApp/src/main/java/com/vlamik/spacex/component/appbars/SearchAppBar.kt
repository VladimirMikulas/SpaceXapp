package com.vlamik.spacex.component.appbars

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vlamik.spacex.R
import com.vlamik.spacex.common.filtering.FilterItem
import com.vlamik.spacex.common.filtering.FilterState
import com.vlamik.spacex.common.filtering.FilterValue
import com.vlamik.spacex.common.utils.asString

/**
 * A dynamic app bar that switches between a normal display mode and an interactive search mode
 * with filters. It manages its own `isSearching` state and focus.
 *
 * @param onSearchTextChange Callback when the search query changes.
 * @param onFilterValueToggle Callback for individual filter chip toggles, providing the
 * filter key, the specific filter value, and its new selection state.
 * @param onMenuClick Callback for the menu icon click.
 */
@Composable
fun SearchAppBar(
    title: String,
    searchText: String,
    activeFilters: FilterState,
    filters: List<FilterItem>,
    onSearchTextChange: (String) -> Unit,
    onFilterValueToggle: (filterKey: String, filterValue: FilterValue, isSelected: Boolean) -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearching by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearching) {
        if (isSearching) {
            focusRequester.requestFocus()
        }
    }

    Surface(
        shadowElevation = 4.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.statusBarsPadding()
        ) {
            if (isSearching) {
                SearchBarContent(
                    searchText = searchText,
                    activeFilters = activeFilters,
                    filters = filters,
                    onSearchTextChange = onSearchTextChange,
                    onFilterValueToggle = onFilterValueToggle,
                    onBackClick = { isSearching = false },
                    focusRequester = focusRequester
                )
            } else {
                NormalAppBarContent(
                    title = title,
                    onMenuClick = onMenuClick,
                    onSearchClick = { isSearching = true }
                )
            }
        }
    }
}

/**
 * Content displayed when in search mode, combining the search input and filter section.
 */
@Composable
private fun SearchBarContent(
    searchText: String,
    activeFilters: FilterState,
    filters: List<FilterItem>,
    onSearchTextChange: (String) -> Unit,
    onFilterValueToggle: (filterKey: String, filterValue: FilterValue, isSelected: Boolean) -> Unit,
    onBackClick: () -> Unit,
    focusRequester: FocusRequester
) {
    Column {
        SearchInputRow(
            searchText = searchText,
            onSearchTextChange = onSearchTextChange,
            onBackClick = onBackClick,
            focusRequester = focusRequester
        )
        HorizontalDivider()
        FilterSection(
            filters = filters,
            activeFilters = activeFilters,
            onFilterValueToggle = onFilterValueToggle
        )
    }
}

/**
 * Defines the search input row: back button, [TextField], and clear text button.
 */
@Composable
private fun SearchInputRow(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onBackClick: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back)
            )
        }

        TextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            placeholder = { Text(stringResource(R.string.search_items_placeholder)) },
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = { onSearchTextChange("") }) {
                        Icon(Icons.Default.Close, stringResource(R.string.clear))
                    }
                }
            },
            singleLine = true
        )
    }
}

/**
 * Displays a scrollable list of filter categories and their chips.
 *
 * @param onFilterValueToggle Callback for individual filter chip toggles, providing the
 * filter key, the specific filter value, and its new selection state.
 */
@Composable
private fun FilterSection(
    filters: List<FilterItem>,
    activeFilters: FilterState,
    onFilterValueToggle: (filterKey: String, filterValue: FilterValue, isSelected: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxFilterHeight = 160.dp
    LazyColumn(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .heightIn(max = maxFilterHeight)
    ) {
        items(filters) { filter ->
            Text(
                text = filter.displayName.asString(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            FilterChipsRow(
                filterValues = filter.values,
                selectedFilterValues = activeFilters.selectedFilters[filter.key].orEmpty(),
                onFilterValueClick = { clickedFilterValue, isSelected ->
                    onFilterValueToggle(filter.key, clickedFilterValue, isSelected)
                }
            )
            HorizontalDivider()
        }
    }
}

/**
 * Displays a horizontal row of [FilterChip] components for a given filter category.
 * Supports different [FilterValue] types, rendering them based on their `displayName`.
 *
 * @param onFilterValueClick Callback when a chip is clicked, providing the [FilterValue]
 * and its new selection state.
 */
@Composable
private fun FilterChipsRow(
    filterValues: List<FilterValue>,
    selectedFilterValues: Set<FilterValue>,
    onFilterValueClick: (filterValue: FilterValue, isSelected: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.padding(bottom = 8.dp)
    ) {
        items(filterValues) { filterValue ->
            val isSelected = selectedFilterValues.contains(filterValue)

            when (filterValue) {
                is FilterValue.ExactMatch -> {
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterValueClick(filterValue, !isSelected) },
                        label = { Text(filterValue.displayName.asString()) },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                is FilterValue.Range -> {
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterValueClick(filterValue, !isSelected) },
                        label = { Text(filterValue.displayName.asString()) },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                is FilterValue.YearRange -> {
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterValueClick(filterValue, !isSelected) },
                        label = { Text(filterValue.displayName.asString()) },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    }
}

/**
 * Displays the standard [TopAppBar] content: centered title, menu icon, and search icon.
 */
@Composable
private fun NormalAppBarContent(
    title: String,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                textAlign = TextAlign.Center,
                modifier = modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            Row(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable(onClick = onMenuClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Menu, stringResource(R.string.menu))
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, stringResource(R.string.search))
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}