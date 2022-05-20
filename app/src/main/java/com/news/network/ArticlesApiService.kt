package com.news.network

import com.news.model.dto.ArticlesListResponse
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ArticlesApiService {

    @GET(Endpoints.ARTICLES_LIST)
    fun loadArticlesAsync(
        @Query("sortBy") sortBy: String,
        @Query("sources") source: String?,
        @Query("from") from: String?,
        @Query("to") to: String?,
        @Query("q") query: String = "news",
        @Query("pageSize") pageSize: Int,
        @Query("page") page: Int,
        @Query("apiKey") apiKey: String
    ) : Deferred<Response<ArticlesListResponse>>

}