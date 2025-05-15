package com.vlamik.core.data.repositories

import com.vlamik.core.data.models.CrewDto
import com.vlamik.core.data.network.CrewApi
import javax.inject.Inject

class CrewRepositoryImpl @Inject constructor(
    private val crewApi: CrewApi,
) : CrewRepository {
    override suspend fun getCrew(): Result<List<CrewDto>> =
        crewApi.getCrew()
}