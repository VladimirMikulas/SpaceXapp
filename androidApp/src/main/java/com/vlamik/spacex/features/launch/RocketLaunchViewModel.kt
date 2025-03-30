package com.vlamik.spacex.features.launch

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RocketLaunchViewModel @AssistedInject constructor(
    @Assisted private val sensorManager: SensorManager
) : ViewModel() {
    private val _launchState = MutableStateFlow<LaunchRocketState>(LaunchRocketState.Ready)
    val launchState: StateFlow<LaunchRocketState> = _launchState

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val rotationX = event.values[1]
            if (rotationX > 5 && _launchState.value is LaunchRocketState.Ready) {
                _launchState.value = LaunchRocketState.Launching
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    init {
        sensorManager.registerListener(
            sensorEventListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(sensorEventListener)
    }

    sealed interface LaunchRocketState {
        data object Ready : LaunchRocketState
        data object Launching : LaunchRocketState
    }

    @AssistedFactory
    interface Factory {
        fun create(sensorManager: SensorManager): RocketLaunchViewModel
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            factory: Factory,
            sensorManager: SensorManager,
        ) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return factory.create(sensorManager) as T
            }
        }
    }
}
