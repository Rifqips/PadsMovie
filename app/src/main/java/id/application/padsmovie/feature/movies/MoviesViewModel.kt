package id.application.padsmovie.feature.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.padsmovie.core.domain.repository.MovieRepository

class MoviesViewModel(repository: MovieRepository, genreId: Int, language: String) : ViewModel() {
    val movies = Pager(PagingConfig(pageSize = 20, initialLoadSize = 20, enablePlaceholders = false)) {
        MoviesPagingSource(repository, genreId, language)
    }.flow.cachedIn(viewModelScope)
}
