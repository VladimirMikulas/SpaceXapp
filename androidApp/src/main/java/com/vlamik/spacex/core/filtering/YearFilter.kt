package com.vlamik.spacex.core.filtering

object YearFilter {
    /**
     * Checks if the year from a given date string falls within any of the selected year ranges.
     * @param date The date string.
     * @param selectedRanges The set of selected FilterValue.YearRange objects.
     * @return True if the year falls within at least one of the selected ranges, otherwise False.
     * No longer uses Context for filtering logic.
     */
    fun matches(
        date: String,
        selectedRanges: Set<FilterValue.YearRange> // Accepting Set<FilterValue.YearRange>
    ): Boolean {
        val year = FilterUtils.extractYear(date)
            ?: return false // Still need to extract the year using FilterUtils
        if (selectedRanges.isEmpty()) return true

        return selectedRanges.any { yearRangeValue ->
            // Use the values from the FilterValue.YearRange object
            when {
                // If FilterValue defines both startYear and endYear (represents an "from - to" range)
                yearRangeValue.startYear != null && yearRangeValue.endYear != null ->
                    year >= yearRangeValue.startYear && year <= yearRangeValue.endYear
                // If FilterValue defines only startYear (represents an "after year" range)
                yearRangeValue.startYear != null ->
                    year >= yearRangeValue.startYear
                // If FilterValue defines only endYear (represents a "before year" range)
                yearRangeValue.endYear != null ->
                    year <= yearRangeValue.endYear
                // Other case (should not happen with correct FilterValue)
                else -> false
            }
        }
    }
}