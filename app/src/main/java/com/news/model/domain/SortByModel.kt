package com.news.model.domain

import androidx.annotation.StringRes
import com.news.model.dto.SortBy

class SortByModel(
    val mode: SortBy,
    @StringRes val text: Int,
    val isSelected: Boolean
)