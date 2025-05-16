@file:Suppress("MatchingDeclarationName")

package com.vlamik.spacex.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vlamik.spacex.MainActivity.ViewModelFactoryProvider
import com.vlamik.spacex.features.rocketdetail.RocketDetailViewModel
import dagger.hilt.android.EntryPointAccessors

interface BaseViewModelFactoryProvider {
    fun getRocketDetailViewModelFactory(): RocketDetailViewModel.Factory
}

@Composable
fun rocketDetailViewModel(rocketId: String): RocketDetailViewModel = viewModel(
    factory = RocketDetailViewModel.provideFactory(
        getViewModelFactoryProvider().getRocketDetailViewModelFactory(), rocketId
    )
)

@Composable
private fun getViewModelFactoryProvider() = EntryPointAccessors.fromActivity(
    LocalActivity.current!!,
    ViewModelFactoryProvider::class.java
)
