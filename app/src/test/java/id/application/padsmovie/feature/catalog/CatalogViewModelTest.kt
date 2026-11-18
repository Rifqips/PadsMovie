package id.application.padsmovie.feature.catalog

import id.application.padsmovie.feature.detail.DetailViewModel
import id.application.padsmovie.feature.genres.GenresViewModel
import id.application.padsmovie.ui.state.UiState
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import id.application.padsmovie.testing.FakeMovieRepository

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `genre request recovers after retry`() = runTest(dispatcher) {
        val repository = FakeMovieRepository().apply { failure = IOException("offline") }
        val model = GenresViewModel(repository, "id-ID")
        advanceUntilIdle()
        assertTrue(model.state.value is UiState.Error)
        repository.failure = null
        model.retry()
        advanceUntilIdle()
        assertEquals("Action", (model.state.value as UiState.Success).data.single().name)
    }

    @Test
    fun `trailer failure leaves movie detail available`() = runTest(dispatcher) {
        val repository = object : FakeMovieRepository() {
            override suspend fun getTrailer(movieId: Int, language: String): com.padsmovie.core.domain.model.Trailer? {
                throw IOException("video service unavailable")
            }
        }
        val model = DetailViewModel(repository, 10, "en-US")
        advanceUntilIdle()
        assertEquals(10, (model.detail.value as UiState.Success).data.id)
        assertTrue(model.trailer.value is UiState.Error)
    }
}
