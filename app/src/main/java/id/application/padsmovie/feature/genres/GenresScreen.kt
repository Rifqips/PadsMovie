package id.application.padsmovie.feature.genres

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.padsmovie.core.designsystem.components.ContentState
import com.padsmovie.core.designsystem.theme.PadsMovieTheme
import com.padsmovie.core.designsystem.R
import com.padsmovie.core.domain.model.Genre
import id.application.padsmovie.ui.components.ScreenScaffold
import id.application.padsmovie.ui.state.UiState
import id.application.padsmovie.ui.state.errorMessage

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
        actions = {
            IconButton(onClick = onAbout) {
                Icon(Icons.Default.Info, contentDescription = stringResource(R.string.about))
            }
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(156.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, padding.calculateTopPadding() + 16.dp, 16.dp, padding.calculateBottomPadding() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.genres),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(stringResource(R.string.genres_hint), style = MaterialTheme.typography.bodyMedium)
                    }
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
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 78.dp).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(genre.name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GenreCardPreview() {
    PadsMovieTheme { GenreCard(Genre(28, "Action"), {}) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 780)
@Composable
private fun GenresScreenPreview() {
    PadsMovieTheme {
        GenresScreen(
            state = UiState.Success(
                listOf(
                    Genre(28, "Action"),
                    Genre(16, "Animation"),
                    Genre(35, "Comedy"),
                    Genre(18, "Drama"),
                    Genre(27, "Horror"),
                    Genre(878, "Science Fiction"),
                ),
            ),
            onRetry = {},
            onGenre = {},
            onAbout = {},
        )
    }
}
