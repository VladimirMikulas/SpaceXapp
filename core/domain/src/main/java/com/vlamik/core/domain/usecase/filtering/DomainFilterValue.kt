package com.vlamik.core.domain.usecase.filtering

/**
 * Sealed class representing different types of filter values in the domain layer.
 * These models hold the raw data needed for filtering and are independent of the presentation layer.
 */
sealed interface DomainFilterValue {

    /**
     * Represents an exact match value (e.g., rocket name).
     * @param value The actual String value for filtering.
     */
    data class ExactMatch(val value: String) : DomainFilterValue

    /**
     * Represents a numerical range (e.g., height, diameter, mass).
     * @param start The lower bound of the range (inclusive), null if it's an "under" range.
     * @param end The upper bound of the range (inclusive), null if it's an "over" range.
     */
    data class Range(
        val start: Double? = null,
        val end: Double? = null
    ) : DomainFilterValue

    /**
     * Represents a range of years (e.g., first flight date).
     * @param startYear The lower bound of the year range (inclusive), null if it's a "before" range.
     * @param endYear The upper bound of the year range (inclusive), null if it's an "after" range.
     */
    data class YearRange(
        val startYear: Int? = null,
        val endYear: Int? = null
    ) : DomainFilterValue
}
