package com.news.model.mappers

import com.news.model.domain.SourceModel
import com.news.model.dto.Source

class SourcesMapper : Mapper<Source, SourceModel> {
    override fun transform(src: Source): SourceModel {
        return SourceModel(
            id = src.id ?: "",
            name = src.name ?: "",
            isSelected = false
        )
    }
}