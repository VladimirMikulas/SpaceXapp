package com.vlamik.core.data.repositories

import com.vlamik.core.data.models.CrewDto
import com.vlamik.core.data.network.SpaceXApi
import javax.inject.Inject

class CrewRepositoryImpl @Inject constructor(
    private val spaceXApi: SpaceXApi,
) : CrewRepository {
    override suspend fun getCrew(): Result<List<CrewDto>> =
        spaceXApi.getCrew()
}