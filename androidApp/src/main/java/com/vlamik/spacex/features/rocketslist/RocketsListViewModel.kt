package com.vlamik.spacex.features.rocketslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlamik.core.domain.GetRocketsList
import com.vlamik.core.domain.models.RocketListItemModel
import com.vlamik.spacex.R
import com.vlamik.spacex.component.appbars.models.FilterState
import com.vlamik.spacex.core.filtering.FilterConstants
import com.vlamik.spacex.core.filtering.FilterItem
import com.vlamik.spacex.core.filtering.FilterUtils
import com.vlamik.spacex.core.filtering.FilterValue
import com.vlamik.spacex.core.filtering.RangeFilter
import com.vlamik.spacex.core.filtering.YearFilter
import com.vlamik.spacex.core.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RocketsListViewModel @Inject constructor(
    private val getRocketsList: GetRocketsList,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RocketsListContract.State())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<RocketsListContract.Effect>()
    val effect = _effect.receiveAsFlow() // Using Channel for one-time events

    init {
        processIntent(RocketsListContract.Intent.LoadRockets)
    }

    fun processIntent(intent: RocketsListContract.Intent) {
        viewModelScope.launch { // Most intents will trigger suspend functions or change state
            when (intent) {
                is RocketsListContract.Intent.LoadRockets -> loadRockets(refresh = false)
                is RocketsListContract.Intent.RefreshRockets -> refreshRockets()
                is RocketsListContract.Intent.SearchQueryChanged -> updateSearchQuery(intent.query)
                is RocketsListContract.Intent.FilterSelected -> updateFilters(intent.newFilterState)
                is RocketsListContract.Intent.RocketClicked -> _effect.send(
                    RocketsListContract.Effect.OpenRocketDetails(
                        intent.rocketId
                    )
                )

                is RocketsListContract.Intent.NavigateTo -> _effect.send(
                    RocketsListContract.Effect.NavigateToRoute(
                        intent.route
                    )
                )

                is RocketsListContract.Intent.DrawerMenuClicked -> _effect.send(RocketsListContract.Effect.OpenDrawer)
                is RocketsListContract.Intent.RetryLoadRockets -> loadRockets(refresh = false)
                is RocketsListContract.Intent.ConsumeError -> _uiState.update { it.copy(error = null) }
            }
        }
    }

    private suspend fun loadRockets(refresh: Boolean) {
        _uiState.update {
            it.copy(
                isLoading = !refresh,
                isRefreshing = refresh,
                error = null
            )
        }

        getRocketsList(refresh)
            .onSuccess { rockets ->
                val availableFilters = createAvailableFilters(rockets)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        rockets = rockets,
                        filteredRockets = applyFiltersToData(
                            rockets,
                            it.searchQuery,
                            it.activeFilters
                        ),
                        availableFilters = availableFilters,
                        error = null
                    )
                }
            }
            .onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = throwable.message?.let { UiText.dynamic(it) }
                            ?: UiText.from(R.string.data_error)
                    )
                }
            }
    }

    private suspend fun refreshRockets() {
        // Reset search and filters on refresh
        _uiState.update {
            it.copy(
                searchQuery = "",
                activeFilters = FilterState() // FilterState no longer contains strings
            )
        }
        loadRockets(refresh = true)
    }

    private fun updateSearchQuery(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                // applyFiltersToData will be updated to work with FilterValue
                filteredRockets = applyFiltersToData(
                    currentState.rockets,
                    query,
                    currentState.activeFilters
                )
            )
        }
    }

    private fun updateFilters(newFilterState: FilterState) {
        _uiState.update { currentState ->
            currentState.copy(
                activeFilters = newFilterState,
                filteredRockets = applyFiltersToData(
                    currentState.rockets,
                    currentState.searchQuery,
                    newFilterState
                )
            )
        }
    }

    /**
     * Applies the search query and active filters to the list of rockets.
     */
    private fun applyFiltersToData(
        rockets: List<RocketListItemModel>,
        query: String,
        filters: FilterState
    ): List<RocketListItemModel> {
        return rockets
            .filter { rocket ->
                query.isEmpty() || listOf(
                    rocket.name,
                    rocket.firstFlight,
                    rocket.height.toString(),
                    rocket.diameter.toString(),
                    rocket.mass.toString()
                ).any { it.contains(query, ignoreCase = true) }
            }
            .filter { rocket ->
                // Filtering logic based on selected FilterValue
                filters.selectedFilters.all { (filterKey, selectedFilterValues) -> // Iterate over Map<String, Set<FilterValue>>
                    if (selectedFilterValues.isEmpty()) {
                        true // No values selected for this filter key, so it matches
                    } else {
                        when (filterKey) {
                            FilterConstants.KEY_NAME ->
                                // For KEY_NAME, we expect FilterValue.ExactMatch.
                                // Check if the rocket's value matches the value in any selected ExactMatch.
                                selectedFilterValues.filterIsInstance<FilterValue.ExactMatch>()
                                    .any { it.value.equals(rocket.name, ignoreCase = true) }

                            FilterConstants.KEY_FIRST_FLIGHT ->
                                // For KEY_FIRST_FLIGHT, we expect FilterValue.YearRange.
                                // Call YearFilter.matches with the rocket's year and the set of selected YearRange objects.
                                YearFilter.matches(
                                    rocket.firstFlight,
                                    selectedFilterValues.filterIsInstance<FilterValue.YearRange>()
                                        .toSet()
                                ) // Convert to Set

                            FilterConstants.KEY_HEIGHT ->
                                // For KEY_HEIGHT, we expect FilterValue.Range.
                                // Call RangeFilter.matches with the rocket's height and the set of selected Range objects.
                                RangeFilter.matches(
                                    rocket.height,
                                    selectedFilterValues.filterIsInstance<FilterValue.Range>()
                                        .toSet()
                                ) // Convert to Set

                            FilterConstants.KEY_DIAMETER ->
                                // For KEY_DIAMETER, we expect FilterValue.Range.
                                // Call RangeFilter.matches with the rocket's diameter and the set of selected Range objects.
                                RangeFilter.matches(
                                    rocket.diameter,
                                    selectedFilterValues.filterIsInstance<FilterValue.Range>()
                                        .toSet()
                                ) // Convert to Set

                            FilterConstants.KEY_MASS ->
                                // For KEY_MASS, we expect FilterValue.Range (for Double).
                                // Call RangeFilter.matches with the rocket's mass (as Double) and the set of selected Range objects.
                                RangeFilter.matches(
                                    rocket.mass.toDouble(),
                                    selectedFilterValues.filterIsInstance<FilterValue.Range>()
                                        .toSet()
                                ) // Convert to Set

                            else -> true // Unknown filter key, assume it matches
                        }
                    }
                }
            }
    }

    /**
     * Creates the list of available filters based on the provided rockets.
     * This function has been updated to create FilterItem with UiText and List<FilterValue>.
     */
    private fun createAvailableFilters(rockets: List<RocketListItemModel>): List<FilterItem> {
        if (rockets.isEmpty()) return emptyList()
        return listOf(
            createNameFilter(rockets),
            createFirstFlightFilter(rockets),
            createHeightFilter(rockets),
            createDiameterFilter(rockets),
            createMassFilter(rockets)
        )
    }

    /**
     * Creates the FilterItem for the "Name" filter.
     * Creates FilterValue.ExactMatch for each unique name.
     */
    private fun createNameFilter(rockets: List<RocketListItemModel>): FilterItem {
        val names = rockets.map { it.name }.distinct().sorted()
        val filterValues = names.map { name ->
            // Each name is an ExactMatch with a dynamic UiText and the name itself as the value.
            FilterValue.ExactMatch(displayName = UiText.dynamic(name), value = name)
        }
        return FilterItem(
            key = FilterConstants.KEY_NAME,
            displayName = UiText.from(R.string.filter_name), // Use UiText.from for String resource
            values = filterValues
        )
    }

    /**
     * Creates the FilterItem for the "First Flight" filter.
     * Creates FilterValue.YearRange based on calculated year ranges.
     */
    private fun createFirstFlightFilter(rockets: List<RocketListItemModel>): FilterItem {
        val dateStrings = rockets.map { it.firstFlight }
        // Use FilterUtils to get raw year range information
        val yearRangeInfo = FilterUtils.generateYearRangeInfo(dateStrings)

        // Create FilterValue.YearRange objects with UiText for display and data for filtering
        val filterValues = yearRangeInfo?.let { info ->
            listOf(
                // "Before Year X" range: displayName is UiText, endYear is the boundary
                FilterValue.YearRange(
                    displayName = UiText.from(
                        R.string.filter_before,
                        (info.min + info.step).toString()
                    ),
                    endYear = info.min + info.step // E.g., for "Before 1990" matches years <= 1990
                ),
                // "X - Y" range: displayName is UiText, startYear and endYear are the boundaries
                FilterValue.YearRange(
                    displayName = UiText.from( // Use R.string.filter_range, which should take 2 string args.
                        R.string.filter_range,
                        (info.min + info.step).toString(),
                        (info.max - info.step).toString()
                    ),
                    startYear = info.min + info.step, // E.g., for "1990 - 2000" matches >= 1990
                    endYear = info.max - info.step // and <= 2000
                ),
                // "After Year X" range: displayName is UiText, startYear is the boundary
                FilterValue.YearRange(
                    displayName = UiText.from(
                        R.string.filter_after,
                        (info.max - info.step).toString()
                    ),
                    startYear = info.max - info.step // E.g., for "After 2000" matches >= 2000
                )
            )
        } ?: emptyList() // If no data, the filter list is empty


        return FilterItem(
            key = FilterConstants.KEY_FIRST_FLIGHT,
            displayName = UiText.from(R.string.filter_first_flight), // Use UiText.from
            values = filterValues, // Use the list of FilterValue.YearRange
            extraParams = mapOf(FilterConstants.PARAM_UNIT to UiText.from(R.string.unit_year)) // Use UiText.from for extra param
        )
    }

    /**
     * Creates the FilterItem for the "Height" filter.
     * Creates FilterValue.Range based on calculated numerical ranges.
     */
    private fun createHeightFilter(rockets: List<RocketListItemModel>): FilterItem {
        val heights = rockets.map { it.height }
        // Use FilterUtils to get raw numerical range information
        val rangeInfo = FilterUtils.generateDoubleRangeInfo(heights)

        // Create FilterValue.Range objects with UiText for display and data for filtering
        val filterValues = rangeInfo?.let { info ->
            listOf(
                // "Under X" range: displayName is UiText, end is the boundary
                FilterValue.Range(
                    displayName = UiText.from(
                        R.string.filter_under,
                        "%.1f".format(info.min + info.step)
                    ), // Format the number here for display
                    end = info.min + info.step // E.g., for "Under 100.0m" matches values <= 100.0
                ),
                // "X - Y" range: displayName is UiText, start and end are the boundaries
                FilterValue.Range(
                    displayName = UiText.from( // Use R.string.filter_range
                        R.string.filter_range,
                        "%.1f".format(info.min + info.step),
                        "%.1f".format(info.max - info.step)
                    ),
                    start = info.min + info.step, // E.g., for "100.0m - 200.0m" matches >= 100.0
                    end = info.max - info.step // and <= 200.0
                ),
                // "Over X" range: displayName is UiText, start is the boundary
                FilterValue.Range(
                    displayName = UiText.from(
                        R.string.filter_over,
                        "%.1f".format(info.max - info.step)
                    ),
                    start = info.max - info.step // E.g., for "Over 200.0m" matches >= 200.0
                )
            )
        } ?: emptyList() // If no data, the filter list is empty


        return FilterItem(
            key = FilterConstants.KEY_HEIGHT,
            displayName = UiText.from(R.string.filter_height),
            values = filterValues, // Use the list of FilterValue.Range
            extraParams = mapOf(FilterConstants.PARAM_UNIT to UiText.from(R.string.unit_meters)) // Use UiText.from for extra param
        )
    }

    /**
     * Creates the FilterItem for the "Diameter" filter.
     * Creates FilterValue.Range based on calculated numerical ranges.
     */
    private fun createDiameterFilter(rockets: List<RocketListItemModel>): FilterItem {
        val diameters = rockets.map { it.diameter }
        val rangeInfo = FilterUtils.generateDoubleRangeInfo(diameters)

        val filterValues = rangeInfo?.let { info ->
            listOf(
                FilterValue.Range(
                    displayName = UiText.from(
                        R.string.filter_under,
                        "%.1f".format(info.min + info.step)
                    ),
                    end = info.min + info.step
                ),
                FilterValue.Range(
                    displayName = UiText.from(
                        R.string.filter_range,
                        "%.1f".format(info.min + info.step),
                        "%.1f".format(info.max - info.step)
                    ),
                    start = info.min + info.step,
                    end = info.max - info.step
                ),
                FilterValue.Range(
                    displayName = UiText.from(
                        R.string.filter_over,
                        "%.1f".format(info.max - info.step)
                    ),
                    start = info.max - info.step
                )
            )
        } ?: emptyList()


        return FilterItem(
            key = FilterConstants.KEY_DIAMETER,
            displayName = UiText.from(R.string.filter_diameter),
            values = filterValues,
            extraParams = mapOf(FilterConstants.PARAM_UNIT to UiText.from(R.string.unit_meters))
        )
    }

    /**
     * Creates the FilterItem for the "Mass" filter.
     * Creates FilterValue.Range based on calculated numerical ranges.
     */
    private fun createMassFilter(rockets: List<RocketListItemModel>): FilterItem {
        val masses = rockets.map { it.mass.toDouble() }
        val rangeInfo = FilterUtils.generateDoubleRangeInfo(masses)

        val filterValues = rangeInfo?.let { info ->
            listOf(
                FilterValue.Range(
                    displayName = UiText.from(
                        R.string.filter_under,
                        "%.1f".format(info.min + info.step)
                    ),
                    end = info.min + info.step
                ),
                FilterValue.Range(
                    displayName = UiText.from(
                        R.string.filter_range,
                        "%.1f".format(info.min + info.step),
                        "%.1f".format(info.max - info.step)
                    ),
                    start = info.min + info.step,
                    end = info.max - info.step
                ),
                FilterValue.Range(
                    displayName = UiText.from(
                        R.string.filter_over,
                        "%.1f".format(info.max - info.step)
                    ),
                    start = info.max - info.step
                )
            )
        } ?: emptyList()


        return FilterItem(
            key = FilterConstants.KEY_MASS,
            displayName = UiText.from(R.string.filter_mass),
            values = filterValues,
            extraParams = mapOf(FilterConstants.PARAM_UNIT to UiText.from(R.string.unit_kilograms))
        )
    }
}
