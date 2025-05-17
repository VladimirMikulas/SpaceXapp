package com.vlamik.spacex.features.crewlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlamik.core.domain.models.CrewListItemModel
import com.vlamik.core.domain.usecase.GetCrewListUseCase
import com.vlamik.spacex.features.crewlist.CrewListViewModel.ListScreenUiState.DataError
import com.vlamik.spacex.features.crewlist.CrewListViewModel.ListScreenUiState.LoadingData
import com.vlamik.spacex.features.crewlist.CrewListViewModel.ListScreenUiState.UpdateSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CrewListViewModel @Inject constructor(
    private val getCrewListUseCase: GetCrewListUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ListScreenUiState>(LoadingData)
    val state: StateFlow<ListScreenUiState> = _state.asStateFlow()

    init {
        loadCrew()
    }

    private fun loadCrew() {
        viewModelScope.launch {
            _state.value = LoadingData
            getCrewListUseCase()
                .onSuccess { crew ->
                    _state.value = UpdateSuccess(crew)
                }
                .onFailure {
                    _state.value = DataError
                }
        }
    }

    fun refresh() {
        loadCrew()
    }

    sealed interface ListScreenUiState {
        data object LoadingData : ListScreenUiState
        data class UpdateSuccess(
            val crew: List<CrewListItemModel>
        ) : ListScreenUiState

        data object DataError : ListScreenUiState
    }
}