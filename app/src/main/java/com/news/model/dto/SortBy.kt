package com.news.model.dto

enum class SortBy(val value: String) {
    PUBLISHED_AT("publishedAt"),
    RELEVANCY("relevancy"),
    POPULARITY("popularity")
}