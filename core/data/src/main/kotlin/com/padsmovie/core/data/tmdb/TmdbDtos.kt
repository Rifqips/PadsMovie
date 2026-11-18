package com.padsmovie.core.data.tmdb

import com.google.gson.annotations.SerializedName

internal data class MovieDetailDto(
    val id: Int,
    val title: String,
    val overview: String?,
    @SerializedName("release_date") val releaseDate: String?,
    val runtime: Int?,
    @SerializedName("vote_average") val rating: Double,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
)

internal data class ReviewPageDto(
    val page: Int,
    @SerializedName("total_pages") val totalPages: Int,
    val results: List<ReviewDto>,
)

internal data class ReviewDto(
    val id: String,
    val author: String,
    val content: String,
    @SerializedName("author_details") val authorDetails: AuthorDetailsDto?,
)
internal data class AuthorDetailsDto(val rating: Double?)
internal data class VideoResponseDto(val results: List<VideoDto>)
internal data class VideoDto(val key: String, val name: String, val site: String, val type: String, val official: Boolean = false)


internal data class GenreResponseDto(
    val genres: List<GenreDto>,
)

internal data class GenreDto(
    val id: Int,
    val name: String,
)

internal data class MoviePageDto(
    val page: Int,
    @SerializedName("total_pages") val totalPages: Int,
    val results: List<MovieDto>,
)

internal data class MovieDto(
    val id: Int,
    val title: String,
    @SerializedName("poster_path") val posterPath: String?,
)
