package com.news.di

import androidx.viewbinding.BuildConfig
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.news.data.NewsRepo
import com.news.data.NewsRepoImpl
import com.news.data.SourcesRepo
import com.news.data.SourcesRepoImpl
import com.news.network.ArticlesApiService
import com.news.network.SourcesApiService
import com.news.utils.KeyHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://newsapi.org/v2/"

    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .setLenient()
                        .excludeFieldsWithoutExposeAnnotation().create()
                )
            )
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(okHttpClient)
            .build()
    }

    @Provides
    fun providesOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    @Provides
    fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().also {
            it.level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    fun provideArticlesApi(retrofit: Retrofit): ArticlesApiService {
        return retrofit.create(ArticlesApiService::class.java)
    }

    @Provides
    fun provideSourcesApi(retrofit: Retrofit): SourcesApiService {
        return retrofit.create(SourcesApiService::class.java)
    }

    @Provides
    fun provideNewsRepo(
        apiService: ArticlesApiService,
        keyHelper: KeyHelper
    ): NewsRepo {
        return NewsRepoImpl(apiService, keyHelper)
    }

    @Provides
    fun provideSourcesRepo(
        apiService: SourcesApiService,
        keyHelper: KeyHelper
    ): SourcesRepo {
        return SourcesRepoImpl(apiService, keyHelper)
    }

}