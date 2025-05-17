package com.vlamik.core.domain.usecase


import com.vlamik.core.domain.models.RocketListItemModel
import javax.inject.Inject

/**
 * UseCase to apply a text search query to a list of rockets.
 * This logic is extracted from the ViewModel/combined filter UseCase
 * to adhere to the Single Responsibility Principle.
 */
class ApplyRocketsSearchUseCase @Inject constructor() {

    /**
     * Filters a list of rockets based on a search query string.
     * The search is performed across name, firstFlight, height, diameter, and mass (as strings).
     *
     * @param rockets The list of rockets to search within.
     * @param query The search query string.
     * @return A new list containing only the rockets that match the search query.
     */
    operator fun invoke(
        rockets: List<RocketListItemModel>,
        query: String
    ): List<RocketListItemModel> {
        if (query.isEmpty()) {
            return rockets // If query is empty, return the original list
        }

        return rockets.filter { rocket ->
            // Search logic: check if the query is contained (case-insensitive)
            // in any of the relevant rocket attributes converted to strings.
            listOf(
                rocket.name,
                rocket.firstFlight,
                rocket.height.toString(),
                rocket.diameter.toString(),
                rocket.mass.toString()
            ).any { it.contains(query, ignoreCase = true) }
        }
    }
}
