package com.vlamik.core.domain.usecase

import com.vlamik.core.data.repositories.RocketsRepository
import com.vlamik.core.domain.models.RocketDetailModel
import com.vlamik.core.domain.models.toRocketDetailModel
import javax.inject.Inject

class GetRocketDetailUseCase @Inject constructor(
    private val rocketsRepository: RocketsRepository
) {
    suspend operator fun invoke(id: String): Result<RocketDetailModel> {
        return rocketsRepository.getRocket(id).map { rocket ->
            rocket.toRocketDetailModel()
        }
    }
}
