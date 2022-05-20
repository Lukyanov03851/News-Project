package com.news.model.mappers

import com.news.model.domain.NewsModel
import com.news.model.dto.Article

class NewsMapper : Mapper<Article, NewsModel> {
    override fun transform(src: Article): NewsModel {
        return NewsModel(
            title = src.title ?: "",
            description = src.description ?: "",
            url = src.url ?: "",
            picture = src.urlToImage ?: "",
            source = src.source?.name ?: ""
        )
    }
}