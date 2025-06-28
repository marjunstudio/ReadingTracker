package com.example.readingtracker.data.database.dao

import androidx.room.*
import com.example.readingtracker.data.database.entity.Book
import com.example.readingtracker.data.model.BookStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    
    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun findById(id: String): Book?
    
    @Query("SELECT * FROM books WHERE isbn = :isbn")
    suspend fun findByIsbn(isbn: String): Book?
    
    @Insert
    suspend fun insert(book: Book)
    
    @Update
    suspend fun update(book: Book)
    
    @Delete
    suspend fun delete(book: Book)
    
    @Query("SELECT * FROM books WHERE status = :status ORDER BY created_at DESC")
    fun getBooksByStatus(status: BookStatus): Flow<List<Book>>
    
    @Query("SELECT * FROM books WHERE status = 'READING' ORDER BY created_at DESC")
    fun getReadingBooks(): Flow<List<Book>>
    
    @Query("SELECT * FROM books WHERE status = 'COMPLETED' ORDER BY completed_at DESC")
    fun getCompletedBooks(): Flow<List<Book>>
    
    @Query("UPDATE books SET status = 'COMPLETED', completed_at = :completedAt WHERE id = :bookId")
    suspend fun completeBook(bookId: String, completedAt: Long)
    
    @Query("SELECT COUNT(*) FROM books WHERE status = :status")
    suspend fun getBookCountByStatus(status: BookStatus): Int
    
    @Query("SELECT COUNT(*) FROM books")
    suspend fun getTotalBookCount(): Int
}