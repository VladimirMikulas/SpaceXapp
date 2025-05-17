package com.vlamik.core.domain.usecase

import com.vlamik.core.data.repositories.RocketsRepository
import com.vlamik.core.domain.models.RocketListItemModel
import com.vlamik.core.domain.models.toRocketListItemModel
import javax.inject.Inject

class GetRocketsListUseCase @Inject constructor(
    private val rocketsRepository: RocketsRepository
) {
    suspend operator fun invoke(refresh: Boolean = false): Result<List<RocketListItemModel>> {
        return rocketsRepository.getRockets(refresh).map { rocketList ->
            rocketList.map { rocket -> rocket.toRocketListItemModel() }
        }
    }
}
