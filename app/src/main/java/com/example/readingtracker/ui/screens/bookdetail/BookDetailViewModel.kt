package com.example.readingtracker.ui.screens.bookdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingtracker.data.database.dao.ActionItemDao
import com.example.readingtracker.data.database.dao.BookDao
import com.example.readingtracker.data.database.dao.MemoDao
import com.example.readingtracker.data.database.dao.ReadingPurposeDao
import com.example.readingtracker.data.database.entity.ActionItem
import com.example.readingtracker.data.database.entity.Book
import com.example.readingtracker.data.database.entity.Memo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val bookDao: BookDao,
    private val memoDao: MemoDao,
    private val actionItemDao: ActionItemDao,
    private val readingPurposeDao: ReadingPurposeDao
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()
    
    fun loadBook(bookId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val book = bookDao.findById(bookId)
                if (book != null) {
                    combine(
                        memoDao.getMemosByBookId(bookId),
                        actionItemDao.getActionItemsByBookId(bookId),
                        readingPurposeDao.getPurposesByBookId(bookId)
                    ) { memos, actionItems, purposes ->
                        BookDetailUiState(
                            book = book,
                            memos = memos,
                            actionItems = actionItems,
                            purposes = purposes.map { it.purposeText },
                            isLoading = false
                        )
                    }.collect { state ->
                        _uiState.value = state
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    fun startReading() {
        viewModelScope.launch {
            val currentBook = _uiState.value.book
            if (currentBook != null && currentBook.startedAt == null) {
                val updatedBook = currentBook.copy(
                    startedAt = System.currentTimeMillis()
                )
                bookDao.update(updatedBook)
                _uiState.value = _uiState.value.copy(book = updatedBook)
            }
        }
    }
}

data class BookDetailUiState(
    val book: Book? = null,
    val memos: List<Memo> = emptyList(),
    val actionItems: List<ActionItem> = emptyList(),
    val purposes: List<String> = emptyList(),
    val isLoading: Boolean = false
)