package id.application.padsmovie.feature.reviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.padsmovie.core.designsystem.components.ContentState
import com.padsmovie.core.designsystem.theme.PadsMovieTheme
import com.padsmovie.core.designsystem.R
import com.padsmovie.core.domain.model.Review
import id.application.padsmovie.ui.components.PagingFooter
import id.application.padsmovie.ui.components.ScreenScaffold
import id.application.padsmovie.ui.state.errorMessage
import java.text.NumberFormat

@Composable
fun ReviewsScreen(reviews: LazyPagingItems<Review>, onBack: () -> Unit) {
    ScreenScaffold(stringResource(R.string.reviews), onBack) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, padding.calculateTopPadding() + 16.dp, 16.dp, padding.calculateBottomPadding() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (reviews.itemCount == 0) item {
                when (val refresh = reviews.loadState.refresh) {
                    is LoadState.Loading -> ContentState(loading = true)
                    is LoadState.Error -> ContentState(errorMessage(refresh.error), onRetry = reviews::retry)
                    is LoadState.NotLoading -> ContentState(stringResource(R.string.empty_reviews))
                }
            }
            items(reviews.itemCount, key = reviews.itemKey { it.id }) { index ->
                reviews[index]?.let { ReviewCard(it) }
            }
            item { PagingFooter(reviews.loadState.append, reviews::retry) }
        }
    }
}

@Composable
fun ReviewCard(review: Review) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(review.author, style = MaterialTheme.typography.titleMedium)
            review.rating?.let { Text(stringResource(R.string.rating, NumberFormat.getNumberInstance().format(it))) }
            Text(review.content, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReviewCardPreview() {
    PadsMovieTheme { ReviewCard(Review("1", "Movie fan", "A memorable story with wonderful performances.", 8.0)) }
}
