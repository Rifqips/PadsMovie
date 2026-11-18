package id.application.padsmovie.testing

import com.padsmovie.core.domain.model.*
import com.padsmovie.core.domain.repository.MovieRepository

open class FakeMovieRepository : MovieRepository {
    var failure: Exception? = null
    var lastGenreId: Int? = null
    var lastMovieId: Int? = null
    var lastLanguage: String? = null
    var lastPage: Int? = null
    var moviePage = Page(listOf(MovieSummary(10, "Sample", null)), 1, 3)
    var reviewPage = Page(listOf(Review("r1", "Alex", "Great story", null)), 1, 3)

    override suspend fun getGenres(language: String): List<Genre> {
        failure?.let { throw it }
        return listOf(Genre(28, "Action"))
    }

    override suspend fun discoverMovies(genreId: Int, language: String, page: Int): Page<MovieSummary> {
        lastGenreId = genreId
        lastLanguage = language
        lastPage = page
        failure?.let { throw it }
        return moviePage
    }

    override suspend fun getReviews(movieId: Int, language: String, page: Int): Page<Review> {
        lastMovieId = movieId
        lastLanguage = language
        lastPage = page
        failure?.let { throw it }
        return reviewPage
    }

    override suspend fun getMovieDetail(movieId: Int, language: String) =
        MovieDetail(movieId, "Sample", "Synopsis", "2024-01-01", 120, 8.0, null, null)

    override suspend fun getTrailer(movieId: Int, language: String): Trailer? = null
}
