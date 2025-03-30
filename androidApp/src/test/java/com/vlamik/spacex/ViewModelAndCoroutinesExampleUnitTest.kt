package com.vlamik.spacex

/*class ViewModelAndCoroutinesExampleUnitTest {

    @get:Rule
    val coroutinesTestRule = MockMainDispatcherTestRule()

    private val getNewsMock = mockk<GetNews>()

    private fun buildVM(): NewsListViewModel = NewsListViewModel(
        getNewsMock
    )

    @Test
    fun `Test Initial State`() = runTest(coroutinesTestRule.testDispatcher) {
        // Arrange
        val vm = buildVM()

        // Assert
        vm.state.test {
            assertThat(
                awaitItem(),
                instanceOf(LoadingFromAPI::class.java)
            )
        }
    }

    @Test
    fun `Test Refresh`() = runTest(coroutinesTestRule.testDispatcher) {
        // Arrange
        val result = Result.success(listOf(News()))
        coEvery { getNewsMock.invoke() } returns result

        val vm = buildVM()

        // Act
        vm.refresh()

        // Assert
        vm.state.test {
            assertEquals(
                awaitItem(),
                UpdateSuccess(result.getOrThrow())
            )
        }
    }

    @Test
    fun `Test Error State`() = runTest(coroutinesTestRule.testDispatcher) {
        // Arrange
        val result: Result<List<News>> = Result.failure(Exception(""))
        coEvery { getNewsMock.invoke() } returns result

        val vm = buildVM()

        // Act
        vm.refresh()

        // Assert
        vm.state.test {
            assertThat(
                awaitItem(),
                instanceOf(ErrorFromAPI::class.java)
            )
        }
    }
}*/
