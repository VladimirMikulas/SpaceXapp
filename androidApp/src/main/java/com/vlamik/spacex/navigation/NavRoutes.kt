package com.vlamik.spacex.navigation

sealed class NavRoutes(internal open val path: String) {

    // Drawer items
    data object RocketsList : NavRoutes("rockets_list/")
    data object Crew : NavRoutes("crew/")

    // Non-drawer items
    data object RocketDetails : NavRoutes("rocket_details/{$DETAILS_ID_KEY}") {
        fun build(id: String): String =
            path.replace("{$DETAILS_ID_KEY}", id)
    }

    data object RocketLaunch : NavRoutes("rocket_launch/{$ROCKET_NAME_KEY}") {
        fun build(name: String): String =
            path.replace("{$ROCKET_NAME_KEY}", name)
    }


    companion object {
        const val DETAILS_ID_KEY: String = "id"
        const val ROCKET_NAME_KEY: String = "rocket_name"

        // List of drawer route items
        val drawerItems: List<NavRoutes> by lazy {
            listOf(RocketsList, Crew)
        }
    }
}
