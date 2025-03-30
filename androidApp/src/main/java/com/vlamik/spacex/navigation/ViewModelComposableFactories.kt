@file:Suppress("MatchingDeclarationName")

package com.vlamik.spacex.navigation

import android.app.Activity
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vlamik.spacex.MainActivity.ViewModelFactoryProvider
import com.vlamik.spacex.features.details.RocketDetailViewModel
import com.vlamik.spacex.features.launch.RocketLaunchViewModel
import dagger.hilt.android.EntryPointAccessors

interface BaseViewModelFactoryProvider {
    fun getRocketDetailViewModelFactory(): RocketDetailViewModel.Factory
    fun getRocketLaunchViewModelFactory(): RocketLaunchViewModel.Factory

}

@Composable
fun rocketDetailViewModel(rocketId: String): RocketDetailViewModel = viewModel(
    factory = RocketDetailViewModel.provideFactory(
        getViewModelFactoryProvider().getRocketDetailViewModelFactory(), rocketId
    )
)

@Composable
fun rocketLaunchViewModel(sensorManager: SensorManager): RocketLaunchViewModel = viewModel(
    factory = RocketLaunchViewModel.provideFactory(
        getViewModelFactoryProvider().getRocketLaunchViewModelFactory(), sensorManager
    )
)

@Composable
private fun getViewModelFactoryProvider() = EntryPointAccessors.fromActivity(
    LocalContext.current as Activity,
    ViewModelFactoryProvider::class.java
)
