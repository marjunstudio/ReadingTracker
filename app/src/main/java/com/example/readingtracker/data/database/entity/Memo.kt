package com.example.readingtracker.data.database.entity

import androidx.room.*
import java.util.UUID

@Entity(
    tableName = "memos",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["book_id"])]
)
data class Memo(
    @PrimaryKey 
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "book_id")
    val bookId: String,
    
    @ColumnInfo(name = "quote")
    val quote: String,
    
    @ColumnInfo(name = "comment")
    val comment: String,
    
    @ColumnInfo(name = "page_number")
    val pageNumber: Int? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)