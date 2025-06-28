package com.example.readingtracker.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_cache")
data class BookCache(
    @PrimaryKey 
    val isbn: String,
    val title: String,
    val author: String,
    val coverImageUrl: String?,
    val description: String?,
    val publisher: String?,
    val publishedDate: String?,
    val cachedAt: Long,
    val apiSource: String
)