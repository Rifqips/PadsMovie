package id.application.padsmovie.feature.movies

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.padsmovie.core.domain.model.MovieSummary
import com.padsmovie.core.domain.repository.MovieRepository
import kotlinx.coroutines.CancellationException

class MoviesPagingSource(
    private val repository: MovieRepository,
    private val genreId: Int,
    private val language: String,
) : PagingSource<Int, MovieSummary>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieSummary> = try {
        val page = repository.discoverMovies(genreId, language, params.key ?: 1)
        LoadResult.Page(
            data = page.items,
            prevKey = if (page.page > 1) page.page - 1 else null,
            // TMDB discover only permits the first 500 pages.
            nextKey = if (page.items.isNotEmpty() && page.page < minOf(page.totalPages, 500)) page.page + 1 else null,
        )
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        LoadResult.Error(error)
    }

    override fun getRefreshKey(state: PagingState<Int, MovieSummary>): Int? =
        state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.let { it.prevKey?.plus(1) ?: it.nextKey?.minus(1) }
        }
}
