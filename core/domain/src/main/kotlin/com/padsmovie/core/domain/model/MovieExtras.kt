package com.padsmovie.core.domain.model

data class Review(val id: String, val author: String, val content: String, val rating: Double?)

data class Trailer(val key: String, val name: String) {
    val youtubeUrl: String get() = "https://www.youtube.com/watch?v=$key"
}
