package id.application.padsmovie.genres

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import java.net.URI

internal data class PromoBanner(val title: String, val imageUrl: String, val movieId: Int) {
    companion object {
        fun fromConfig(enabled: Boolean, title: String, imageUrl: String, movieId: Long): PromoBanner? {
            val cleanTitle = title.trim()
            val cleanUrl = imageUrl.trim()
            if (!enabled || cleanTitle.isEmpty() || movieId !in 1L..Int.MAX_VALUE.toLong()) return null
            val uri = runCatching { URI(cleanUrl) }.getOrNull() ?: return null
            if (!uri.scheme.equals("https", ignoreCase = true) || uri.host.isNullOrEmpty() || uri.rawUserInfo != null) return null
            return PromoBanner(cleanTitle, cleanUrl, movieId.toInt())
        }
    }
}

@Composable
internal fun rememberPromoBanner(): PromoBanner? {
    val context = LocalContext.current.applicationContext
    val banner by produceState<PromoBanner?>(null, context) {
        if (FirebaseApp.getApps(context).none { it.name == FirebaseApp.DEFAULT_APP_NAME }) return@produceState
        val config = FirebaseRemoteConfig.getInstance()
        config.ensureInitialized().addOnSuccessListener { value = config.promoBanner() }
        config.fetchAndActivate().addOnSuccessListener { value = config.promoBanner() }
    }
    return banner
}

private fun FirebaseRemoteConfig.promoBanner(): PromoBanner? =
    PromoBanner.fromConfig(
        getBoolean("banner_enabled"),
        getString("banner_title"),
        getString("banner_image_url"),
        getLong("banner_movie_id"),
    )

@Composable
internal fun PromoBannerCard(banner: PromoBanner?, onMovie: (Int) -> Unit) {
    if (banner == null) return
    Card(onClick = { onMovie(banner.movieId) }, modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = banner.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
        )
        Text(banner.title, Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
    }
}
