package id.application.padsmovie.feature.reviews

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.padsmovie.core.domain.model.Review
import com.padsmovie.core.domain.repository.MovieRepository
import kotlinx.coroutines.CancellationException

class ReviewsPagingSource(
    private val repository: MovieRepository,
    private val movieId: Int,
    private val language: String,
) : PagingSource<Int, Review>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Review> = try {
        val page = repository.getReviews(movieId, language, params.key ?: 1)
        LoadResult.Page(
            data = page.items,
            prevKey = if (page.page > 1) page.page - 1 else null,
            nextKey = if (page.items.isNotEmpty() && page.page < page.totalPages) page.page + 1 else null,
        )
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        LoadResult.Error(error)
    }

    override fun getRefreshKey(state: PagingState<Int, Review>): Int? =
        state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.let { it.prevKey?.plus(1) ?: it.nextKey?.minus(1) }
        }
}
