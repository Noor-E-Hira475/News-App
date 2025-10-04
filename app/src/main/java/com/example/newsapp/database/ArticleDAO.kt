package com.example.newsapp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
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
            put("imagePath", localPath)
            put("isSaved", isSaved)
            put("isFavorite", isFavorite)
        }
        val rowId = db.insertWithOnConflict("Articles", null, values, SQLiteDatabase.CONFLICT_REPLACE)
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

    fun updateArticleFlags(url: String, isSaved: Int? = null, isFavorite: Int? = null) {
        val db = dbManager.getWritableDB()
        val values = ContentValues()

        if (isSaved != null) values.put("isSaved", isSaved)
        if (isFavorite != null) values.put("isFavorite", isFavorite)

        db.update("Articles", values, "url = ?", arrayOf(url))

        // Only delete row if both flags are 0
        val cursor = db.rawQuery("SELECT isSaved, isFavorite FROM Articles WHERE url = ?", arrayOf(url))
        if (cursor.moveToFirst()) {
            val saved = cursor.getInt(cursor.getColumnIndexOrThrow("isSaved"))
            val fav = cursor.getInt(cursor.getColumnIndexOrThrow("isFavorite"))
            if (saved == 0 && fav == 0) {
                db.delete("Articles", "url = ?", arrayOf(url))
            }
        }
        cursor.close()
        db.close()
    }

    fun deleteArticleByUrl(url: String) {
        val db = dbManager.getWritableDB()
        db.delete("Articles", "url = ?", arrayOf(url))
        db.close()
    }

    fun getSavedArticles(): List<Article> {
        val db = dbManager.getReadableDB()
        val cursor = db.rawQuery("SELECT * FROM Articles WHERE isSaved = 1", null)
        val articles = mutableListOf<Article>()

        if (cursor.moveToFirst()) {
            do {
                val article = Article(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
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

    fun getFavoriteArticles(): List<Article> {
        val db = dbManager.getReadableDB()
        val cursor = db.rawQuery("SELECT * FROM Articles WHERE isFavorite = 1", null)
        val articles = mutableListOf<Article>()

        if (cursor.moveToFirst()) {
            do {
                val article = Article(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
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

}
