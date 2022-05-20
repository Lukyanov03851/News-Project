package com.news.utils

import com.news.network.Resource
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

object ErrorHelper {
    fun getErrorMessage(errorBody: ResponseBody?): String {
        var errorMsg = "Something went wrong!"
        try {
            val jObjError = JSONObject(errorBody?.string() ?: "{}")
            errorMsg = jObjError.getString("message")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return errorMsg
    }

    fun <T> processException(e: Exception): Resource<T> {
        e.printStackTrace()
        return if (e is HttpException || e is IOException) {
            Resource.error("Connection error!")
        } else {
            Resource.error("Something went wrong!")
        }
    }
}