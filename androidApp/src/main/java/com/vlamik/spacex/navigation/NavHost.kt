package com.vlamik.spacex.navigation

import android.content.Context
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
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
    val currentRoute = rememberCurrentRoute(navController)
    NavHost(navController = navController, startDestination = NavRoutes.RocketsList.path) {
        composable(NavRoutes.RocketsList.path) {
            RocketsListScreen(
                hiltViewModel(),
                openDetailsClicked = { id ->
                    navController.navigate(NavRoutes.RocketDetails.build(id))
                },
                navigateTo = { route ->
                    navController.navigate(route.path) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                currentRoute = currentRoute
            )
        }

        composable(NavRoutes.Crew.path) {
            /*CrewScreen(
                onBackClicked = { navController.popBackStack() }
            )*/
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

@Composable
fun rememberCurrentRoute(navController: NavHostController): NavRoutes {
    return remember(navController) {
        derivedStateOf {
            when (navController.currentDestination?.route) {
                NavRoutes.RocketsList.path -> NavRoutes.RocketsList
                NavRoutes.Crew.path -> NavRoutes.Crew
                else -> NavRoutes.RocketsList // Default fallback
            }
        }.value
    }
}