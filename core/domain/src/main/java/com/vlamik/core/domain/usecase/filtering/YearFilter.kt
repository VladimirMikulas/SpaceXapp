package com.vlamik.core.domain.usecase.filtering

/**
 * Object containing logic to check if a year matches a domain year range filter.
 */
object YearFilter {
    /**
     * Checks if the year from a given date string falls within a domain year range filter.
     * @param date The date string.
     * @param domainYearRange The DomainFilterValue.YearRange object.
     * @return True if the year falls within the range, otherwise False.
     */
    fun matches(
        date: String,
        domainYearRange: DomainFilterValue.YearRange // Accepts DomainFilterValue.YearRange
    ): Boolean {
        // FilterUtils.extractYear should also be in the domain layer
        val year = FilterUtils.extractYear(date) ?: return false

        // Use the values from the DomainFilterValue.YearRange object
        return when {
            // If DomainFilterValue defines both startYear and endYear (represents an "from - to" range)
            domainYearRange.startYear != null && domainYearRange.endYear != null ->
                year >= domainYearRange.startYear && year <= domainYearRange.endYear
            // If DomainFilterValue defines only startYear (represents an "after year" range)
            domainYearRange.startYear != null ->
                year >= domainYearRange.startYear
            // If DomainFilterValue defines only endYear (represents a "before year" range)
            domainYearRange.endYear != null ->
                year <= domainYearRange.endYear
            // Other case (should not happen with correct DomainFilterValue)
            else -> false
        }
    }
}
