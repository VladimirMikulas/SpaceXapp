package com.vlamik.spacex.features.list


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlamik.core.domain.GetRocketsList
import com.vlamik.core.domain.models.RocketListItemModel
import com.vlamik.spacex.features.list.RocketsListViewModel.ListScreenUiState.ErrorFromAPI
import com.vlamik.spacex.features.list.RocketsListViewModel.ListScreenUiState.LoadingFromAPI
import com.vlamik.spacex.features.list.RocketsListViewModel.ListScreenUiState.UpdateSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RocketsListViewModel @Inject constructor(
    private val getRocketsList: GetRocketsList
) : ViewModel() {

    private val _state = MutableStateFlow<ListScreenUiState>(LoadingFromAPI)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            loadRockets()
        }
    }

    private suspend fun loadRockets() {
        getRocketsList()
            .onSuccess {
                _state.value = UpdateSuccess(it)
            }
            .onFailure { _state.value = ErrorFromAPI }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = LoadingFromAPI
            loadRockets()
        }
    }

    sealed interface ListScreenUiState {
        object LoadingFromAPI : ListScreenUiState
        data class UpdateSuccess(val rockets: List<RocketListItemModel>) : ListScreenUiState
        object ErrorFromAPI : ListScreenUiState

    }
}
