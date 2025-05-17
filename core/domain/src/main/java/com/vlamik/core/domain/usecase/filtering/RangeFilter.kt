package com.vlamik.core.domain.usecase.filtering

/**
 * Object containing logic to check if a numerical value matches a domain range filter.
 */
object RangeFilter {
    /**
     * Checks if a given numerical value falls within a domain range filter.
     * @param value The numerical value to check.
     * @param domainRange The DomainFilterValue.Range object.
     * @return True if the value falls within the range, otherwise False.
     */
    fun matches(
        value: Double,
        domainRange: DomainFilterValue.Range
    ): Boolean {
        // Use the values from the DomainFilterValue.Range object
        return when {
            // If DomainFilterValue defines both start and end (represents an "from - to" range)
            domainRange.start != null && domainRange.end != null ->
                value >= domainRange.start && value <= domainRange.end
            // If DomainFilterValue defines only start (represents an "over" range)
            domainRange.start != null ->
                value >= domainRange.start
            // If DomainFilterValue defines only end (represents an "under" range)
            domainRange.end != null ->
                value <= domainRange.end
            // Other case (should not happen with correct DomainFilterValue)
            else -> false
        }
    }
}
