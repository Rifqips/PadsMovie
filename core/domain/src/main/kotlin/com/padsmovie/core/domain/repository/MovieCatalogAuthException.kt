package com.padsmovie.core.domain.repository

class MovieCatalogAuthException(
    val reason: Reason,
    cause: Throwable? = null,
) : Exception(reason.name, cause) {
    enum class Reason {
        MISSING_TOKEN,
        REJECTED_TOKEN,
    }
}
