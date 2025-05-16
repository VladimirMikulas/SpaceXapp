package com.vlamik.spacex.features.rocketlaunch

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class RocketLaunchViewModel @Inject constructor(
) : ViewModel() {
    // State of the rocket launch
    private val _launchState = MutableStateFlow<LaunchRocketState>(LaunchRocketState.Ready)
    val launchState: StateFlow<LaunchRocketState> = _launchState.asStateFlow()

    /**
     * Sets the current state of the rocket launch.
     * This function will be called from the UI based on sensor events.
     * @param newState The new launch state.
     */
    fun setLaunchState(newState: LaunchRocketState) {
        _launchState.value = newState
    }

    /**
     * Sealed interface representing the different UI states during rocket launch.
     * Moved here for better organization or kept in the contract if it exists.
     */
    sealed interface LaunchRocketState {
        data object Ready : LaunchRocketState
        data object Launching : LaunchRocketState
    }
}

