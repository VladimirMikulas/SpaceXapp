package com.vlamik.spacex.features.rocketslist

import com.vlamik.core.domain.models.RocketListItemModel
import com.vlamik.spacex.common.filtering.FilterItem
import com.vlamik.spacex.common.filtering.FilterState
import com.vlamik.spacex.common.filtering.FilterValue
import com.vlamik.spacex.common.utils.UiText
import com.vlamik.spacex.navigation.NavRoutes

// This file defines the contract between the UI (View/Composable) and the ViewModel.
// It includes:
// - State: The data the UI displays.
// - Intent: User actions or events the UI sends to the ViewModel.
// - Effect: One-time events the ViewModel sends to the UI (e.g., navigation).

interface RocketsListContract {

    data class State(
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val rockets: List<RocketListItemModel> = emptyList(), // Original, unfiltered rockets
        val filteredRockets: List<RocketListItemModel> = emptyList(), // Rockets after search/filters
        val searchQuery: String = "",
        val activeFilters: FilterState = FilterState(), // Current active filters
        val availableFilters: List<FilterItem> = emptyList(), // Filter options for the UI
        val error: UiText? = null
    )

    sealed interface Intent {
        data object LoadRockets : Intent // Initial data load
        data object RefreshRockets : Intent // User initiated refresh
        data class SearchQueryChanged(val query: String) : Intent // Search text changed
        data class FilterChipToggled(
            val filterKey: String,
            val filterValue: FilterValue,
            val isSelected: Boolean
        ) : Intent

        data class RocketClicked(val rocketId: String) : Intent // Rocket item clicked
        data class NavigateTo(val route: NavRoutes) : Intent // Navigation event
        data object DrawerMenuClicked : Intent // Menu icon clicked
        data object RetryLoadRockets : Intent // Retry loading data after an error
        data object ConsumeError : Intent // Acknowledge and clear an error message
    }

    sealed interface Effect {
        data class OpenRocketDetails(val rocketId: String) : Effect
        data class NavigateToRoute(val route: NavRoutes) : Effect
        data object OpenDrawer : Effect
    }
}