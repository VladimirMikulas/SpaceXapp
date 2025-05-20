package com.vlamik.core.data.network

import com.vlamik.core.commons.endpoints.OpenLibraryEndpoint
import com.vlamik.core.commons.loge
import com.vlamik.core.commons.onFailureIgnoreCancellation
import com.vlamik.core.data.models.CrewDto
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import javax.inject.Inject

class CrewKtorApi
@Inject constructor(
    private val httpClient: KtorHttpClient,
) : CrewApi {
    override suspend fun getCrew(): Result<List<CrewDto>> = runCatching {
        httpClient().get {
            url(path = OpenLibraryEndpoint.crew)
        }.body<List<CrewDto>>()
    }.onFailureIgnoreCancellation { exception ->
        loge("Failed to get Crew", exception)
    }
}
