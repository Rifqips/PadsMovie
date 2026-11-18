package com.padsmovie.core.data.tmdb.repository

import com.padsmovie.core.domain.model.Genre
import com.padsmovie.core.domain.model.MovieSummary
import com.padsmovie.core.domain.model.Page
import com.padsmovie.core.domain.repository.MovieCatalogAuthException
import com.padsmovie.core.domain.repository.MovieCatalogAuthException.Reason
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test

class TmdbMovieRepositoryTest {
    @Test
    fun `detail maps optional fields and preserves locale`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"id":10,"title":"Sample","overview":null,"release_date":"2024-01-01","runtime":null,"vote_average":8.2,"poster_path":null,"backdrop_path":"/backdrop.jpg"}"""))
            val movie = createTmdbMovieRepository("test-token", server.url("/3/").toString()).getMovieDetail(10, "id-ID")
            assertEquals("", movie.overview)
            assertEquals(null, movie.runtimeMinutes)
            assertEquals("/backdrop.jpg", movie.backdropPath)
            val request = checkNotNull(server.takeRequest(1, TimeUnit.SECONDS))
            assertEquals("/3/movie/10", request.requestUrl?.encodedPath)
            assertEquals("id-ID", request.requestUrl?.queryParameter("language"))
        }
    }

    @Test
    fun `reviews preserve content optional rating and requested page`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"page":2,"total_pages":3,"results":[{"id":"r1","author":"Alex","content":"Original review","author_details":{"rating":null}}]}"""))
            val page = createTmdbMovieRepository("test-token", server.url("/3/").toString()).getReviews(10, "en-US", 2)
            assertEquals("Original review", page.items.single().content)
            assertEquals(null, page.items.single().rating)
            assertEquals(3, page.totalPages)
            val request = checkNotNull(server.takeRequest(1, TimeUnit.SECONDS))
            assertEquals("/3/movie/10/reviews", request.requestUrl?.encodedPath)
            assertEquals("2", request.requestUrl?.queryParameter("page"))
        }
    }

    @Test
    fun `trailer falls back to English and prefers official YouTube trailers`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"results":[]}"""))
            server.enqueue(MockResponse().setBody("""{"results":[{"key":"teaser","name":"Teaser","site":"YouTube","type":"Teaser","official":true},{"key":"fan","name":"Fan trailer","site":"YouTube","type":"Trailer","official":false},{"key":"official","name":"Official trailer","site":"YouTube","type":"Trailer","official":true}]}"""))
            val trailer = createTmdbMovieRepository("test-token", server.url("/3/").toString()).getTrailer(10, "id-ID")
            assertEquals("official", trailer?.key)
            assertEquals("id-ID", server.takeRequest(1, TimeUnit.SECONDS)?.requestUrl?.queryParameter("language"))
            assertEquals("en-US", server.takeRequest(1, TimeUnit.SECONDS)?.requestUrl?.queryParameter("language"))
        }
    }

    @Test
    fun `empty English videos return no trailer without another request`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"results":[]}"""))
            assertEquals(null, createTmdbMovieRepository("test-token", server.url("/3/").toString()).getTrailer(10, "en-US"))
            assertEquals(1, server.requestCount)
        }
    }

    @Test
    fun `server error remains retryable`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(500))
            server.enqueue(MockResponse().setBody("""{"genres":[]}"""))
            val repository = createTmdbMovieRepository("test-token", server.url("/3/").toString())
            assertEquals(500, (runCatching { repository.getGenres("en-US") }.exceptionOrNull() as retrofit2.HttpException).code())
            assertEquals(emptyList<Genre>(), repository.getGenres("en-US"))
        }
    }

    @Test
    fun `genres request sends bearer header and locale`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"genres":[{"id":28,"name":"Aksi"}]}"""))
            val repository = createTmdbMovieRepository("test-token", server.url("/3/").toString())

            assertEquals(listOf(Genre(28, "Aksi")), repository.getGenres("id-ID"))

            val request = checkNotNull(server.takeRequest(1, TimeUnit.SECONDS))
            assertEquals("GET", request.method)
            assertEquals("/3/genre/movie/list", request.requestUrl?.encodedPath)
            assertEquals("id-ID", request.requestUrl?.queryParameter("language"))
            assertEquals("Bearer test-token", request.getHeader("Authorization"))
        }
    }

    @Test
    fun `discover sends genre and page and maps pagination metadata`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse().setBody(
                    """{"page":2,"total_pages":3,"results":[{"id":15,"title":"Sample","poster_path":null}]}""",
                ),
            )
            val repository = createTmdbMovieRepository("test-token", server.url("/3/").toString())

            val page = repository.discoverMovies(genreId = 28, language = "en-US", page = 2)

            assertEquals(Page(listOf(MovieSummary(15, "Sample", null)), 2, 3), page)
            val request = checkNotNull(server.takeRequest(1, TimeUnit.SECONDS))
            assertEquals("/3/discover/movie", request.requestUrl?.encodedPath)
            assertEquals("28", request.requestUrl?.queryParameter("with_genres"))
            assertEquals("2", request.requestUrl?.queryParameter("page"))
            assertEquals("en-US", request.requestUrl?.queryParameter("language"))
        }
    }

    @Test
    fun `missing token fails before making an HTTP request`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(500))
            val repository = createTmdbMovieRepository("  ", server.url("/3/").toString())

            val result = runCatching { repository.getGenres("en-US") }

            assertEquals(Reason.MISSING_TOKEN, (result.exceptionOrNull() as? MovieCatalogAuthException)?.reason)
            assertEquals(0, server.requestCount)
        }
    }

    @Test
    fun `unauthorized response becomes a rejected token error`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(401))
            val repository = createTmdbMovieRepository("test-token", server.url("/3/").toString())

            val result = runCatching { repository.getGenres("en-US") }

            assertEquals(Reason.REJECTED_TOKEN, (result.exceptionOrNull() as? MovieCatalogAuthException)?.reason)
        }
    }
}
