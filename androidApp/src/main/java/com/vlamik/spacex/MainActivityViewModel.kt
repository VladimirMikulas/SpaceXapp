package com.vlamik.spacex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlamik.core.commons.logd
import com.vlamik.core.domain.AppSettingsInteractor
import com.vlamik.spacex.MainActivityViewModel.UiState.Loading
import com.vlamik.spacex.MainActivityViewModel.UiState.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    appSettings: AppSettingsInteractor
) : ViewModel() {
    val uiState: StateFlow<UiState> = appSettings.hasBeenOpened().map {
        if (!it) {
            // Here you can add new logic
            // for first time opening the app it can be useful to check if theres is existing local
            // data from previous installations, or check and download heavier resources
            appSettings.appOpened()
            logd("Opening the app for the first time")
        }
        Success
    }.stateIn(
        scope = viewModelScope,
        initialValue = Loading,
        started = SharingStarted.WhileSubscribed(1_000)
    )

    sealed interface UiState {
        object Loading : UiState
        object Success : UiState
    }
}
