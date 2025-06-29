package com.example.readingtracker.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingtracker.data.database.dao.BookDao
import com.example.readingtracker.data.database.dao.MemoDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val bookDao: BookDao,
    private val memoDao: MemoDao
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _selectedMonth.collect { month ->
                loadStatistics(month)
            }
        }
    }

    private suspend fun loadStatistics(month: YearMonth) {
        val startDate = month.atDay(1).atStartOfDay()
        val endDate = month.atEndOfMonth().atTime(23, 59, 59)
        
        val startMillis = startDate.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endDate.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val completedBooks = bookDao.getCompletedBooksInPeriod(startMillis, endMillis)
        val completedBooksInfo = completedBooks.map { book ->
            val memoCount = memoDao.getMemoCountByBookId(book.id)
            CompletedBookInfo(
                id = book.id,
                title = book.title,
                author = book.author,
                completedAt = book.completedAt?.let {
                    LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(it), java.time.ZoneId.systemDefault())
                } ?: LocalDateTime.now(),
                memoCount = memoCount
            )
        }
        
        val totalMemos = completedBooksInfo.sumOf { it.memoCount }
        
        _uiState.value = StatisticsUiState(
            selectedMonth = month,
            completedBooksCount = completedBooks.size,
            totalMemosCount = totalMemos,
            completedBooks = completedBooksInfo
        )
    }

    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        val nextMonth = _selectedMonth.value.plusMonths(1)
        if (nextMonth <= YearMonth.now()) {
            _selectedMonth.value = nextMonth
        }
    }
}

data class StatisticsUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val completedBooksCount: Int = 0,
    val totalMemosCount: Int = 0,
    val completedBooks: List<CompletedBookInfo> = emptyList()
)

data class CompletedBookInfo(
    val id: String,
    val title: String,
    val author: String?,
    val completedAt: LocalDateTime,
    val memoCount: Int
)