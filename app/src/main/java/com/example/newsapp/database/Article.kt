package com.example.newsapp.database

data class Article (
        val id: Int = 0,
        val title: String,
        val url: String,
        val imagePath: String,
        val isSaved: Int = 0,
        val isFavorite: Int = 0
)

