package id.application.padsmovie.di

import com.padsmovie.core.data.tmdb.repository.createTmdbMovieRepository
import com.padsmovie.core.domain.repository.MovieRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.application.padsmovie.BuildConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MovieCatalogModule {
    @Provides
    @Singleton
    fun provideMovieRepository(): MovieRepository =
        createTmdbMovieRepository(BuildConfig.TMDB_READ_ACCESS_TOKEN)
}
