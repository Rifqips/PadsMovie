package com.padsmovie.core.domain.repository

import com.padsmovie.core.domain.model.Genre
import com.padsmovie.core.domain.model.MovieSummary
import com.padsmovie.core.domain.model.Page
import com.padsmovie.core.domain.model.MovieDetail
import com.padsmovie.core.domain.model.Review
import com.padsmovie.core.domain.model.Trailer

interface MovieRepository {
    suspend fun getMovieDetail(movieId: Int, language: String): MovieDetail
    suspend fun getReviews(movieId: Int, language: String, page: Int): Page<Review>
    suspend fun getTrailer(movieId: Int, language: String): Trailer?

    suspend fun getGenres(language: String): List<Genre>

    suspend fun discoverMovies(
        genreId: Int,
        language: String,
        page: Int,
    ): Page<MovieSummary>
}
