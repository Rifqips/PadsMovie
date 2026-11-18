package id.application.padsmovie.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso.pressBack
import androidx.test.platform.app.InstrumentationRegistry
import com.padsmovie.core.designsystem.theme.PadsMovieTheme
import com.padsmovie.core.designsystem.R
import com.padsmovie.core.domain.model.*
import com.padsmovie.core.domain.repository.MovieRepository
import java.io.IOException
import org.junit.Rule
import org.junit.Test

class CatalogNavigationTest {
    @get:Rule val compose = createComposeRule()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun genreToMovieDetailAndReviewsThenBack() {
        compose.setContent { PadsMovieTheme { PadsMovieApp(CatalogRepository()) } }
        waitFor("Action")
        compose.onNodeWithText("Action").performClick()
        waitFor("Film 28")
        compose.onNodeWithText("Film 28").performClick()
        waitFor("A sample synopsis")
        compose.onNodeWithText(context.getString(R.string.reviews)).performScrollTo().performClick()
        waitFor("A thoughtful review")
        compose.onNodeWithText("A thoughtful review").assertIsDisplayed()
        pressBack()
        waitFor("A sample synopsis")
        pressBack()
        waitFor("Film 28")
        compose.onNodeWithText("Film 28").assertIsDisplayed()
    }

    @Test
    fun changingGenreUsesItsOwnMovieList() {
        compose.setContent { PadsMovieTheme { PadsMovieApp(CatalogRepository()) } }
        waitFor("Action")
        compose.onNodeWithText("Action").performClick()
        waitFor("Film 28")
        pressBack()
        waitFor("Animation")
        compose.onNodeWithText("Animation").performClick()
        waitFor("Film 16")
        compose.onNodeWithText("Film 16").assertIsDisplayed()
    }

    @Test
    fun failedGenreLoadRecoversAfterRetry() {
        val repository = CatalogRepository().apply { failGenres = true }
        compose.setContent { PadsMovieTheme { PadsMovieApp(repository) } }
        val retry = context.getString(R.string.retry)
        waitFor(retry)
        repository.failGenres = false
        compose.onNodeWithText(retry).performClick()
        waitFor("Action")
        compose.onNodeWithText("Action").assertIsDisplayed()
    }

    private fun waitFor(text: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }
}

private class CatalogRepository : MovieRepository {
    @Volatile var failGenres = false
    override suspend fun getGenres(language: String): List<Genre> {
        if (failGenres) throw IOException("offline")
        return listOf(Genre(28, "Action"), Genre(16, "Animation"))
    }
    override suspend fun discoverMovies(genreId: Int, language: String, page: Int) =
        Page(listOf(MovieSummary(genreId * 10, "Film $genreId", null)), 1, 1)
    override suspend fun getMovieDetail(movieId: Int, language: String) =
        MovieDetail(movieId, "Selected movie", "A sample synopsis", "2024-01-01", 120, 8.0, null, null)
    override suspend fun getReviews(movieId: Int, language: String, page: Int) =
        Page(listOf(Review("1", "Alex", "A thoughtful review", null)), 1, 1)
    override suspend fun getTrailer(movieId: Int, language: String): Trailer? = null
}
