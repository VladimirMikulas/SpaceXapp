package com.vlamik.core.domain

import com.vlamik.core.data.repositories.AppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppSettingsInteractor @Inject constructor(
    private val appRepository: AppRepository,
) {

    fun hasBeenOpened(): Flow<Boolean> {
        return appRepository.hasBeenOpened()
    }

    suspend fun appOpened() {
        appRepository.saveHasBeenOpenedPreference()
    }
}
