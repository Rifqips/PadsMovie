package com.padsmovie.core.domain.model

data class MovieDetail(
    val id: Int,
    val title: String,
    val overview: String,
    val releaseDate: String,
    val runtimeMinutes: Int?,
    val rating: Double,
    val posterPath: String?,
    val backdropPath: String?,
)
