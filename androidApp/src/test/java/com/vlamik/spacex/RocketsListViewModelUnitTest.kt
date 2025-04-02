package com.vlamik.spacex

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vlamik.core.data.models.Dimensions
import com.vlamik.core.data.models.FirstStage
import com.vlamik.core.data.models.Mass
import com.vlamik.core.data.models.RocketDto
import com.vlamik.core.data.models.SecondStage
import com.vlamik.core.data.repositories.RocketsRepository
import com.vlamik.core.domain.GetRocketsList
import com.vlamik.core.domain.models.RocketListItemModel
import com.vlamik.spacex.features.rocketslist.RocketsListViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RocketsListViewModelUnitTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val mockRepository = mockk<RocketsRepository>()
    private val getRocketsList = GetRocketsList(mockRepository)
    private val mockContext = mockk<Context> {
        every { getString(any()) } returns "test" // Mock string resources
    }
    private lateinit var viewModel: RocketsListViewModel

    @Before
    fun setup() {
        // Configure default mock response for all getRockets calls
        coEvery { mockRepository.getRockets(any()) } returns Result.success(emptyList())
        viewModel = RocketsListViewModel(getRocketsList, mockContext)
    }

    @Test
    fun `initial state should be loading`() = runTest {
        // Create a fresh ViewModel for this test with special mock configuration
        coEvery { mockRepository.getRockets(any()) } coAnswers {
            delay(1000) // Long delay to keep state as Loading
            Result.success(emptyList())
        }

        val testViewModel = RocketsListViewModel(getRocketsList, mockContext)

        // Assert immediately after creation
        assertEquals(RocketsListViewModel.ListScreenUiState.LoadingData, testViewModel.state.value)

        // Clean up by advancing time
        advanceUntilIdle()
    }

    @Test
    fun `refresh should update state to success when data loaded`() = runTest {
        // Arrange
        val testRocket = createTestRocket()
        coEvery { mockRepository.getRockets(any()) } returns Result.success(listOf(testRocket.toRocketDto()))

        // Act
        viewModel.refresh()
        advanceUntilIdle() // Process all coroutines

        // Assert
        assertTrue(viewModel.state.value is RocketsListViewModel.ListScreenUiState.UpdateSuccess)
        assertEquals(
            1,
            (viewModel.state.value as RocketsListViewModel.ListScreenUiState.UpdateSuccess).rockets.size
        )
    }

    @Test
    fun `search should filter rockets by name`() = runTest {
        // Arrange
        val rockets = listOf(
            createTestRocket(id = "1", name = "Falcon 9"),
            createTestRocket(id = "2", name = "Starship")
        )

        // 1. Mock the repository response
        coEvery { mockRepository.getRockets(any()) } returns Result.success(rockets.map { it.toRocketDto() })

        // 2. Create fresh ViewModel for this test
        val testViewModel = RocketsListViewModel(getRocketsList, mockContext)

        // 3. Wait for initial data load
        advanceUntilIdle()

        // Verify initial state
        val initialState =
            testViewModel.state.value as RocketsListViewModel.ListScreenUiState.UpdateSuccess
        assertEquals(2, initialState.rockets.size) // Should have 2 rockets initially

        // Act - Perform search
        testViewModel.updateSearchQuery("Falcon")
        advanceUntilIdle() // Process filtering

        // Assert
        val stateAfterSearch =
            testViewModel.state.value as RocketsListViewModel.ListScreenUiState.UpdateSuccess
        assertEquals(1, stateAfterSearch.filteredRockets.size)
        assertEquals("Falcon 9", stateAfterSearch.filteredRockets[0].name)
    }

    // Coroutine test rule
    class MainCoroutineRule : TestWatcher() {
        private val testDispatcher = StandardTestDispatcher()

        override fun starting(description: Description) {
            Dispatchers.setMain(testDispatcher)
        }

        override fun finished(description: Description) {
            Dispatchers.resetMain()
        }
    }

    private fun createTestRocket(
        id: String = "1",
        name: String = "Test Rocket",
        firstFlight: String = "2020-01-01",
        height: Double = 50.0,
        diameter: Double = 3.0,
        mass: Int = 100000
    ) = RocketListItemModel(id, name, firstFlight, height, diameter, mass)

    private fun RocketListItemModel.toRocketDto() = RocketDto(
        id = id,
        name = name,
        firstFlight = firstFlight,
        height = Dimensions(height, height),
        diameter = Dimensions(diameter, diameter),
        mass = Mass(mass.toDouble(), mass.toDouble()),
        firstStage = FirstStage(),
        secondStage = SecondStage()
    )
}