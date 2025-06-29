package com.example.readingtracker.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingtracker.data.database.dao.BookDao
import com.example.readingtracker.data.database.entity.Book
import com.example.readingtracker.data.model.BookStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookDao: BookDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadBooks()
    }

    private fun loadBooks() {
        viewModelScope.launch {
            combine(
                bookDao.getBooksByStatus(BookStatus.READING),
                bookDao.getBooksByStatus(BookStatus.COMPLETED)
            ) { readingBooks, completedBooks ->
                HomeUiState(
                    readingBooks = readingBooks,
                    completedBooks = completedBooks
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

data class HomeUiState(
    val readingBooks: List<Book> = emptyList(),
    val completedBooks: List<Book> = emptyList()
)