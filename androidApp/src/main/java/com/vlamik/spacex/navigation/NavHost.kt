package com.vlamik.spacex.navigation

import android.content.Context
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vlamik.spacex.features.details.RocketDetailScreen
import com.vlamik.spacex.features.launch.RocketLaunchScreen
import com.vlamik.spacex.features.list.RocketsListScreen

@Composable
fun SpaceXNavHost(
    navController: NavHostController = rememberNavController(),
    sensorManager: SensorManager = LocalContext.current.getSystemService(Context.SENSOR_SERVICE) as SensorManager
) {
    NavHost(navController = navController, startDestination = NavRoutes.RocketsList.path) {
        composable(NavRoutes.RocketsList.path) {
            RocketsListScreen(
                hiltViewModel(),
                openDetailsClicked = { articleId ->
                    navController.navigate(NavRoutes.RocketDetails.build(articleId))
                })

        }
        composable(NavRoutes.RocketDetails.path) { backStackEntry ->
            backStackEntry.arguments?.getString(NavRoutes.DETAILS_ID_KEY)?.let {
                RocketDetailScreen(
                    rocketDetailViewModel(rocketId = it),
                    onLaunchClicked = { rocketName ->
                        navController.navigate(NavRoutes.RocketLaunch.build(rocketName))
                    }) {
                    navController.popBackStack()
                }
            }
        }
        composable(NavRoutes.RocketLaunch.path) { backStackEntry ->
            backStackEntry.arguments?.getString(NavRoutes.ROCKET_NAME_KEY)?.let {
                RocketLaunchScreen(
                    rocketName = it,
                    rocketLaunchViewModel = rocketLaunchViewModel(sensorManager = sensorManager)
                ) {
                    navController.popBackStack()
                }
            }
        }
    }
}

