package com.example.newsapp.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase

class DBManager(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    // method for insertion
    fun getWritableDB() : SQLiteDatabase = dbHelper.writableDatabase

    // method for reading
    fun getReadableDB() : SQLiteDatabase = dbHelper.readableDatabase

    //close connection
    private fun closeDB(){
        dbHelper.close()
    }

    // reset table
    private fun clearTable(){
        val db = getWritableDB()
        db.delete("Article", null, null)
    }
}