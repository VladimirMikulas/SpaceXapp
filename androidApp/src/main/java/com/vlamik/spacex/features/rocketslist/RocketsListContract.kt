package com.vlamik.spacex.features.rocketslist


import com.vlamik.core.domain.models.RocketListItemModel
import com.vlamik.spacex.common.filtering.FilterItem
import com.vlamik.spacex.common.filtering.FilterState
import com.vlamik.spacex.common.utils.UiText
import com.vlamik.spacex.navigation.NavRoutes

object RocketsListContract {

    // State: Represents the UI state
    data class State(
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val rockets: List<RocketListItemModel> = emptyList(),
        val filteredRockets: List<RocketListItemModel> = emptyList(),
        val searchQuery: String = "",
        val activeFilters: FilterState = FilterState(),
        val availableFilters: List<FilterItem> = emptyList(),
        val error: UiText? = null,
    )

    // Intent: Represents user actions or UI events
    sealed interface Intent {
        data object LoadRockets : Intent
        data object RefreshRockets : Intent
        data class SearchQueryChanged(val query: String) : Intent
        data class FilterSelected(val newFilterState: FilterState) : Intent
        data class RocketClicked(val rocketId: String) : Intent
        data class NavigateTo(val route: NavRoutes) : Intent
        data object DrawerMenuClicked : Intent
        data object RetryLoadRockets : Intent
        data object ConsumeError : Intent
    }

    // Effect: Represents one-time events (navigation, showing Toast/Snackbar, opening drawer)
    sealed interface Effect {
        data class NavigateToRoute(val route: NavRoutes) : Effect
        data class OpenRocketDetails(val rocketId: String) : Effect
        data object OpenDrawer : Effect
    }
}