package com.example.readingtracker.data.database.entity

import androidx.room.*
import com.example.readingtracker.data.model.PurposeCategory
import java.util.UUID

@Entity(
    tableName = "reading_purposes",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["book_id"], unique = true)]
)
data class ReadingPurpose(
    @PrimaryKey 
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "book_id")
    val bookId: String,
    
    @ColumnInfo(name = "purpose_text")
    val purposeText: String,
    
    @ColumnInfo(name = "category")
    val category: PurposeCategory,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)