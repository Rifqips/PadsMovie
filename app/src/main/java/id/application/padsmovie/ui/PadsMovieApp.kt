package id.application.padsmovie.ui

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.OutlinedButton
import id.application.padsmovie.R as AppR
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.decode.SvgDecoder
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.padsmovie.core.designsystem.R
import com.padsmovie.core.domain.repository.MovieRepository
import id.application.padsmovie.account.AccountScreen
import id.application.padsmovie.detail.DetailScreen
import id.application.padsmovie.detail.DetailViewModel
import id.application.padsmovie.genres.GenresScreen
import id.application.padsmovie.genres.GenresViewModel
import id.application.padsmovie.genres.rememberPromoBanner
import id.application.padsmovie.movies.MoviesScreen
import id.application.padsmovie.movies.MoviesViewModel
import id.application.padsmovie.reviews.ReviewsScreen
import id.application.padsmovie.reviews.ReviewsViewModel
import java.util.Locale

@Composable
fun PadsMovieApp(
    repository: MovieRepository,
    notificationMovieId: Int? = null,
    onNotificationOpened: () -> Unit = {},
    onEnableNotifications: (() -> Unit)? = null,
) {
    val navController = rememberNavController()
    val language = Locale.getDefault().toLanguageTag()
    val back: () -> Unit = { navController.popBackStack() }
    NavHost(navController = navController, startDestination = "genres") {
        composable("genres") {
            val model: GenresViewModel = viewModel(factory = viewModelFactory { initializer { GenresViewModel(repository, language) } })
            val state by model.state.collectAsStateWithLifecycle()
            val banner = rememberPromoBanner()
            GenresScreen(
                state = state,
                onRetry = model::retry,
                onGenre = { genre ->
                    navController.navigate("movies/${genre.id}?name=${Uri.encode(genre.name)}")
                },
                onAbout = { navController.navigate("about") },
                banner = banner,
                onPromoMovie = { navController.navigate("detail/$it") },
            )
        }
        composable("movies/{genreId}?name={name}", arguments = listOf(
            navArgument("genreId") { type = NavType.IntType },
            navArgument("name") { defaultValue = ""; type = NavType.StringType },
        )) { entry ->
            val genreId = requireNotNull(entry.arguments).getInt("genreId")
            val model: MoviesViewModel = viewModel(factory = viewModelFactory { initializer { MoviesViewModel(repository, genreId, language) } })
            MoviesScreen(entry.arguments?.getString("name").orEmpty(), model.movies.collectAsLazyPagingItems(), {
                navController.navigate("detail/$it")
            }, back)
        }
        composable("detail/{movieId}", arguments = listOf(navArgument("movieId") { type = NavType.IntType })) { entry ->
            val movieId = requireNotNull(entry.arguments).getInt("movieId")
            val model: DetailViewModel = viewModel(factory = viewModelFactory { initializer { DetailViewModel(repository, movieId, language) } })
            val detail by model.detail.collectAsStateWithLifecycle()
            val trailer by model.trailer.collectAsStateWithLifecycle()
            DetailScreen(detail, trailer, model::retryDetail, model::retryTrailer, {
                navController.navigate("reviews/$movieId")
            }, back)
        }
        composable("reviews/{movieId}", arguments = listOf(navArgument("movieId") { type = NavType.IntType })) { entry ->
            val movieId = requireNotNull(entry.arguments).getInt("movieId")
            val model: ReviewsViewModel = viewModel(factory = viewModelFactory { initializer { ReviewsViewModel(repository, movieId, language) } })
            ReviewsScreen(model.reviews.collectAsLazyPagingItems(), back)
        }
        composable("account") {
            AccountScreen(onBack = back)
        }
        composable("about") {
            val context = LocalContext.current
            val imageLoader = remember(context) {
                ImageLoader.Builder(context).components { add(SvgDecoder.Factory()) }.build()
            }
            ScreenScaffold(stringResource(R.string.about), back) { padding ->
                LazyColumn(
                    contentPadding = PaddingValues(24.dp, padding.calculateTopPadding() + 24.dp, 24.dp, padding.calculateBottomPadding() + 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    item { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge) }
                    item { Text(stringResource(R.string.about_description)) }
                    item {
                        AsyncImage(
                            model = "https://www.themoviedb.org/assets/2/v4/logos/v2/blue_long_2-9665a76b1ae401a510ec1e0ca40ddcb3b0cfe45f1d51b77a308fea0845885648.svg",
                            imageLoader = imageLoader,
                            contentDescription = stringResource(R.string.tmdb_logo),
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                        )
                    }
                    item { Text(stringResource(R.string.tmdb_notice)) }
                    item {
                        OutlinedButton(onClick = { navController.navigate("account") }) {
                            Text(stringResource(AppR.string.account_title))
                        }
                    }
                    if (onEnableNotifications != null) item {
                        OutlinedButton(onClick = onEnableNotifications) {
                            Text(stringResource(AppR.string.enable_movie_notifications))
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(notificationMovieId) {
        if (notificationMovieId != null) {
            navController.navigate("detail/$notificationMovieId") { launchSingleTop = true }
            onNotificationOpened()
        }
    }
}
