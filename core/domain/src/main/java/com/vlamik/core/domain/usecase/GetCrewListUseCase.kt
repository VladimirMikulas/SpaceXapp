package com.vlamik.core.domain.usecase

import com.vlamik.core.data.repositories.CrewRepository
import com.vlamik.core.domain.models.CrewListItemModel
import com.vlamik.core.domain.models.toCrewListItemModel
import javax.inject.Inject

class GetCrewListUseCase @Inject constructor(
    private val crewRepository: CrewRepository
) {
    suspend operator fun invoke(): Result<List<CrewListItemModel>> {
        return crewRepository.getCrew().map { crewList ->
            crewList.map { crew -> crew.toCrewListItemModel() }
        }
    }
}
