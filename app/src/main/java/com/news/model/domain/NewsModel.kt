package com.news.model.domain

data class NewsModel(
    val title: String,
    val description: String,
    val source: String,
    val url: String,
    val picture: String
)