package com.example.readingtracker.data.database.dao

import androidx.room.*
import com.example.readingtracker.data.database.entity.BookCache

@Dao
interface BookCacheDao {
    
    @Query("SELECT * FROM book_cache WHERE isbn = :isbn")
    suspend fun findByIsbn(isbn: String): BookCache?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookCache: BookCache)
    
    @Query("DELETE FROM book_cache WHERE cachedAt < :expiredTime")
    suspend fun deleteExpired(expiredTime: Long)
    
    @Query("DELETE FROM book_cache")
    suspend fun deleteAll()
}