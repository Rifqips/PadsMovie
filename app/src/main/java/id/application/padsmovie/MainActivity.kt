package id.application.padsmovie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.padsmovie.core.designsystem.PadsMovieTheme
import com.padsmovie.core.domain.repository.MovieRepository
import dagger.hilt.android.AndroidEntryPoint
import id.application.padsmovie.ui.PadsMovieApp
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var repository: MovieRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PadsMovieTheme { PadsMovieApp(repository) }
        }
    }
}
