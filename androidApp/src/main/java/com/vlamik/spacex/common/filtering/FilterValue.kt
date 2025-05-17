package com.vlamik.spacex.common.filtering

import com.vlamik.spacex.common.utils.UiText

/**
 * Sealed class representing different types of filter values.
 * Each filter value has a display representation (displayName) and the data needed for the actual filtering.
 */
sealed class FilterValue(open val displayName: UiText) {
    /**
     * Represents an exact match value (e.g., rocket name).
     * @param displayName UiText for displaying the value to the user.
     * @param value The actual String value for filtering.
     */
    data class ExactMatch(override val displayName: UiText, val value: String) :
        FilterValue(displayName)

    /**
     * Represents a numerical range (e.g., height, diameter, mass).
     * @param displayName UiText for displaying the range to the user.
     * @param start The lower bound of the range (inclusive), null if it's an "under" range.
     * @param end The upper bound of the range (inclusive), null if it's an "over" range.
     */
    data class Range(
        override val displayName: UiText,
        val start: Double? = null,
        val end: Double? = null
    ) : FilterValue(displayName)

    /**
     * Represents a range of years (e.g., first flight date).
     * @param displayName UiText for displaying the year range to the user.
     * @param startYear The lower bound of the year range (inclusive), null if it's a "before" range.
     * @param endYear The upper bound of the year range (inclusive), null if it's an "after" range.
     */
    data class YearRange(
        override val displayName: UiText,
        val startYear: Int? = null,
        val endYear: Int? = null
    ) : FilterValue(displayName)
}
