package com.vlamik.core.domain

import com.vlamik.core.data.repositories.RocketsRepository
import com.vlamik.core.domain.models.RocketListItemModel
import com.vlamik.core.domain.models.toRocketListItemModel
import javax.inject.Inject

class GetRocketsList @Inject constructor(
    private val rocketsRepository: RocketsRepository
) {
    suspend operator fun invoke(refresh: Boolean = false): Result<List<RocketListItemModel>> {
        return rocketsRepository.getRockets(refresh).map { rocketList ->
            rocketList.map { rocket -> rocket.toRocketListItemModel() }
        }
    }
}
