package com.vlamik.core.data.repositories

import kotlinx.coroutines.flow.Flow

interface AppRepository {
    suspend fun saveHasBeenOpenedPreference()
    fun hasBeenOpened(): Flow<Boolean>
}