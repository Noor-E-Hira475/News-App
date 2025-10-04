package com.example.newsapp.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context): SQLiteOpenHelper(context, "Article_db", null, 2){

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE Articles(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT,
            url TEXT UNIQUE,
            imagePath TEXT,
            isSaved INTEGER,
            isFavorite INTEGER
            ) 
            """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Articles")
        onCreate(db)
    }

}