package com.vlamik.spacex.features.rocketslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlamik.core.domain.models.RocketListItemModel
import com.vlamik.core.domain.usecase.ApplyRocketsFiltersUseCase
import com.vlamik.core.domain.usecase.ApplyRocketsSearchUseCase
import com.vlamik.core.domain.usecase.GetRocketsListUseCase
import com.vlamik.core.domain.usecase.filtering.DomainFilterValue
import com.vlamik.core.domain.usecase.filtering.FilterConstants
import com.vlamik.core.domain.usecase.filtering.FilterUtils
import com.vlamik.spacex.R
import com.vlamik.spacex.common.filtering.FilterItem
import com.vlamik.spacex.common.filtering.FilterState
import com.vlamik.spacex.common.filtering.FilterValue
import com.vlamik.spacex.common.utils.UiText
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
    private val getRocketsListUseCase: GetRocketsListUseCase, // UseCase to get the original data
    private val applyRocketsSearch: ApplyRocketsSearchUseCase, // Inject the new search UseCase
    private val applyRocketsFilters: ApplyRocketsFiltersUseCase // Inject the filter UseCase
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

        getRocketsListUseCase(refresh)
            .onSuccess { rockets ->
                val availableFilters =
                    createAvailableFilters(rockets) // Creates presentation FilterItems
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        rockets = rockets,
                        // Apply search first, then filters to the search results
                        filteredRockets = applySearchAndFilters(
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
                activeFilters = FilterState() // Reset presentation FilterState
            )
        }
        loadRockets(refresh = true)
    }

    private fun updateSearchQuery(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                // Apply search first, then filters to the search results
                filteredRockets = applySearchAndFilters(
                    currentState.rockets,
                    query,
                    currentState.activeFilters // Pass presentation FilterState
                )
            )
        }
    }

    private fun updateFilters(newFilterState: FilterState) {
        _uiState.update { currentState ->
            currentState.copy(
                activeFilters = newFilterState, // New presentation FilterState
                // Apply search first (to original data), then filters to the search results
                filteredRockets = applySearchAndFilters(
                    currentState.rockets,
                    currentState.searchQuery,
                    newFilterState
                )
            )
        }
    }

    /**
     * Applies both search and filters using the domain UseCases.
     * This function maps presentation FilterValue to domain DomainFilterValue before calling the filter UseCase.
     */
    private fun applySearchAndFilters(
        rockets: List<RocketListItemModel>,
        query: String,
        filters: FilterState
    ): List<RocketListItemModel> {
        val searchedRockets = applyRocketsSearch(rockets, query)

        val selectedDomainFilters = filters.selectedFilters.mapValues { (_, selectedFilterValues) ->
            selectedFilterValues.map { presentationFilterValue ->
                when (presentationFilterValue) {
                    is FilterValue.ExactMatch -> DomainFilterValue.ExactMatch(
                        presentationFilterValue.value
                    )

                    is FilterValue.Range -> DomainFilterValue.Range(
                        presentationFilterValue.start,
                        presentationFilterValue.end
                    )

                    is FilterValue.YearRange -> DomainFilterValue.YearRange(
                        presentationFilterValue.startYear,
                        presentationFilterValue.endYear
                    )
                }
            }.toSet()
        }

        // Apply filters using the domain UseCase with domain filter values
        return applyRocketsFilters(searchedRockets, selectedDomainFilters)
    }


    /**
     * Creates the list of available filters for the UI based on the provided rockets.
     * This function remains in the ViewModel as it creates presentation-specific FilterItem objects
     * which include UiText.
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
     * Creates the FilterItem for the "Name" filter (presentation model).
     * Creates presentation FilterValue.ExactMatch with UiText.
     */
    private fun createNameFilter(rockets: List<RocketListItemModel>): FilterItem {
        val names = rockets.map { it.name }.distinct().sorted()
        val filterValues = names.map { name ->
            // Create presentation FilterValue.ExactMatch with UiText
            FilterValue.ExactMatch(displayName = UiText.dynamic(name), value = name)
        }
        return FilterItem(
            key = FilterConstants.KEY_NAME,
            displayName = UiText.from(R.string.filter_name), // Use UiText.from for String resource
            values = filterValues // List of presentation FilterValue
        )
    }

    /**
     * Creates the FilterItem for the "First Flight" filter (presentation model).
     * Creates presentation FilterValue.YearRange with UiText based on domain range info.
     */
    private fun createFirstFlightFilter(rockets: List<RocketListItemModel>): FilterItem {
        val dateStrings = rockets.map { it.firstFlight }
        // Use FilterUtils (from domain) to get raw year range information
        val yearRangeInfo = FilterUtils.generateYearRangeInfo(dateStrings)

        // Create presentation FilterValue.YearRange objects with UiText for display
        val filterValues = yearRangeInfo?.let { info ->
            listOf(
                // "Before Year X" range: displayName is UiText, endYear is the boundary
                FilterValue.YearRange(
                    displayName = UiText.from(
                        R.string.filter_before,
                        (info.min + info.step).toString()
                    ),
                    endYear = info.min + info.step // Data for filtering (matches domain model structure)
                ),
                // "X - Y" range: displayName is UiText, startYear and endYear are the boundaries
                FilterValue.YearRange(
                    displayName = UiText.from( // Use R.string.filter_range, which should take 2 string args.
                        R.string.filter_range,
                        (info.min + info.step).toString(),
                        (info.max - info.step).toString()
                    ),
                    startYear = info.min + info.step, // Data for filtering
                    endYear = info.max - info.step // Data for filtering
                ),
                // "After Year X" range: displayName is UiText, startYear is the boundary
                FilterValue.YearRange(
                    displayName = UiText.from(
                        R.string.filter_after,
                        (info.max - info.step).toString()
                    ),
                    startYear = info.max - info.step // Data for filtering
                )
            )
        } ?: emptyList() // If no data, the filter list is empty


        return FilterItem(
            key = FilterConstants.KEY_FIRST_FLIGHT,
            displayName = UiText.from(R.string.filter_first_flight), // Use UiText.from
            values = filterValues, // List of presentation FilterValue
            extraParams = mapOf(FilterConstants.PARAM_UNIT to UiText.from(R.string.unit_year)) // Use UiText.from for extra param
        )
    }

    /**
     * Creates the FilterItem for the "Height" filter (presentation model).
     * Creates presentation FilterValue.Range with UiText based on domain range info.
     */
    private fun createHeightFilter(rockets: List<RocketListItemModel>): FilterItem {
        val heights = rockets.map { it.height }
        val rangeInfo = FilterUtils.generateDoubleRangeInfo(heights)

        // Create presentation FilterValue.Range objects with UiText for display
        val filterValues = rangeInfo?.let { info ->
            listOf(
                // "Under X" range: displayName is UiText, end is the boundary
                FilterValue.Range(
                    displayName = UiText.from(
                        R.string.filter_under,
                        "%.1f".format(info.min + info.step)
                    ), // Format the number here for display
                    end = info.min + info.step // Data for filtering
                ),
                // "X - Y" range: displayName is UiText, start and end are the boundaries
                FilterValue.Range(
                    displayName = UiText.from( // Use R.string.filter_range
                        R.string.filter_range,
                        "%.1f".format(info.min + info.step),
                        "%.1f".format(info.max - info.step)
                    ),
                    start = info.min + info.step, // Data for filtering
                    end = info.max - info.step // Data for filtering
                ),
                // "Over X" range: displayName is UiText, start is the boundary
                FilterValue.Range(
                    displayName = UiText.from(
                        R.string.filter_over,
                        "%.1f".format(info.max - info.step)
                    ),
                    start = info.max - info.step // Data for filtering
                )
            )
        } ?: emptyList() // If no data, the filter list is empty


        return FilterItem(
            key = FilterConstants.KEY_HEIGHT,
            displayName = UiText.from(R.string.filter_height),
            values = filterValues, // List of presentation FilterValue
            extraParams = mapOf(FilterConstants.PARAM_UNIT to UiText.from(R.string.unit_meters)) // Use UiText.from for extra param
        )
    }

    /**
     * Creates the FilterItem for the "Diameter" filter (presentation model).
     * Creates presentation FilterValue.Range with UiText based on domain range info.
     */
    private fun createDiameterFilter(rockets: List<RocketListItemModel>): FilterItem {
        val diameters = rockets.map { it.diameter }
        val rangeInfo = FilterUtils.generateDoubleRangeInfo(diameters)

        // Create presentation FilterValue.Range objects with UiText for display
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
     * Creates the FilterItem for the "Mass" filter (presentation model).
     * Creates presentation FilterValue.Range with UiText based on domain range info.
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
