package com.vlamik.core.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.vlamik.core.data.models.RocketDto
import com.vlamik.core.data.network.RocketsApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class RocketsRepositoryImpl
@Inject constructor(
    private val rocketsApi: RocketsApi,
    private val dataStore: DataStore<Preferences>
) : RocketsRepository {
    override suspend fun getRockets(refresh: Boolean): Result<List<RocketDto>> {
        val cachedRockets = getCachedRockets()
        if (cachedRockets.isNotEmpty() && !refresh) {
            return Result.success(cachedRockets)
        }
        val rocketsResult = rocketsApi.getRockets()
        rocketsResult.onSuccess {
            saveRocketsToCache(it)
        }
        return rocketsResult
    }

    private suspend fun getCachedRockets(): List<RocketDto> {
        val jsonString = dataStore.data
            .map { it[rocketCacheKey] }
            .firstOrNull() ?: return emptyList()

        return try {
            json.decodeFromString<List<RocketDto>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun saveRocketsToCache(rockets: List<RocketDto>) {
        dataStore.edit { preferences ->
            preferences[rocketCacheKey] = json.encodeToString(rockets)
        }
    }

    override suspend fun getRocket(id: String): Result<RocketDto> =
        rocketsApi.getRocket(id)

    companion object {
        private val rocketCacheKey = stringPreferencesKey("LAST_ROCKET_DATA")
        private val json = Json { ignoreUnknownKeys = true }
    }
}
