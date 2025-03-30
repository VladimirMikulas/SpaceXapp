package com.vlamik.core.data.network

import com.vlamik.core.commons.BackgroundDispatcher
import com.vlamik.core.commons.endpoints.OpenLibraryEndpoint
import com.vlamik.core.commons.loge
import com.vlamik.core.data.models.RocketDto
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class OpenLibraryService
@Inject constructor(
    private val httpClient: OpenLibraryHttpClient,
    @BackgroundDispatcher private val coroutineContext: CoroutineContext
) {
    suspend fun getRockets(): Result<List<RocketDto>> = withContext(coroutineContext) {
        return@withContext try {
            Result.success(
                httpClient().get {
                    url(path = OpenLibraryEndpoint.allRockets)
                }.body()
            )
        } catch (e: Exception) {
            loge("Failed to get Rockets", e)
            Result.failure(e)
        }
    }

    suspend fun getRocket(id: String): Result<RocketDto> = withContext(coroutineContext) {
        return@withContext try {
            Result.success(
                httpClient().get {
                    url(path = OpenLibraryEndpoint.rocket(id))
                }.body()
            )
        } catch (e: Exception) {
            loge("Failed to get Rocket", e)
            Result.failure(e)
        }
    }
}
