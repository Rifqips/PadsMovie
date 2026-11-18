package com.padsmovie.core.data.tmdb.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.padsmovie.core.data.tmdb.model.GenreResponseDto
import com.padsmovie.core.data.tmdb.model.MovieDetailDto
import com.padsmovie.core.data.tmdb.model.MoviePageDto
import com.padsmovie.core.data.tmdb.model.ReviewPageDto
import com.padsmovie.core.data.tmdb.model.VideoResponseDto

internal interface TmdbApi {
    @GET("movie/{movieId}")
    suspend fun getMovieDetail(@Path("movieId") movieId: Int, @Query("language") language: String): MovieDetailDto

    @GET("movie/{movieId}/reviews")
    suspend fun getReviews(@Path("movieId") movieId: Int, @Query("language") language: String, @Query("page") page: Int): ReviewPageDto

    @GET("movie/{movieId}/videos")
    suspend fun getVideos(@Path("movieId") movieId: Int, @Query("language") language: String): VideoResponseDto

    @GET("genre/movie/list")
    suspend fun getGenres(@Query("language") language: String): GenreResponseDto

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("with_genres") genreId: Int,
        @Query("language") language: String,
        @Query("page") page: Int,
    ): MoviePageDto
}
