package com.vlamik.core.data.repositories

import com.vlamik.core.data.models.RocketDto

interface RocketsRepository {
    suspend fun getRockets(refresh: Boolean): Result<List<RocketDto>>
    suspend fun getRocket(id: String): Result<RocketDto>
}