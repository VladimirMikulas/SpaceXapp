package com.vlamik.spacex.di

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.vlamik.core.commons.ApiUrl
import com.vlamik.core.commons.endpoints.OpenLibraryEndpoint
import com.vlamik.core.data.repositories.AppRepository
import com.vlamik.core.data.repositories.AppRepositoryImpl
import com.vlamik.core.data.repositories.RocketsRepository
import com.vlamik.core.data.repositories.RocketsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.android.Android
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
open class DataModule {

    @VisibleForTesting
    internal open val baseUrl = OpenLibraryEndpoint.baseUrl

    @Provides
    @ApiUrl
    fun apiUrl(): String = baseUrl

    protected open fun internalHttpClientEngine(): HttpClientEngineFactory<*> = Android

    @Provides
    fun httpClientEngine(): HttpClientEngineFactory<*> = internalHttpClientEngine()

    protected open fun internalDataStore(context: Context) = context.dataStore

    @Provides
    @Singleton
    fun dataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        internalDataStore(context)

    @Provides
    @Singleton
    fun providesAppRepository(
        repo: AppRepositoryImpl
    ): AppRepository {
        return repo
    }

    @Provides
    @Singleton
    fun providesRocketsRepository(
        repo: RocketsRepositoryImpl
    ): RocketsRepository {
        return repo
    }

    companion object {
        private const val DATA_STORE = "store"
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE)
    }
}
