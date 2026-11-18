package id.application.padsmovie.ui

import androidx.compose.runtime.Composable
import androidx.paging.LoadState
import com.padsmovie.core.designsystem.ContentState

@Composable
fun PagingFooter(state: LoadState, onRetry: () -> Unit) {
    when (state) {
        is LoadState.Loading -> ContentState(loading = true)
        is LoadState.Error -> ContentState(errorMessage(state.error), onRetry = onRetry)
        is LoadState.NotLoading -> Unit
    }
}
