package com.vlamik.core.domain.models

import com.vlamik.core.data.models.CrewDto


data class CrewListItemModel(
    val name: String,
    val agency: String,
    val wikipedia: String,
    val status: String,
)

fun CrewDto.toCrewListItemModel(): CrewListItemModel = CrewListItemModel(
    name = name.orEmpty(),
    agency = agency.orEmpty(),
    wikipedia = wikipedia.orEmpty(),
    status = status.orEmpty(),
)

