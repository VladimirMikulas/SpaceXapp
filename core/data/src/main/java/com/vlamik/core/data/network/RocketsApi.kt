package com.vlamik.core.data.network

import com.vlamik.core.data.models.RocketDto

interface RocketsApi {
    suspend fun getRockets(): Result<List<RocketDto>>

    suspend fun getRocket(id: String): Result<RocketDto>
}