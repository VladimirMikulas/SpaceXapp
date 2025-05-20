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
 * with filters. Manages its own search state and focus.
 *
 * @param onFilterSelected Callback providing the entire updated [FilterState] when a filter changes.
 */
@Composable
fun SearchAppBar(
    title: String,
    searchText: String,
    activeFilters: FilterState,
    filters: List<FilterItem>,
    onSearchTextChange: (String) -> Unit,
    onFilterSelected: (FilterState) -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Manages UI mode: true for search, false for normal.
    var isSearching by remember { mutableStateOf(false) }
    // Requests focus for TextField when entering search mode.
    val focusRequester = remember { FocusRequester() }

    // Requests focus for the search TextField when `isSearching` becomes true.
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
                // Displays search input and filter section.
                SearchBarContent(
                    searchText = searchText,
                    activeFilters = activeFilters,
                    filters = filters,
                    onSearchTextChange = onSearchTextChange,
                    onFilterSelected = onFilterSelected,
                    onBackClick = { isSearching = false }, // Exits search mode.
                    focusRequester = focusRequester
                )
            } else {
                // Displays normal app bar.
                NormalAppBarContent(
                    title = title,
                    onMenuClick = onMenuClick,
                    onSearchClick = { isSearching = true } // Enters search mode.
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
    onFilterSelected: (FilterState) -> Unit,
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
        HorizontalDivider() // Separates search input from filters.
        FilterSection(
            filters = filters,
            activeFilters = activeFilters,
            onFilterSelected = onFilterSelected
        )
    }
}

/**
 * Defines the search input row: back button, [TextField], and clear text button.
 *
 * @param focusRequester Attached to the [TextField] for programmatic focus control.
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
            colors = TextFieldDefaults.colors( // Transparent TextField styling.
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            trailingIcon = {
                // Shows clear button only if text is present.
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
 * @param onFilterSelected Callback invoked with the updated [FilterState] after a chip selection.
 */
@Composable
private fun FilterSection(
    filters: List<FilterItem>,
    activeFilters: FilterState,
    onFilterSelected: (FilterState) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxFilterHeight = 160.dp // Max height before scrolling.
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
                    // Logic to update FilterState based on chip click. This could ideally
                    // be moved to a ViewModel or dedicated state holder for better separation.
                    val updatedSelectedFilters = activeFilters.selectedFilters.toMutableMap()
                    val currentSelectedValues =
                        updatedSelectedFilters[filter.key]?.toMutableSet()
                            ?: mutableSetOf()

                    if (isSelected) {
                        currentSelectedValues.add(clickedFilterValue)
                    } else {
                        currentSelectedValues.remove(clickedFilterValue)
                    }
                    updatedSelectedFilters[filter.key] = currentSelectedValues.toSet()

                    onFilterSelected(activeFilters.copy(selectedFilters = updatedSelectedFilters.toMap()))
                }
            )
            HorizontalDivider() // Separates filter categories.
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
            // Checks if the filterValue is currently selected.
            val isSelected = selectedFilterValues.contains(filterValue)

            // Renders different FilterChip types based on the FilterValue.
            // Colors adjust based on the FilterValue type for visual distinction.
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
                textAlign = TextAlign.Center, // Centers text within its space.
                modifier = modifier.fillMaxWidth() // Fills available width.
            )
        },
        navigationIcon = {
            Row(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable(onClick = onMenuClick), // Makes the whole row clickable.
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