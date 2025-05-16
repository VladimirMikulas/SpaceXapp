package com.vlamik.spacex.core.filtering

object RangeFilter {
    /**
     * Checks if a given numerical value falls within any of the selected ranges.
     * @param value The numerical value to check.
     * @param selectedRanges The set of selected FilterValue.Range objects.
     * @return True if the value falls within at least one of the selected ranges, otherwise False.
     * No longer uses Context.
     */
    fun matches(
        value: Double,
        selectedRanges: Set<FilterValue.Range>
    ): Boolean {
        if (selectedRanges.isEmpty()) return true

        return selectedRanges.any { rangeValue ->
            when {
                // If FilterValue defines both start and end (represents an "from - to" range)
                rangeValue.start != null && rangeValue.end != null ->
                    value >= rangeValue.start && value <= rangeValue.end
                // If FilterValue defines only start (represents an "over" range)
                rangeValue.start != null ->
                    value >= rangeValue.start
                // If FilterValue defines only end (represents an "under" range)
                rangeValue.end != null ->
                    value <= rangeValue.end
                // Other case (should not happen with correct FilterValue)
                else -> false
            }
        }
    }
}