package id.application.padsmovie.feature.catalog

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import com.padsmovie.core.domain.model.MovieSummary
import com.padsmovie.core.domain.model.Page
import com.padsmovie.core.domain.model.Review
import id.application.padsmovie.feature.movies.MoviesPagingSource
import id.application.padsmovie.feature.reviews.ReviewsPagingSource
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import id.application.padsmovie.testing.FakeMovieRepository

class PagingSourceTest {
    private val repository = FakeMovieRepository()
    private val movies = MoviesPagingSource(repository, 28, "id-ID")
    private val reviews = ReviewsPagingSource(repository, 10, "en-US")

    @Test
    fun `movies start at page one with the selected genre and language`() = runTest {
        val result = movies.load(LoadParams.Refresh(null, 20, false)) as LoadResult.Page
        assertEquals(28, repository.lastGenreId)
        assertEquals("id-ID", repository.lastLanguage)
        assertEquals(1, repository.lastPage)
        assertEquals(repository.moviePage.items, result.data)
        assertNull(result.prevKey)
        assertEquals(2, result.nextKey)
    }

    @Test
    fun `movies append the requested page and stop at the final page`() = runTest {
        repository.moviePage = repository.moviePage.copy(page = 3)
        val result = movies.load(LoadParams.Append(3, 20, false)) as LoadResult.Page
        assertEquals(3, repository.lastPage)
        assertEquals(2, result.prevKey)
        assertNull(result.nextKey)
    }

    @Test
    fun `empty movie response stops pagination`() = runTest {
        repository.moviePage = Page(emptyList(), 1, 3)
        val result = movies.load(LoadParams.Refresh(null, 20, false)) as LoadResult.Page
        assertTrue(result.data.isEmpty())
        assertNull(result.nextKey)
    }

    @Test
    fun `discover stops before the TMDB page limit`() = runTest {
        repository.moviePage = repository.moviePage.copy(page = 500, totalPages = 900)
        val result = movies.load(LoadParams.Append(500, 20, false)) as LoadResult.Page
        assertNull(result.nextKey)
    }

    @Test
    fun `failed movie append can retry the same page`() = runTest {
        val error = IOException("offline")
        repository.failure = error
        assertSame(error, (movies.load(LoadParams.Append(2, 20, false)) as LoadResult.Error).throwable)
        repository.failure = null
        repository.moviePage = repository.moviePage.copy(page = 2)
        val result = movies.load(LoadParams.Append(2, 20, false)) as LoadResult.Page
        assertEquals(2, repository.lastPage)
        assertEquals(3, result.nextKey)
    }

    @Test
    fun `movie refresh key uses the page nearest the visible item`() {
        val page = LoadResult.Page<Int, MovieSummary>(repository.moviePage.items, 1, 3)
        assertEquals(2, movies.getRefreshKey(PagingState(listOf(page), 0, PagingConfig(20), 0)))
    }

    @Test
    fun `reviews preserve movie id locale and pagination`() = runTest {
        val result = reviews.load(LoadParams.Refresh(null, 20, false)) as LoadResult.Page
        assertEquals(10, repository.lastMovieId)
        assertEquals("en-US", repository.lastLanguage)
        assertEquals(1, repository.lastPage)
        assertNull(result.prevKey)
        assertEquals(2, result.nextKey)
    }

    @Test
    fun `reviews stop on their last page`() = runTest {
        repository.reviewPage = repository.reviewPage.copy(page = 3)
        val result = reviews.load(LoadParams.Append(3, 20, false)) as LoadResult.Page
        assertEquals(3, repository.lastPage)
        assertEquals(2, result.prevKey)
        assertNull(result.nextKey)
    }

    @Test
    fun `empty reviews stop pagination`() = runTest {
        repository.reviewPage = Page(emptyList(), 1, 0)
        val result = reviews.load(LoadParams.Refresh(null, 20, false)) as LoadResult.Page
        assertTrue(result.data.isEmpty())
        assertNull(result.nextKey)
    }

    @Test
    fun `failed review append can retry without advancing its page`() = runTest {
        repository.failure = IOException("offline")
        assertTrue(reviews.load(LoadParams.Append(2, 20, false)) is LoadResult.Error)
        repository.failure = null
        repository.reviewPage = repository.reviewPage.copy(page = 2)
        val result = reviews.load(LoadParams.Append(2, 20, false)) as LoadResult.Page
        assertEquals(2, repository.lastPage)
        assertEquals(3, result.nextKey)
    }

    @Test
    fun `review refresh key uses the visible page`() {
        val page = LoadResult.Page<Int, Review>(repository.reviewPage.items, 1, 3)
        assertEquals(2, reviews.getRefreshKey(PagingState(listOf(page), 0, PagingConfig(20), 0)))
    }

    @Test(expected = CancellationException::class)
    fun `cancellation is not exposed as a retryable error`() = runTest {
        repository.failure = CancellationException()
        movies.load(LoadParams.Refresh(null, 20, false))
        Unit
    }
}
