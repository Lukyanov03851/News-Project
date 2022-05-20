package com.news.network

import com.news.model.dto.SourcesListResponse
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SourcesApiService {

    @GET(Endpoints.SOURCES_LIST)
    fun loadSourcesAsync(
        @Query("apiKey") apiKey: String
    ): Deferred<Response<SourcesListResponse>>
}