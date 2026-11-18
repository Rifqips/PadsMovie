package com.padsmovie.core.data.tmdb

import com.padsmovie.core.domain.model.Genre
import com.padsmovie.core.domain.model.MovieSummary
import com.padsmovie.core.domain.model.Page
import com.padsmovie.core.domain.model.MovieDetail
import com.padsmovie.core.domain.model.Review
import com.padsmovie.core.domain.model.Trailer
import com.padsmovie.core.domain.repository.MovieCatalogAuthException
import com.padsmovie.core.domain.repository.MovieCatalogAuthException.Reason
import com.padsmovie.core.domain.repository.MovieRepository
import retrofit2.HttpException

fun createTmdbMovieRepository(readAccessToken: String): MovieRepository =
    createTmdbMovieRepository(readAccessToken, TMDB_BASE_URL)

internal fun createTmdbMovieRepository(readAccessToken: String, baseUrl: String): MovieRepository {
    val token = readAccessToken.trim()
    return TmdbMovieRepository(
        api = TmdbNetwork.createApi(token, baseUrl),
        hasReadAccessToken = token.isNotEmpty(),
    )
}

private class TmdbMovieRepository(
    private val api: TmdbApi,
    private val hasReadAccessToken: Boolean,
) : MovieRepository {
    override suspend fun getMovieDetail(movieId: Int, language: String) = request {
        val movie = api.getMovieDetail(movieId, language)
        MovieDetail(
            id = movie.id,
            title = movie.title,
            overview = movie.overview.orEmpty(),
            releaseDate = movie.releaseDate.orEmpty(),
            runtimeMinutes = movie.runtime,
            rating = movie.rating,
            posterPath = movie.posterPath,
            backdropPath = movie.backdropPath,
        )
    }

    override suspend fun getReviews(movieId: Int, language: String, page: Int) = request {
        val response = api.getReviews(movieId, language, page)
        Page(
            items = response.results.map {
                Review(id = it.id, author = it.author, content = it.content, rating = it.authorDetails?.rating)
            },
            page = response.page,
            totalPages = response.totalPages,
        )
    }

    override suspend fun getTrailer(movieId: Int, language: String) = request {
        // TMDB may only have the original trailer when localized videos are absent.
        val video = selectTrailer(api.getVideos(movieId, language).results)
            ?: if (language != "en-US") selectTrailer(api.getVideos(movieId, "en-US").results) else null
        video?.let { Trailer(it.key, it.name) }
    }

    private fun selectTrailer(videos: List<VideoDto>): VideoDto? =
        videos.filter { it.site == "YouTube" && it.type == "Trailer" && it.key.matches(Regex("[A-Za-z0-9_-]+")) }
            .maxByOrNull { it.official }

    override suspend fun getGenres(language: String): List<Genre> = request {
        api.getGenres(language).genres.map { Genre(id = it.id, name = it.name) }
    }

    override suspend fun discoverMovies(
        genreId: Int,
        language: String,
        page: Int,
    ): Page<MovieSummary> = request {
        val response = api.discoverMovies(genreId, language, page)
        Page(
            items = response.results.map {
                MovieSummary(id = it.id, title = it.title, posterPath = it.posterPath)
            },
            page = response.page,
            totalPages = response.totalPages,
        )
    }

    private suspend fun <T> request(fetch: suspend () -> T): T {
        if (!hasReadAccessToken) {
            throw MovieCatalogAuthException(Reason.MISSING_TOKEN)
        }

        return try {
            fetch()
        } catch (error: HttpException) {
            if (error.code() == 401 || error.code() == 403) {
                throw MovieCatalogAuthException(Reason.REJECTED_TOKEN, error)
            }
            throw error
        }
    }
}
