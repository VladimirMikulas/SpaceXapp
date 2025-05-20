package com.vlamik.core.data.network

import com.vlamik.core.commons.endpoints.OpenLibraryEndpoint
import com.vlamik.core.commons.loge
import com.vlamik.core.commons.onFailureIgnoreCancellation
import com.vlamik.core.data.models.RocketDto
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import javax.inject.Inject

class RocketsKtorApi
@Inject constructor(
    private val httpClient: KtorHttpClient,
) : RocketsApi {
    override suspend fun getRockets(): Result<List<RocketDto>> = runCatching {
        httpClient().get {
            url(path = OpenLibraryEndpoint.allRockets)
        }.body<List<RocketDto>>()
    }.onFailureIgnoreCancellation { exception ->
        loge("Failed to get Rockets with runCatching", exception)
    }

    override suspend fun getRocket(id: String): Result<RocketDto> = runCatching {
        httpClient().get {
            url(path = OpenLibraryEndpoint.rocket(id))
        }.body<RocketDto>()
    }.onFailureIgnoreCancellation { exception ->
        loge("Failed to get Rocket with id: $id", exception)
    }

}
