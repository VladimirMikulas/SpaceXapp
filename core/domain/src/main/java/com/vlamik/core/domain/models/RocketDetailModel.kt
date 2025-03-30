package com.vlamik.core.domain.models

import com.vlamik.core.data.models.RocketDto

data class RocketDetailModel(
    val name: String,
    val description: String,
    val height: Double,
    val diameter: Double,
    val mass: Double,
    val firstStage: StageDetailModel,
    val secondStage: StageDetailModel,
    val images: List<String>

)

fun RocketDto.toRocketDetailModel(): RocketDetailModel = RocketDetailModel(
    name = name.orEmpty(),
    description = description.orEmpty(),
    height = height?.meters ?: -1.0,
    diameter = diameter?.meters ?: -1.0,
    mass = mass?.kg ?: -1.0,
    firstStage = firstStage.toStageDetailModel(),
    secondStage = secondStage.toStageDetailModel(),
    images = flickrImages.orEmpty()
)
