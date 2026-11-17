package com.padsmovie.core.domain.repository

import com.padsmovie.core.domain.model.Genre
import com.padsmovie.core.domain.model.MovieSummary
import com.padsmovie.core.domain.model.Page

interface MovieRepository {
    suspend fun getGenres(language: String): List<Genre>

    suspend fun discoverMovies(
        genreId: Int,
        language: String,
        page: Int,
    ): Page<MovieSummary>
}
