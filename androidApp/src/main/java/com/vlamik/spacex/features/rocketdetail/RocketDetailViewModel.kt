package com.vlamik.spacex.features.rocketdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vlamik.core.domain.GetRocketDetail
import com.vlamik.core.domain.models.RocketDetailModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RocketDetailViewModel @AssistedInject constructor(
    @Assisted rocketId: String,
    getRocketDetail: GetRocketDetail,
) : ViewModel() {

    private val _updateState =
        MutableStateFlow<UiState>(UiState.LoadingData)
    val updateState = _updateState.asStateFlow()

    init {
        viewModelScope.launch {
            getRocketDetail(rocketId)
                .onSuccess { _updateState.value = UiState.Success(it) }
                .onFailure { _updateState.value = UiState.DataError }
        }
    }

    sealed interface UiState {
        object LoadingData : UiState
        data class Success(val rocket: RocketDetailModel) : UiState
        object DataError : UiState
    }

    @AssistedFactory
    interface Factory {
        fun create(rocketId: String): RocketDetailViewModel
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            factory: Factory,
            rocketId: String,
        ) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return factory.create(rocketId) as T
            }
        }
    }
}
