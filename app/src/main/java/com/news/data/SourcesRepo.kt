package com.news.data

import com.news.model.domain.SourceModel
import com.news.model.mappers.SourcesMapper
import com.news.network.Resource
import com.news.network.SourcesApiService
import com.news.utils.ErrorHelper
import com.news.utils.KeyHelper
import javax.inject.Inject

interface SourcesRepo {
    suspend fun getSourcesList(): Resource<List<SourceModel>>
}

class SourcesRepoImpl @Inject constructor(
    private val apiService: SourcesApiService,
    private val keyHelper: KeyHelper
) : SourcesRepo {

    var sourcesList: List<SourceModel> = listOf()

    override suspend fun getSourcesList(): Resource<List<SourceModel>> {
        return if (sourcesList.isEmpty()) {
            try {
                val response = apiService.loadSourcesAsync(keyHelper.newsApiKey()).await()
                if (response.isSuccessful) {
                    val mapper = SourcesMapper()
                    sourcesList = response.body()?.sources?.map { mapper.transform(it) } ?: listOf()
                    Resource.success(sourcesList)
                } else {
                    Resource.error(ErrorHelper.getErrorMessage(response.errorBody()))
                }
            } catch (e: Exception) {
                ErrorHelper.processException(e)
            }
        } else {
            Resource.success(sourcesList)
        }
    }
}