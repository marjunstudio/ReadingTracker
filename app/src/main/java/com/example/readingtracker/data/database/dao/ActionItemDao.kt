package com.example.readingtracker.data.database.dao

import androidx.room.*
import com.example.readingtracker.data.database.entity.ActionItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionItemDao {
    
    @Query("SELECT * FROM action_items WHERE book_id = :bookId ORDER BY created_at DESC")
    fun getActionItemsByBookId(bookId: String): Flow<List<ActionItem>>
    
    @Query("SELECT COUNT(*) FROM action_items WHERE book_id = :bookId")
    suspend fun getActionItemCount(bookId: String): Int
    
    @Insert
    suspend fun insert(actionItem: ActionItem)
    
    @Update
    suspend fun update(actionItem: ActionItem)
    
    @Delete
    suspend fun delete(actionItem: ActionItem)
}