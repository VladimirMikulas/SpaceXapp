package com.vlamik.core.domain.models

import com.vlamik.core.data.models.RocketDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter

const val datePattern = "dd.MM.yyyy"

data class RocketListItemModel(
    val id: String,
    val name: String,
    val firstFlight: String
)

fun RocketDto.toRocketListItemModel(): RocketListItemModel = RocketListItemModel(
    id = id.orEmpty(),
    name = name.orEmpty(),
    firstFlight = getFirstFlightDateFormat(firstFlight.orEmpty())
)

fun getFirstFlightDateFormat(date: String): String {
    val formatter = DateTimeFormatter.ofPattern(datePattern)
    return LocalDate.parse(date).format(formatter)
}
