package com.vlamik.spacex.features.list


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
import com.vlamik.spacex.features.list.RocketsListViewModel.ListScreenUiState.DataError
import com.vlamik.spacex.features.list.RocketsListViewModel.ListScreenUiState.LoadingData
import com.vlamik.spacex.features.list.RocketsListViewModel.ListScreenUiState.UpdateSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RocketsListViewModel @Inject constructor(
    private val getRocketsList: GetRocketsList,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow<ListScreenUiState>(LoadingData)
    val state = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _activeFilters = MutableStateFlow(FilterState())
    val activeFilters = _activeFilters.asStateFlow()

    val availableFilters: StateFlow<List<FilterItem>>
        get() = _state.map { state ->
            when (state) {
                is UpdateSuccess -> listOf(
                    createNameFilter(state.rockets),
                    createFirstFlightFilter(state.rockets),
                    createHeightFilter(state.rockets),
                    createDiameterFilter(state.rockets),
                    createMassFilter(state.rockets)
                )

                else -> emptyList()
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadRockets()
    }

    private fun loadRockets(refresh: Boolean = false) {
        viewModelScope.launch {
            _state.value = LoadingData
            getRocketsList(refresh)
                .onSuccess { rockets ->
                    _state.value = UpdateSuccess(
                        rockets = rockets,
                        filteredRockets = applyFilters(rockets)
                    )
                }
                .onFailure { _state.value = DataError }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = LoadingData
            _searchQuery.value = ""
            _activeFilters.value = FilterState()

            loadRockets(refresh = true)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun updateFilters(newFilterState: FilterState) {
        _activeFilters.value = newFilterState
        applyFilters()
    }

    private fun applyFilters() {
        val current = _state.value
        if (current is UpdateSuccess) {
            _state.value = current.copy(
                filteredRockets = applyFilters(current.rockets)
            )
        }
    }

    private fun applyFilters(
        rockets: List<RocketListItemModel>,
        query: String = _searchQuery.value,
        filters: FilterState = _activeFilters.value
    ): List<RocketListItemModel> {

        val filtered = rockets
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
                    // If no values are selected for this filter, ignore it (return true)
                    if (selectedValues.isEmpty()) {
                        true
                    } else {
                        val result = when (filterKey) {
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
                        result
                    }
                }
            }
        return filtered
    }

    // Filter creation functions
    private fun createNameFilter(rockets: List<RocketListItemModel>): FilterItem {
        return FilterItem(
            key = FilterConstants.KEY_NAME,
            displayName = context.getString(R.string.filter_name),
            values = rockets.map { it.name }
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

    // State and data classes
    sealed interface ListScreenUiState {
        object LoadingData : ListScreenUiState
        data class UpdateSuccess(
            val rockets: List<RocketListItemModel>,
            val filteredRockets: List<RocketListItemModel> = rockets
        ) : ListScreenUiState

        object DataError : ListScreenUiState
    }
}
