package id.application.padsmovie.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.padsmovie.core.designsystem.R
import com.padsmovie.core.domain.repository.MovieCatalogAuthException
import kotlinx.coroutines.CancellationException

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val cause: Throwable) : UiState<Nothing>
}

suspend fun <T> loadState(block: suspend () -> T): UiState<T> = try {
    UiState.Success(block())
} catch (error: CancellationException) {
    throw error
} catch (error: Exception) {
    UiState.Error(error)
}

@Composable
fun errorMessage(error: Throwable): String = stringResource(
    when ((error as? MovieCatalogAuthException)?.reason) {
        MovieCatalogAuthException.Reason.MISSING_TOKEN -> R.string.missing_token
        MovieCatalogAuthException.Reason.REJECTED_TOKEN -> R.string.rejected_token
        null -> R.string.load_error
    }
)
