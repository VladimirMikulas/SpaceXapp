package com.vlamik.spacex.component.appbars

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vlamik.spacex.R
import com.vlamik.spacex.common.filtering.FilterItem
import com.vlamik.spacex.common.filtering.FilterState

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
            modifier = Modifier
                .statusBarsPadding()
        ) {
            if (isSearching) {
                SearchModeContent(
                    searchText = searchText,
                    activeFilters = activeFilters,
                    filters = filters,
                    onTextChange = onSearchTextChange,
                    onFilterSelected = onFilterSelected,
                    onBackClick = { isSearching = false },
                    focusRequester = focusRequester
                )
            } else {
                NormalModeContent(
                    title = title,
                    onMenuClick = onMenuClick,
                    onSearchClick = { isSearching = true }
                )
            }
        }
    }
}

@Composable
private fun SearchModeContent(
    searchText: String,
    activeFilters: FilterState,
    filters: List<FilterItem>,
    onTextChange: (String) -> Unit,
    onFilterSelected: (FilterState) -> Unit,
    onBackClick: () -> Unit,
    focusRequester: FocusRequester
) {
    val context = LocalContext.current

    Column {
        Row(
            modifier = Modifier
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
                onValueChange = onTextChange,
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
                        IconButton(onClick = { onTextChange("") }) {
                            Icon(Icons.Default.Close, stringResource(R.string.clear))
                        }
                    }
                },
                singleLine = true
            )
        }

        HorizontalDivider()

        val maxFilterHeight = 160.dp
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .heightIn(max = maxFilterHeight)
        ) {
            items(filters) { filter ->
                Text(
                    text = filter.displayName.asString(context),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    items(filter.values) { filterValue ->
                        val isSelected =
                            activeFilters.selectedFilters[filter.key]?.contains(filterValue) == true

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val updatedSelectedFilters =
                                    activeFilters.selectedFilters.toMutableMap()
                                val currentSelectedValues =
                                    updatedSelectedFilters[filter.key]?.toMutableSet()
                                        ?: mutableSetOf()

                                if (isSelected) {
                                    currentSelectedValues.remove(filterValue)
                                } else {
                                    currentSelectedValues.add(filterValue)
                                }
                                updatedSelectedFilters[filter.key] = currentSelectedValues.toSet()

                                onFilterSelected(activeFilters.copy(selectedFilters = updatedSelectedFilters.toMap()))
                            },
                            label = { Text(filterValue.displayName.asString(context)) },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
                HorizontalDivider()
            }
        }
    }
}


@Composable
private fun NormalModeContent(
    title: String,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit
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
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
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
