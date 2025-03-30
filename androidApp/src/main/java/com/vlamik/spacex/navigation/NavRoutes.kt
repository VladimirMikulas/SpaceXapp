package com.vlamik.spacex.navigation

sealed class NavRoutes(internal open val path: String) {

    object RocketsList : NavRoutes("rockets_list/")
    object RocketDetails : NavRoutes("rocket_details/{$DETAILS_ID_KEY}") {
        fun build(id: String): String =
            path.replace("{$DETAILS_ID_KEY}", id)
    }

    object RocketLaunch : NavRoutes("rocket_launch/{$ROCKET_NAME_KEY}") {
        fun build(name: String): String =
            path.replace("{$ROCKET_NAME_KEY}", name)
    }

    companion object {
        const val DETAILS_ID_KEY: String = "id"
        const val ROCKET_NAME_KEY: String = "rocket_name"

    }
}
