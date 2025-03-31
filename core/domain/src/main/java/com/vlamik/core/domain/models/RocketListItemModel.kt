package com.vlamik.core.domain.models

import com.vlamik.core.data.models.RocketDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter

const val datePattern = "dd.MM.yyyy"

data class RocketListItemModel(
    val id: String,
    val name: String,
    val firstFlight: String,
    val height: Double,
    val diameter: Double,
    val mass: Int,
)

fun RocketDto.toRocketListItemModel(): RocketListItemModel = RocketListItemModel(
    id = id.orEmpty(),
    name = name.orEmpty(),
    firstFlight = getFirstFlightDateFormat(firstFlight.orEmpty()),
    height = height?.meters ?: -1.0,
    diameter = diameter?.meters ?: -1.0,
    mass = mass?.kg?.toInt() ?: -1
)

fun getFirstFlightDateFormat(date: String): String {
    val formatter = DateTimeFormatter.ofPattern(datePattern)
    return LocalDate.parse(date).format(formatter)
}
