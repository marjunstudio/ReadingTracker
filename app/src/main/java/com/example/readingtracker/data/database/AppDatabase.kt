package com.example.readingtracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.readingtracker.data.database.dao.*
import com.example.readingtracker.data.database.entity.*

@Database(
    entities = [
        Book::class,
        ReadingPurpose::class,
        Memo::class,
        ActionItem::class,
        BookCache::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun bookDao(): BookDao
    abstract fun readingPurposeDao(): ReadingPurposeDao
    abstract fun memoDao(): MemoDao
    abstract fun actionItemDao(): ActionItemDao
    abstract fun bookCacheDao(): BookCacheDao
    
    companion object {
        const val DATABASE_NAME = "reading_tracker_database"
    }
}