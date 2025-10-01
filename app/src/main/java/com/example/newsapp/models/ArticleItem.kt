package com.example.newsapp.models

import com.example.newsapp.database.Article as DbArticle

data class ArticleItem(
    val title: String,
    val url: String,
    var imageUrl: String,
    var isSaved: Int = 0,
    var isFavorite: Int = 0
)

// converters
fun ApiArticle.toArticleItem(): ArticleItem {
    return ArticleItem(
        title = this.title ?: "No title",
        url = this.url ?: "",
        imageUrl = this.urlToImage ?: "",
        isSaved = 0,
        isFavorite = 0
    )
}

fun DbArticle.toArticleItem(): ArticleItem {
    return ArticleItem(
        title = this.title,
        url = this.url,
        imageUrl = this.imagePath,
        isSaved = this.isSaved,
        isFavorite = this.isFavorite
    )
}
