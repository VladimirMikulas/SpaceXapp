package com.vlamik.core.data.repositories

import com.vlamik.core.data.models.CrewDto

interface CrewRepository {
    suspend fun getCrew(): Result<List<CrewDto>>
}