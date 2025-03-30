package com.vlamik.core.domain

import com.vlamik.core.data.repositories.RocketsRepository
import com.vlamik.core.domain.models.RocketDetailModel
import com.vlamik.core.domain.models.toRocketDetailModel
import javax.inject.Inject

class GetRocketDetail @Inject constructor(
    private val rocketsRepository: RocketsRepository
) {
    suspend operator fun invoke(id: String): Result<RocketDetailModel> {
        return rocketsRepository.getRocket(id).map { rocket ->
            rocket.toRocketDetailModel()
        }
    }
}
