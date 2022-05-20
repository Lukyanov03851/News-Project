package com.news.model.dto

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SourcesListResponse {
    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("sources")
    @Expose
    var sources: List<Source>? = null
}