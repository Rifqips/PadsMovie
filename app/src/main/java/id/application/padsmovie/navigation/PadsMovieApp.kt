package id.application.padsmovie.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import com.padsmovie.core.designsystem.R
import com.padsmovie.core.domain.repository.MovieRepository
import id.application.padsmovie.feature.account.AccountScreen
import id.application.padsmovie.feature.detail.DetailScreen
import id.application.padsmovie.feature.detail.DetailViewModel
import id.application.padsmovie.feature.genres.GenresScreen
import id.application.padsmovie.feature.genres.GenresViewModel
import id.application.padsmovie.feature.genres.rememberPromoBanner
import id.application.padsmovie.feature.movies.MoviesScreen
import id.application.padsmovie.feature.movies.MoviesViewModel
import id.application.padsmovie.feature.reviews.ReviewsScreen
import id.application.padsmovie.feature.reviews.ReviewsViewModel
import id.application.padsmovie.ui.components.ScreenScaffold
import java.util.Locale
import id.application.padsmovie.R as AppR

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
    val context = LocalContext.current
    val genresRoute = stringResource(AppR.string.route_genres)
    val accountRoute = stringResource(AppR.string.route_account)
    val aboutRoute = stringResource(AppR.string.route_about)
    val genreIdArg = stringResource(AppR.string.nav_arg_genre_id)
    val movieIdArg = stringResource(AppR.string.nav_arg_movie_id)
    val genreNameArg = stringResource(AppR.string.nav_arg_genre_name)
    val genreIdPlaceholder = stringResource(AppR.string.nav_arg_placeholder, genreIdArg)
    val movieIdPlaceholder = stringResource(AppR.string.nav_arg_placeholder, movieIdArg)
    val genreNamePlaceholder = stringResource(AppR.string.nav_arg_placeholder, genreNameArg)
    val moviesPattern =
        stringResource(AppR.string.route_movies, genreIdPlaceholder, genreNamePlaceholder)
    val detailPattern = stringResource(AppR.string.route_detail, movieIdPlaceholder)
    val reviewsPattern = stringResource(AppR.string.route_reviews, movieIdPlaceholder)

    NavHost(navController = navController, startDestination = genresRoute) {
        composable(genresRoute) {
            val model: GenresViewModel = viewModel(factory = viewModelFactory {
                initializer {
                    GenresViewModel(
                        repository,
                        language
                    )
                }
            })
            val state by model.state.collectAsStateWithLifecycle()
            val banner = rememberPromoBanner()
            GenresScreen(
                state = state,
                onRetry = model::retry,
                onGenre = { genre ->
                    navController.navigate(
                        context.getString(
                            AppR.string.route_movies,
                            genre.id.toString(),
                            Uri.encode(genre.name)
                        )
                    )
                },
                onAbout = { navController.navigate(aboutRoute) },
                banner = banner,
                onPromoMovie = {
                    navController.navigate(
                        context.getString(
                            AppR.string.route_detail,
                            it.toString()
                        )
                    )
                },
            )
        }
        composable(
            moviesPattern, arguments = listOf(
                navArgument(genreIdArg) { type = NavType.IntType },
                navArgument(genreNameArg) { defaultValue = String(); type = NavType.StringType },
            )
        ) { entry ->
            val genreId = requireNotNull(entry.arguments).getInt(genreIdArg)
            val model: MoviesViewModel = viewModel(factory = viewModelFactory {
                initializer {
                    MoviesViewModel(repository, genreId, language)
                }
            })
            MoviesScreen(
                entry.arguments?.getString(genreNameArg).orEmpty(),
                model.movies.collectAsLazyPagingItems(),
                {
                    navController.navigate(
                        context.getString(
                            AppR.string.route_detail,
                            it.toString()
                        )
                    )
                },
                back
            )
        }
        composable(
            detailPattern,
            arguments = listOf(navArgument(movieIdArg) { type = NavType.IntType })
        ) { entry ->
            val movieId = requireNotNull(entry.arguments).getInt(movieIdArg)
            val model: DetailViewModel = viewModel(factory = viewModelFactory {
                initializer {
                    DetailViewModel(
                        repository,
                        movieId,
                        language
                    )
                }
            })
            val detail by model.detail.collectAsStateWithLifecycle()
            val trailer by model.trailer.collectAsStateWithLifecycle()
            DetailScreen(detail, trailer, model::retryDetail, model::retryTrailer, {
                navController.navigate(
                    context.getString(
                        AppR.string.route_reviews,
                        movieId.toString()
                    )
                )
            }, back)
        }
        composable(
            reviewsPattern,
            arguments = listOf(navArgument(movieIdArg) { type = NavType.IntType })
        ) { entry ->
            val movieId = requireNotNull(entry.arguments).getInt(movieIdArg)
            val model: ReviewsViewModel = viewModel(factory = viewModelFactory {
                initializer {
                    ReviewsViewModel(
                        repository,
                        movieId,
                        language
                    )
                }
            })
            ReviewsScreen(model.reviews.collectAsLazyPagingItems(), back)
        }
        composable(accountRoute) {
            AccountScreen(onBack = back)
        }
        composable(aboutRoute) {
            val imageLoader = remember(context) {
                ImageLoader.Builder(context).components { add(SvgDecoder.Factory()) }.build()
            }
            ScreenScaffold(stringResource(R.string.about), back) { padding ->
                LazyColumn(
                    contentPadding = PaddingValues(
                        24.dp,
                        padding.calculateTopPadding() + 24.dp,
                        24.dp,
                        padding.calculateBottomPadding() + 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    item {
                        Text(
                            stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                    item { Text(stringResource(R.string.about_description)) }
                    item {
                        AsyncImage(
                            model = stringResource(AppR.string.tmdb_logo_url),
                            imageLoader = imageLoader,
                            contentDescription = stringResource(R.string.tmdb_logo),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                        )
                    }
                    item { Text(stringResource(R.string.tmdb_notice)) }
                    item {
                        OutlinedButton(onClick = { navController.navigate(accountRoute) }) {
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
            navController.navigate(
                context.getString(
                    AppR.string.route_detail, notificationMovieId.toString()
                )
            ) { launchSingleTop = true }
            onNotificationOpened()
        }
    }
}
