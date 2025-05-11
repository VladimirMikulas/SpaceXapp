package com.vlamik.spacex.features.rocketslist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlamik.core.domain.GetRocketsList
import com.vlamik.core.domain.models.RocketListItemModel
import com.vlamik.spacex.R
import com.vlamik.spacex.component.appbars.models.FilterState
import com.vlamik.spacex.core.filtering.FilterConstants
import com.vlamik.spacex.core.filtering.FilterItem
import com.vlamik.spacex.core.filtering.FilterUtils
import com.vlamik.spacex.core.filtering.RangeFilter
import com.vlamik.spacex.core.filtering.YearFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context
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
                isRefreshing = refresh, // Shows pull-to-refresh indicator
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
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = throwable.message ?: context.getString(R.string.data_error)
                    )
                }
            }
    }

    private suspend fun refreshRockets() {
        // Reset search and filters on refresh
        _uiState.update {
            it.copy(
                searchQuery = "",
                activeFilters = FilterState()
            )
        }
        loadRockets(refresh = true)
    }

    private fun updateSearchQuery(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
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
                filters.selectedFilters.all { (filterKey, selectedValues) ->
                    if (selectedValues.isEmpty()) {
                        true
                    } else {
                        when (filterKey) {
                            FilterConstants.KEY_NAME ->
                                selectedValues.any { it.equals(rocket.name, ignoreCase = true) }
                            FilterConstants.KEY_FIRST_FLIGHT ->
                                YearFilter.matches(rocket.firstFlight, selectedValues, context)
                            FilterConstants.KEY_HEIGHT ->
                                RangeFilter.matches(rocket.height, selectedValues, context)
                            FilterConstants.KEY_DIAMETER ->
                                RangeFilter.matches(rocket.diameter, selectedValues, context)
                            FilterConstants.KEY_MASS ->
                                RangeFilter.matches(rocket.mass.toDouble(), selectedValues, context)
                            else -> true
                        }
                    }
                }
            }
    }

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

    // Filter creation functions (remain the same, just called differently)
    private fun createNameFilter(rockets: List<RocketListItemModel>): FilterItem {
        return FilterItem(
            key = FilterConstants.KEY_NAME,
            displayName = context.getString(R.string.filter_name),
            values = rockets.map { it.name }.distinct().sorted()
        )
    }

    private fun createFirstFlightFilter(rockets: List<RocketListItemModel>): FilterItem {
        val dateStrings = rockets.map { it.firstFlight }
        return FilterItem(
            key = FilterConstants.KEY_FIRST_FLIGHT,
            displayName = context.getString(R.string.filter_first_flight),
            values = FilterUtils.generateYearRanges(dateStrings, context),
            extraParams = mapOf(FilterConstants.PARAM_UNIT to context.getString(R.string.unit_year))
        )
    }

    private fun createHeightFilter(rockets: List<RocketListItemModel>): FilterItem {
        val heights = rockets.map { it.height }
        return FilterItem(
            key = FilterConstants.KEY_HEIGHT,
            displayName = context.getString(R.string.filter_height),
            values = FilterUtils.generateRanges(
                heights,
                context,
                context.getString(R.string.unit_meters)
            ),
            extraParams = mapOf(FilterConstants.PARAM_UNIT to context.getString(R.string.unit_meters))
        )
    }

    private fun createDiameterFilter(rockets: List<RocketListItemModel>): FilterItem {
        val diameters = rockets.map { it.diameter }
        return FilterItem(
            key = FilterConstants.KEY_DIAMETER,
            displayName = context.getString(R.string.filter_diameter),
            values = FilterUtils.generateRanges(
                diameters,
                context,
                context.getString(R.string.unit_meters)
            ),
            extraParams = mapOf(FilterConstants.PARAM_UNIT to context.getString(R.string.unit_meters))
        )
    }

    private fun createMassFilter(rockets: List<RocketListItemModel>): FilterItem {
        val masses = rockets.map { it.mass.toDouble() }
        return FilterItem(
            key = FilterConstants.KEY_MASS,
            displayName = context.getString(R.string.filter_mass),
            values = FilterUtils.generateRanges(
                masses,
                context,
                context.getString(R.string.unit_kilograms)
            ),
            extraParams = mapOf(FilterConstants.PARAM_UNIT to context.getString(R.string.unit_kilograms))
        )
    }
}