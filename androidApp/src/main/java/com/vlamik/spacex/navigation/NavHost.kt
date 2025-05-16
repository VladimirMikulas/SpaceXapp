package com.vlamik.spacex.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vlamik.spacex.features.crewlist.CrewListScreen
import com.vlamik.spacex.features.rocketdetail.RocketDetailScreen
import com.vlamik.spacex.features.rocketlaunch.RocketLaunchScreen
import com.vlamik.spacex.features.rocketslist.RocketsListScreen


@Composable
fun SpaceXNavHost(
    navController: NavHostController = rememberNavController()
) {
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
                })
        }

        composable(NavRoutes.Crew.path) {
            CrewListScreen(
                hiltViewModel(),
                navigateTo = { route ->
                    navController.navigate(route.path) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
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
                    rocketLaunchViewModel = hiltViewModel()
                ) {
                    navController.popBackStack()
                }
            }
        }
    }
}