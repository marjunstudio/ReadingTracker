package com.example.readingtracker.data.database.dao

import androidx.room.*
import com.example.readingtracker.data.database.entity.ReadingPurpose
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingPurposeDao {
    
    @Query("SELECT * FROM reading_purposes WHERE book_id = :bookId")
    suspend fun findByBookId(bookId: String): ReadingPurpose?
    
    @Query("SELECT * FROM reading_purposes WHERE book_id = :bookId ORDER BY created_at DESC")
    fun getPurposesByBookId(bookId: String): Flow<List<ReadingPurpose>>
    
    @Insert
    suspend fun insert(readingPurpose: ReadingPurpose)
    
    @Update
    suspend fun update(readingPurpose: ReadingPurpose)
    
    @Delete
    suspend fun delete(readingPurpose: ReadingPurpose)
}