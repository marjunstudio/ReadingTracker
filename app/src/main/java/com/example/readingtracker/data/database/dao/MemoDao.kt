package com.example.readingtracker.data.database.dao

import androidx.room.*
import com.example.readingtracker.data.database.entity.Memo
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {
    
    @Query("SELECT * FROM memos WHERE id = :id")
    suspend fun findById(id: String): Memo?
    
    @Query("SELECT * FROM memos WHERE book_id = :bookId ORDER BY created_at DESC")
    fun getMemosByBookId(bookId: String): Flow<List<Memo>>
    
    @Query("SELECT COUNT(*) FROM memos WHERE book_id = :bookId")
    suspend fun getMemoCount(bookId: String): Int
    
    @Insert
    suspend fun insert(memo: Memo)
    
    @Update
    suspend fun update(memo: Memo)
    
    @Delete
    suspend fun delete(memo: Memo)
}