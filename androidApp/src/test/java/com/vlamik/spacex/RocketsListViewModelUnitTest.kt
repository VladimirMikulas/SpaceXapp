package com.vlamik.spacex

import com.vlamik.core.data.models.Dimensions
import com.vlamik.core.data.models.FirstStage
import com.vlamik.core.data.models.Mass
import com.vlamik.core.data.models.RocketDto
import com.vlamik.core.data.models.SecondStage
import com.vlamik.core.data.repositories.RocketsRepository
import com.vlamik.core.domain.usecase.ApplyRocketsFiltersUseCase
import com.vlamik.core.domain.usecase.ApplyRocketsSearchUseCase
import com.vlamik.core.domain.usecase.GetRocketsListUseCase
import com.vlamik.core.domain.usecase.filtering.FilterConstants
import com.vlamik.spacex.common.filtering.FilterValue
import com.vlamik.spacex.features.rocketslist.RocketsListContract
import com.vlamik.spacex.features.rocketslist.RocketsListViewModel
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RocketsListViewModelUnitTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRocketsRepository = mockk<RocketsRepository>()
    private val getRocketsListUseCase = GetRocketsListUseCase(mockRocketsRepository)
    private val applyRocketsSearchUseCase = ApplyRocketsSearchUseCase()
    private val applyRocketsFiltersUseCase = ApplyRocketsFiltersUseCase()

    private lateinit var viewModel: RocketsListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockRocketsRepository.getRockets(any()) } returns Result.success(emptyList())

        viewModel = RocketsListViewModel(
            getRocketsListUseCase,
            applyRocketsSearchUseCase,
            applyRocketsFiltersUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `data loads successfully and updates state`() = runTest {
        // Arrange
        val testRocketsDto = listOf(createTestRocketDto(name = "TestRocket1"))
        coEvery { mockRocketsRepository.getRockets(any()) } returns Result.success(testRocketsDto)

        // Act: Explicitly process the LoadRockets intent.
        // Even if it's called in init, explicitly doing it here makes the test clearer
        // about when the action happens.
        viewModel.processIntent(RocketsListContract.Intent.LoadRockets)

        // Allow all pending coroutines to complete, including the data fetching and StateFlow update.
        advanceUntilIdle()

        // Assert
        val currentState = viewModel.uiState.first() // Get the latest emitted state
        assertFalse(currentState.isLoading)
        assertEquals(1, currentState.rockets.size)
        assertEquals("TestRocket1", currentState.rockets.first().name)
        assertTrue(currentState.availableFilters.isNotEmpty())
    }

    @Test
    fun `error during load sets error state`() = runTest {
        // Arrange
        val errorMessage = "Failed to load data!"
        coEvery { mockRocketsRepository.getRockets(any()) } returns Result.failure(
            RuntimeException(
                errorMessage
            )
        )

        // Act: Let the initial load attempt complete (which will fail)
        advanceUntilIdle()

        // Assert
        val currentState = viewModel.uiState.first()
        assertFalse(currentState.isLoading)
        assertTrue(currentState.rockets.isEmpty())
    }

    @Test
    fun `search query filters rockets by name`() = runTest {
        // Arrange
        val rockets = listOf(
            createTestRocketDto(name = "Falcon 9"),
            createTestRocketDto(name = "Starship")
        )
        coEvery { mockRocketsRepository.getRockets(any()) } returns Result.success(rockets)
        viewModel.processIntent(RocketsListContract.Intent.LoadRockets)
        advanceUntilIdle() // Ensure rockets are loaded first

        // Act: Search for "Falcon"
        viewModel.processIntent(RocketsListContract.Intent.SearchQueryChanged("Falcon"))
        advanceUntilIdle()

        // Assert
        val currentState = viewModel.uiState.first()
        assertEquals("Falcon", currentState.searchQuery)
        assertEquals(1, currentState.filteredRockets.size)
        assertEquals("Falcon 9", currentState.filteredRockets.first().name)
    }

    @Test
    fun `filter chip toggles correctly`() = runTest {
        // Arrange
        val rockets = listOf(
            createTestRocketDto(name = "Old Rocket", firstFlight = "2000-01-01"),
            createTestRocketDto(name = "New Rocket", firstFlight = "2020-01-01")
        )
        coEvery { mockRocketsRepository.getRockets(any()) } returns Result.success(rockets)
        viewModel.processIntent(RocketsListContract.Intent.LoadRockets)
        advanceUntilIdle() // Ensure rockets and filters are loaded

        assertEquals(2, viewModel.uiState.first().filteredRockets.size) // All rockets initially

        // Find a "First Flight" filter that would typically filter older rockets
        val firstFlightFilters = viewModel.uiState.first().availableFilters
            .first { it.key == FilterConstants.KEY_FIRST_FLIGHT }.values
        // Find a filter that would select the "Old Rocket" (e.g., 'before a certain year')
        val oldRocketFilter = firstFlightFilters.filterIsInstance<FilterValue.YearRange>()
            .first { it.endYear != null && it.startYear == null } // Get the 'before' filter type


        // Act 1: Select the filter
        viewModel.processIntent(
            RocketsListContract.Intent.FilterChipToggled(
                FilterConstants.KEY_FIRST_FLIGHT,
                oldRocketFilter,
                true
            )
        )
        advanceUntilIdle()

        // Assert 1: Only "Old Rocket" remains
        val stateAfterSelect = viewModel.uiState.first()
        assertEquals(1, stateAfterSelect.filteredRockets.size)
        assertEquals("Old Rocket", stateAfterSelect.filteredRockets.first().name)
        assertTrue(
            stateAfterSelect.activeFilters.selectedFilters[FilterConstants.KEY_FIRST_FLIGHT]?.contains(
                oldRocketFilter
            ) == true
        )

        // Act 2: Deselect the filter
        viewModel.processIntent(
            RocketsListContract.Intent.FilterChipToggled(
                FilterConstants.KEY_FIRST_FLIGHT,
                oldRocketFilter,
                false
            )
        )
        advanceUntilIdle()

        // Assert 2: Both rockets are back
        val stateAfterDeselect = viewModel.uiState.first()
        assertEquals(2, stateAfterDeselect.filteredRockets.size)
        assertTrue(stateAfterDeselect.activeFilters.selectedFilters[FilterConstants.KEY_FIRST_FLIGHT]?.isEmpty() == true)
    }

    private fun createTestRocketDto(
        id: String = "rocket_id",
        name: String = "Test Rocket",
        firstFlight: String = "2006-03-24", // Default date for generic tests
        height: Double = 33.5,
        diameter: Double = 3.66,
        mass: Int = 301460,
        stages: Int = 2,
        boosters: Int = 0,
        costPerLaunch: Int = 6700000,
        successRatePct: Int = 97,
        wikipedia: String = "[https://en.wikipedia.org/wiki/Test_Rocket](https://en.wikipedia.org/wiki/Test_Rocket)",
        description: String = "A test rocket for unit tests.",
        flickrImages: List<String> = listOf("image_url_1", "image_url_2"),
        active: Boolean = true,
        country: String = "USA",
        company: String = "SpaceX",
        firstStage: FirstStage = FirstStage(
            reusable = true,
            engines = 9,
            fuelAmountTons = 385.0,
            burnTimeSEC = 162
        ),
        secondStage: SecondStage = SecondStage(
            engines = 1,
            fuelAmountTons = 90.0,
            burnTimeSEC = 397,
            reusable = false
        )
    ): RocketDto {
        return RocketDto(
            id = id,
            name = name,
            firstFlight = firstFlight,
            height = Dimensions(meters = height, feet = height * 3.28084),
            diameter = Dimensions(meters = diameter, feet = diameter * 3.28084),
            mass = Mass(kg = mass.toDouble(), lb = mass * 2.20462),
            stages = stages.toLong(),
            boosters = boosters.toLong(),
            costPerLaunch = costPerLaunch.toLong(),
            successRatePct = successRatePct.toLong(),
            wikipedia = wikipedia,
            description = description,
            flickrImages = flickrImages,
            active = active,
            country = country,
            company = company,
            firstStage = firstStage,
            secondStage = secondStage
        )
    }
}