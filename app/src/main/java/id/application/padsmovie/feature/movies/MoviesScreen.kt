package id.application.padsmovie.feature.movies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.padsmovie.core.designsystem.components.ContentState
import com.padsmovie.core.designsystem.R
import com.padsmovie.core.domain.model.MovieSummary
import id.application.padsmovie.ui.components.PagingFooter
import id.application.padsmovie.ui.components.ScreenScaffold
import id.application.padsmovie.ui.state.errorMessage

@Composable
fun MoviesScreen(title: String, movies: LazyPagingItems<MovieSummary>, onMovie: (Int) -> Unit, onBack: () -> Unit) {
    ScreenScaffold(title, onBack) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(148.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, padding.calculateTopPadding() + 16.dp, 16.dp, padding.calculateBottomPadding() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (movies.itemCount == 0) item(span = { GridItemSpan(maxLineSpan) }) {
                when (val refresh = movies.loadState.refresh) {
                    is LoadState.Loading -> ContentState(loading = true)
                    is LoadState.Error -> ContentState(errorMessage(refresh.error), onRetry = movies::retry)
                    is LoadState.NotLoading -> ContentState(stringResource(R.string.empty_movies))
                }
            }
            items(movies.itemCount, key = movies.itemKey { it.id }) { index ->
                movies[index]?.let { movie -> MovieCard(movie) { onMovie(movie.id) } }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                PagingFooter(movies.loadState.append, movies::retry)
            }
        }
    }
}
