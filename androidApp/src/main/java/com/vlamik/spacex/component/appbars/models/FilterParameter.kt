package com.vlamik.spacex.component.appbars.models

import com.vlamik.spacex.core.filtering.FilterValue
import com.vlamik.spacex.core.utils.UiText


/**
 * Data class representing a filter parameter for the UI layer.
 * This is a UI-specific representation derived from the domain's FilterItem.
 * @param key Unique key (example "name", "first_flight").
 * @param displayName UiText for the display name (example "Name", "First Flight").
 * @param values List of available FilterValue objects for this parameter.
 */
data class FilterParameter(
    val key: String,
    val displayName: UiText, // Changed from String to UiText
    val values: List<FilterValue> // Changed from List<String> to List<FilterValue>
)
