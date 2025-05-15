package com.vlamik.core.data.network

import com.vlamik.core.data.models.CrewDto

interface CrewApi {
    suspend fun getCrew(): Result<List<CrewDto>>
}