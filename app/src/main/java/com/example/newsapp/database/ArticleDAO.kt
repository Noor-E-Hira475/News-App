package com.example.newsapp.database

import android.content.ContentValues
import android.content.Context
import com.example.newsapp.utils.ImageUtils

class ArticleDAO(private val dbManager: DBManager, private val context: Context) {

    // insertion
    fun insertArticle(title: String, url: String, imagePath: String, isSaved: Int, isFavorite: Int): Int {
        val db = dbManager.getWritableDB()
        // Save image locally (if provided)
        val localPath = if (imagePath.isNotEmpty()) {
            ImageUtils.saveImageLocally(context,imagePath, "user_${System.currentTimeMillis()}")
        } else {
            ""
        }
        val values = ContentValues().apply {
            put("title", title)
            put("url", url)
            put("imagePath", imagePath)
            put("isSaved", isSaved)
            put("isFavorite", isFavorite)
        }
        val rowId = db.insert("Articles", null, values)
        db.close()
        return rowId.toInt()
    }

    // reading
    fun getAllArticles(): List<Article> {
        val articles = mutableListOf<Article>()
        val db = dbManager.getReadableDB()
        val cursor = db.rawQuery("SELECT * FROM Articles", null)

        if (cursor.moveToFirst()) {
            do {
                val article = Article(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),  // FIX: column name is 'id'
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    url = cursor.getString(cursor.getColumnIndexOrThrow("url")),
                    imagePath = cursor.getString(cursor.getColumnIndexOrThrow("imagePath")),
                    isSaved = cursor.getInt(cursor.getColumnIndexOrThrow("isSaved")),
                    isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow("isFavorite"))
                )
                articles.add(article)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return articles
    }

    fun deleteArticleByUrl(url: String) {
        val db = dbManager.getWritableDB()
        db.delete("Articles", "url = ?", arrayOf(url))
        db.close()
    }
}
