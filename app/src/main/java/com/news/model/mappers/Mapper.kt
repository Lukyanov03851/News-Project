package com.news.model.mappers

interface Mapper<SRC, DST> {
    fun transform(src: SRC): DST
}