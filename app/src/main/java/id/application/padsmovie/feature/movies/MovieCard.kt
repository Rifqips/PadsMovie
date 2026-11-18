package id.application.padsmovie.feature.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.padsmovie.core.designsystem.theme.PadsMovieTheme
import com.padsmovie.core.domain.model.MovieSummary
import id.application.padsmovie.R

@Composable
fun MovieArtwork(path: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_movie_placeholder),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )
        if (!path.isNullOrBlank()) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w780$path",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

@Composable
fun MovieCard(movie: MovieSummary, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        MovieArtwork(movie.posterPath, Modifier.fillMaxWidth().aspectRatio(2f / 3f))
        Text(
            movie.title,
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true, widthDp = 168)
@Composable
private fun MovieCardPreview() {
    PadsMovieTheme { MovieCard(MovieSummary(1, "A night at the movies", null), {}) }
}
