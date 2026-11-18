package id.application.padsmovie.feature.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.padsmovie.core.domain.repository.MovieRepository

class ReviewsViewModel(repository: MovieRepository, movieId: Int, language: String) : ViewModel() {
    val reviews = Pager(PagingConfig(pageSize = 20, initialLoadSize = 20, enablePlaceholders = false)) {
        ReviewsPagingSource(repository, movieId, language)
    }.flow.cachedIn(viewModelScope)
}
