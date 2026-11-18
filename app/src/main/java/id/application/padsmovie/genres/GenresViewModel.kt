package id.application.padsmovie.genres

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.padsmovie.core.domain.model.Genre
import com.padsmovie.core.domain.repository.MovieRepository
import id.application.padsmovie.ui.UiState
import id.application.padsmovie.ui.loadState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GenresViewModel(private val repository: MovieRepository, private val language: String) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<Genre>>>(UiState.Loading)
    val state = _state.asStateFlow()
    private var loadJob: Job? = null

    init { retry() }

    fun retry() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = loadState { repository.getGenres(language) }
        }
    }
}
