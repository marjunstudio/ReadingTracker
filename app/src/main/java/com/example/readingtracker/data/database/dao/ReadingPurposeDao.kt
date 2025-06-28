package com.example.readingtracker.data.database.dao

import androidx.room.*
import com.example.readingtracker.data.database.entity.ReadingPurpose

@Dao
interface ReadingPurposeDao {
    
    @Query("SELECT * FROM reading_purposes WHERE book_id = :bookId")
    suspend fun findByBookId(bookId: String): ReadingPurpose?
    
    @Insert
    suspend fun insert(readingPurpose: ReadingPurpose)
    
    @Update
    suspend fun update(readingPurpose: ReadingPurpose)
    
    @Delete
    suspend fun delete(readingPurpose: ReadingPurpose)
}