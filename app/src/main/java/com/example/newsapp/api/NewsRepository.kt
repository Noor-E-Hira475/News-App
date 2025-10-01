package com.example.newsapp.api

import android.util.Log
import com.example.newsapp.models.NewsResponse
import com.example.newsapp.network.ResultState

class NewsRepository(private val apiKey: String) {

    suspend fun getHeadlines(): ResultState<NewsResponse> {
        return try {
            val response = RetrofitInstance.api.getHeadlines(apiKey = apiKey)


            Log.d("API_RESPONSE", "Headlines Response: $response")

            // Check if articles are empty
            if (response.articles.isNotEmpty()) {
                ResultState.Success(response)
            } else {
                ResultState.Error("No articles found or quota exceeded")
            }
        } catch (e: Exception) {
            Log.e("API_RESPONSE", "Error in getHeadlines: ${e.message}", e)
            ResultState.Error(e.message ?: "Unknown Error")
        }
    }

    suspend fun searchNews(query: String): ResultState<NewsResponse> {
        return try {
            val response = RetrofitInstance.api.searchNews(query = query, apiKey = apiKey)
            ResultState.Success(response)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Unknown Error")
        }
    }
}

