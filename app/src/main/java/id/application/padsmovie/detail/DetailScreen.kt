package id.application.padsmovie.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.padsmovie.core.designsystem.ContentState
import com.padsmovie.core.designsystem.R
import com.padsmovie.core.domain.model.MovieDetail
import com.padsmovie.core.domain.model.Trailer
import id.application.padsmovie.movies.MovieArtwork
import id.application.padsmovie.ui.ScreenScaffold
import id.application.padsmovie.ui.UiState
import id.application.padsmovie.ui.errorMessage
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun DetailScreen(
    detail: UiState<MovieDetail>,
    trailer: UiState<Trailer?>,
    onRetryDetail: () -> Unit,
    onRetryTrailer: () -> Unit,
    onReviews: () -> Unit,
    onBack: () -> Unit,
) {
    var showTrailer by rememberSaveable { mutableStateOf(false) }
    ScreenScaffold(stringResource(R.string.detail), onBack) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, padding.calculateTopPadding() + 16.dp, 16.dp, padding.calculateBottomPadding() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (detail) {
                UiState.Loading -> item { ContentState(loading = true) }
                is UiState.Error -> item { ContentState(errorMessage(detail.cause), onRetry = onRetryDetail) }
                is UiState.Success -> {
                    val movie = detail.data
                    item { MovieArtwork(movie.backdropPath ?: movie.posterPath, Modifier.fillMaxWidth().aspectRatio(16f / 9f)) }
                    item { Text(movie.title, style = MaterialTheme.typography.headlineMedium) }
                    if (movie.releaseDate.isNotBlank()) item {
                        val date = runCatching {
                            LocalDate.parse(movie.releaseDate).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                        }.getOrDefault(movie.releaseDate)
                        Text(stringResource(R.string.release_date, date))
                    }
                    movie.runtimeMinutes?.takeIf { it > 0 }?.let { minutes ->
                        item { Text(stringResource(R.string.runtime, minutes)) }
                    }
                    item { Text(stringResource(R.string.rating, NumberFormat.getNumberInstance().apply { maximumFractionDigits = 1 }.format(movie.rating))) }
                    item { Text(movie.overview.ifBlank { stringResource(R.string.empty_overview) }, style = MaterialTheme.typography.bodyLarge) }
                    item { OutlinedButton(onClick = onReviews, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.reviews)) } }
                    item {
                        when (trailer) {
                            UiState.Loading -> ContentState(loading = true)
                            is UiState.Error -> ContentState(errorMessage(trailer.cause), onRetry = onRetryTrailer)
                            is UiState.Success -> if (trailer.data == null) {
                                ContentState(stringResource(R.string.empty_trailer))
                            } else {
                                Button(onClick = { showTrailer = true }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.trailer)) }
                            }
                        }
                    }
                }
            }
        }
    }
    val selected = (trailer as? UiState.Success)?.data
    if (showTrailer && selected != null) TrailerDialog(selected) { showTrailer = false }
}
