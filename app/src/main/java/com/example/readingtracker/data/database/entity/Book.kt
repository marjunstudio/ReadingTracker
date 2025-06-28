package com.example.readingtracker.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.readingtracker.data.model.BookStatus
import java.util.UUID

@Entity(tableName = "books")
data class Book(
    @PrimaryKey 
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "author")
    val author: String,
    
    @ColumnInfo(name = "isbn")
    val isbn: String? = null,
    
    @ColumnInfo(name = "cover_image_url")
    val coverImageUrl: String? = null,
    
    @ColumnInfo(name = "status")
    val status: BookStatus,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "started_at")
    val startedAt: Long? = null,
    
    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,
    
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis()
)