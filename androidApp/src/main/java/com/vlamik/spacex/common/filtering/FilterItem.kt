package com.vlamik.spacex.common.filtering

import com.vlamik.spacex.common.utils.UiText

/**
 * Data class representing one type of filter (e.g., "Height", "First Flight").
 * @param key Unique filter key (used in FilterConstants).
 * @param displayName UiText for displaying the filter name to the user.
 * @param values List of available values for this filter, represented by the FilterValue class.
 * @param extraParams Additional parameters, e.g., units, represented as Map<String, UiText>.
 */
data class FilterItem(
    val key: String,
    val displayName: UiText,
    val values: List<FilterValue>,
    val extraParams: Map<String, UiText> = emptyMap()
)