package id.application.padsmovie.genres

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.padsmovie.core.designsystem.ContentState
import com.padsmovie.core.designsystem.PadsMovieTheme
import com.padsmovie.core.designsystem.R
import com.padsmovie.core.domain.model.Genre
import id.application.padsmovie.ui.ScreenScaffold
import id.application.padsmovie.ui.UiState
import id.application.padsmovie.ui.errorMessage

@Composable
internal fun GenresScreen(
    state: UiState<List<Genre>>,
    onRetry: () -> Unit,
    onGenre: (Genre) -> Unit,
    onAbout: () -> Unit,
    banner: PromoBanner? = null,
    onPromoMovie: (Int) -> Unit = {},
) {
    ScreenScaffold(
        title = stringResource(R.string.app_name),
        actions = { TextButton(onClick = onAbout) { Text(stringResource(R.string.about)) } },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(156.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, padding.calculateTopPadding() + 16.dp, 16.dp, padding.calculateBottomPadding() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(Modifier.padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.genres), style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(R.string.genres_hint), style = MaterialTheme.typography.bodyLarge)
                }
            }
            if (banner != null) {
                item(span = { GridItemSpan(maxLineSpan) }) { PromoBannerCard(banner, onPromoMovie) }
            }
            when (state) {
                UiState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) { ContentState(loading = true) }
                is UiState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                    ContentState(errorMessage(state.cause), onRetry = onRetry)
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                        ContentState(stringResource(R.string.empty_genres))
                    }
                    items(state.data, key = { it.id }) { genre -> GenreCard(genre) { onGenre(genre) } }
                }
            }
        }
    }
}

@Composable
fun GenreCard(genre: Genre, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp)) {
        Text(genre.name, Modifier.padding(20.dp), style = MaterialTheme.typography.titleMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun GenreCardPreview() {
    PadsMovieTheme { GenreCard(Genre(28, "Action"), {}) }
}
