package com.vlamik.core.domain.models

import com.vlamik.core.data.models.FirstStage
import com.vlamik.core.data.models.SecondStage

data class StageDetailModel(
    val reusable: Boolean,
    val engines: Int,
    val fuelAmountTons: Double,
    val burnTimeSEC: Int,
)

fun FirstStage.toStageDetailModel(): StageDetailModel = StageDetailModel(
    reusable = reusable ?: false,
    engines = engines?.toInt() ?: -1,
    fuelAmountTons = fuelAmountTons ?: -1.0,
    burnTimeSEC = burnTimeSEC?.toInt() ?: -1
)

fun SecondStage.toStageDetailModel(): StageDetailModel = StageDetailModel(
    reusable = reusable ?: false,
    engines = engines?.toInt() ?: -1,
    fuelAmountTons = fuelAmountTons ?: -1.0,
    burnTimeSEC = burnTimeSEC?.toInt() ?: -1
)
