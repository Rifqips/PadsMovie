package id.application.padsmovie.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.padsmovie.core.designsystem.PadsMovieTheme
import com.padsmovie.core.domain.model.MovieSummary
import id.application.padsmovie.R

@Composable
fun MovieArtwork(path: String?, modifier: Modifier = Modifier) {
    val placeholder = painterResource(R.drawable.ic_movie_placeholder)
    AsyncImage(
        model = path?.let { "https://image.tmdb.org/t/p/w780$it" },
        contentDescription = null,
        placeholder = placeholder,
        error = placeholder,
        fallback = placeholder,
        contentScale = ContentScale.Crop,
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

@Composable
fun MovieCard(movie: MovieSummary, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        MovieArtwork(movie.posterPath, Modifier.fillMaxWidth().aspectRatio(2f / 3f))
        Text(movie.title, Modifier.padding(12.dp), style = MaterialTheme.typography.titleMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun MovieCardPreview() {
    PadsMovieTheme { MovieCard(MovieSummary(1, "A night at the movies", null), {}) }
}
