package id.application.padsmovie.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.padsmovie.core.domain.model.MovieDetail
import com.padsmovie.core.domain.model.Trailer
import com.padsmovie.core.domain.repository.MovieRepository
import id.application.padsmovie.ui.state.UiState
import id.application.padsmovie.ui.state.loadState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repository: MovieRepository,
    private val movieId: Int,
    private val language: String,
) : ViewModel() {
    private val _detail = MutableStateFlow<UiState<MovieDetail>>(UiState.Loading)
    val detail = _detail.asStateFlow()
    private val _trailer = MutableStateFlow<UiState<Trailer?>>(UiState.Loading)
    val trailer = _trailer.asStateFlow()
    private var detailJob: Job? = null
    private var trailerJob: Job? = null

    init {
        retryDetail()
        retryTrailer()
    }

    fun retryDetail() {
        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            _detail.value = UiState.Loading
            _detail.value = loadState { repository.getMovieDetail(movieId, language) }
        }
    }

    fun retryTrailer() {
        trailerJob?.cancel()
        trailerJob = viewModelScope.launch {
            _trailer.value = UiState.Loading
            _trailer.value = loadState { repository.getTrailer(movieId, language) }
        }
    }
}
