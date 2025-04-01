package com.vlamik.core.data.models

import kotlinx.serialization.Serializable


@Serializable
data class CrewDto(
    val name: String? = null,
    val agency: String? = null,
    val image: String? = null,
    val wikipedia: String? = null,
    val launches: List<String>? = null,
    val status: String? = null,
    val id: String? = null
)
