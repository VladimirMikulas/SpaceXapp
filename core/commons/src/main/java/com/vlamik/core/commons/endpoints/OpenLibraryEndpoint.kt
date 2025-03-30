package com.vlamik.core.commons.endpoints

object OpenLibraryEndpoint {
    val baseUrl: String
        get() = "api.spacexdata.com/v4"

    val allRockets: String
        get() = "rockets"

    fun rocket(id: String) =
        "rockets/$id"
}
