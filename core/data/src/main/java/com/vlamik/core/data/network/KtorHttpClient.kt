package com.vlamik.core.data.network

import com.vlamik.core.commons.ApiUrl
import com.vlamik.core.commons.logd
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * **Singleton Ktor HTTP Client Provider.**
 *
 * This class provides a pre-configured [HttpClient] instance for making API requests.
 * It's set up once and reused throughout the app to optimize resources.
 *
 * @param baseUrl The base URL for API requests (injected via `@ApiUrl`).
 * @param engine The Ktor HTTP client engine (e.g., OkHttp), allowing flexible underlying HTTP stacks.
 */
@Singleton
class KtorHttpClient @Inject constructor(
    @ApiUrl private val baseUrl: String,
    private val engine: HttpClientEngineFactory<*>,
) {

    /**
     * Lazily initialized [HttpClient] instance, configured on first access.
     *
     * **Key configurations include:**
     * - `expectSuccess = true`: Throws exceptions for non-2xx HTTP responses.
     * - **Content Negotiation (JSON):** Handles JSON serialization/deserialization.
     * - `ignoreUnknownKeys = true`: Prevents crashes for unexpected JSON fields.
     * - `isLenient = true`: Allows more relaxed JSON parsing.
     * - **Logging:** Logs all request/response details for debugging.
     * - **Default Request:** Sets common headers (e.g., `Content-Type: application/json`)
     * and the base URL (`HTTPS` protocol, injected `baseUrl`).
     */
    private val client by lazy {
        HttpClient(engine) {
            expectSuccess = true

            install(ContentNegotiation) {
                json(
                    Json {
                        allowSpecialFloatingPointValues = true
                        ignoreUnknownKeys = true
                        isLenient = true
                        useArrayPolymorphism = true
                    }
                )
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        logd("Response: $message")
                    }
                }
                level = LogLevel.ALL
            }

            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                url {
                    protocol = URLProtocol.HTTPS
                    host = baseUrl
                }
            }
        }
    }

    /**
     * **Provides the configured [HttpClient] instance.**
     * Allows consumers to simply call `KtorHttpClient()` to get the client.
     *
     * @return The ready-to-use [HttpClient] instance.
     */
    operator fun invoke(): HttpClient = client
}
