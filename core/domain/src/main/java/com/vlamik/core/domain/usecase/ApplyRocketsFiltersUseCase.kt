package com.vlamik.core.domain.usecase

import com.vlamik.core.domain.models.RocketListItemModel
import com.vlamik.core.domain.usecase.filtering.DomainFilterValue
import com.vlamik.core.domain.usecase.filtering.FilterConstants
import com.vlamik.core.domain.usecase.filtering.RangeFilter
import com.vlamik.core.domain.usecase.filtering.YearFilter
import javax.inject.Inject

/**
 * UseCase to apply active filters (based on DomainFilterValue) to a list of rockets.
 * This logic operates purely on domain models.
 */
class ApplyRocketsFiltersUseCase @Inject constructor() {
    /**
     * Filters a list of rockets based on the selected domain filter values.
     *
     * @param rockets The list of rockets to filter.
     * @param selectedDomainFilters A map where the key is the filter key (String)
     * and the value is a set of selected DomainFilterValue objects for that filter.
     * @return A new list containing only the rockets that match the filter criteria.
     */
    operator fun invoke(
        rockets: List<RocketListItemModel>,
        selectedDomainFilters: Map<String, Set<DomainFilterValue>> // Accepts domain filter values
    ): List<RocketListItemModel> {
        if (selectedDomainFilters.isEmpty() || selectedDomainFilters.all { it.value.isEmpty() }) {
            return rockets // If no filters are selected or all filter sets are empty, return the original list
        }

        return rockets.filter { rocket ->
            // Filtering logic based on selected DomainFilterValue
            // All selected filters must match for a rocket to be included
            selectedDomainFilters.all { (filterKey, selectedDomainFilterValues) -> // Iterate over Map<String, Set<DomainFilterValue>>
                if (selectedDomainFilterValues.isEmpty()) {
                    true // No values selected for this filter key, so this filter type doesn't restrict the result
                } else {
                    // Check if the rocket matches at least one of the selected domain values for this filter key
                    when (filterKey) {
                        FilterConstants.KEY_NAME ->
                            // For KEY_NAME, we expect DomainFilterValue.ExactMatch.
                            // Check if the rocket's name matches the value in any selected ExactMatch.
                            selectedDomainFilterValues.filterIsInstance<DomainFilterValue.ExactMatch>()
                                .any { it.value.equals(rocket.name, ignoreCase = true) }

                        FilterConstants.KEY_FIRST_FLIGHT ->
                            // For KEY_FIRST_FLIGHT, we expect DomainFilterValue.YearRange.
                            // Check if the rocket's year matches any of the selected YearRange domain values.
                            selectedDomainFilterValues.filterIsInstance<DomainFilterValue.YearRange>()
                                .any { domainYearRange ->
                                    YearFilter.matches(rocket.firstFlight, domainYearRange)
                                }

                        FilterConstants.KEY_HEIGHT ->
                            // For KEY_HEIGHT, we expect DomainFilterValue.Range.
                            // Check if the rocket's height matches any of the selected Range domain values.
                            selectedDomainFilterValues.filterIsInstance<DomainFilterValue.Range>()
                                .any { domainRange ->
                                    RangeFilter.matches(rocket.height, domainRange)
                                }

                        FilterConstants.KEY_DIAMETER ->
                            // For KEY_DIAMETER, we expect DomainFilterValue.Range.
                            // Check if the rocket's diameter matches any of the selected Range domain values.
                            selectedDomainFilterValues.filterIsInstance<DomainFilterValue.Range>()
                                .any { domainRange ->
                                    RangeFilter.matches(rocket.diameter, domainRange)
                                }

                        FilterConstants.KEY_MASS ->
                            // For KEY_MASS, we expect DomainFilterValue.Range (for Double).
                            // Check if the rocket's mass matches any of the selected Range domain values.
                            selectedDomainFilterValues.filterIsInstance<DomainFilterValue.Range>()
                                .any { domainRange ->
                                    RangeFilter.matches(rocket.mass.toDouble(), domainRange)
                                }

                        else -> true // Unknown filter key, assume it matches
                    }
                }
            }
        }
    }
}
