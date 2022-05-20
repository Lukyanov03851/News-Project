package com.news.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.news.model.dto.Article
import com.news.model.dto.SortBy
import com.news.network.ArticlesApiService
import com.news.utils.ErrorHelper
import com.news.utils.KeyHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.lang.RuntimeException

class NewsPagingSource(
    private val apiService: ArticlesApiService,
    private val dispatcher: CoroutineDispatcher,
    private val keyHelper: KeyHelper,
    private val sortBy: SortBy,
    private val source: String?,
    private val from: String?,
    private val to: String?
) : PagingSource<Int, Article>() {

    companion object {
        const val TAG = "NewsPagingSource"
        const val DEFAULT_PAGE_SIZE = 10
    }

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, Article> = withContext(dispatcher) {
        try {
            val page = params.key ?: 1

            val response = apiService.loadArticlesAsync(
                sortBy = sortBy.value,
                source = source,
                from = from,
                to = to,
                pageSize = DEFAULT_PAGE_SIZE,
                page = page,
                apiKey = keyHelper.newsApiKey()
            ).await()

            val articles = response.body()?.articles

            if (response.isSuccessful && articles != null) {
                val nextKey = if (articles.isEmpty()) {
                    null
                } else {
                    page + 1
                }
                LoadResult.Page(
                    data = articles,
                    prevKey = null, // Only paging forward.
                    nextKey = nextKey
                )
            } else {
                LoadResult.Error(RuntimeException(ErrorHelper.getErrorMessage(response.errorBody())))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }

    }

}