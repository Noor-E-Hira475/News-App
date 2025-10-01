package com.example.newsapp.api

import com.example.newsapp.models.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query


interface NewsApiService {
    @GET("v2/everything")
        suspend fun searchNews(
            @Query("qInTitle") query: String? = null,
            @Query("sortBy") sortBy: String = "relevancy",
            @Query("language") language: String = "en",
            @Query("apiKey") apiKey: String
        ): NewsResponse

    @GET("v2/top-headlines")
    suspend fun getHeadlines(
        @Query("country") country: String = "us",
        @Query("apiKey") apiKey: String
    ): NewsResponse
}