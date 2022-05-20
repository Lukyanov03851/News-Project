package com.news.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.news.model.domain.NewsModel
import com.news.model.dto.SortBy
import com.news.model.mappers.NewsMapper
import com.news.network.ArticlesApiService
import com.news.utils.KeyHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface NewsRepo {
    fun getNewsResultStream(sortBy: SortBy, source: String?, from: String?, to: String?): Flow<PagingData<NewsModel>>
}

class NewsRepoImpl @Inject constructor(
    private val apiService: ArticlesApiService,
    private val keyHelper: KeyHelper
) : NewsRepo {
    override fun getNewsResultStream(sortBy: SortBy, source: String?, from: String?, to: String?): Flow<PagingData<NewsModel>> {
        val mapper = NewsMapper()
        return Pager(PagingConfig(pageSize = 3)) {
            NewsPagingSource(apiService, Dispatchers.IO, keyHelper, sortBy, source, from, to)
        }
            .flow
            .map { pagingData ->
                pagingData.map { mapper.transform(it) }
            }
    }
}