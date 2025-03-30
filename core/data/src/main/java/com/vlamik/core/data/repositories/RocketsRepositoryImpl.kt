package com.vlamik.core.data.repositories

import com.vlamik.core.data.models.RocketDto
import com.vlamik.core.data.network.OpenLibraryService
import javax.inject.Inject

class RocketsRepositoryImpl
@Inject constructor(
    private val openLibraryService: OpenLibraryService
) : RocketsRepository {
    override suspend fun getRockets(): Result<List<RocketDto>> =
        openLibraryService.getRockets()

    override suspend fun getRocket(id: String): Result<RocketDto> =
        openLibraryService.getRocket(id)

}
