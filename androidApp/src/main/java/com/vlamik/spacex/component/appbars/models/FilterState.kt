package com.vlamik.spacex.component.appbars.models

import com.vlamik.spacex.core.filtering.FilterValue

/**
 * Data class representing the current state of selected filters.
 * @param selectedFilters A map where the key is the filter key (String) and the value is a set of selected FilterValue objects for that filter.
 */
data class FilterState(
    val selectedFilters: Map<String, Set<FilterValue>> = emptyMap()
)