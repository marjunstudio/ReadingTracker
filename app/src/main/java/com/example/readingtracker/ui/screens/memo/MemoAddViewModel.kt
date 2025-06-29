package com.example.readingtracker.ui.screens.memo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingtracker.data.database.dao.MemoDao
import com.example.readingtracker.data.database.entity.Memo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoAddViewModel @Inject constructor(
    private val memoDao: MemoDao
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MemoAddUiState())
    val uiState: StateFlow<MemoAddUiState> = _uiState.asStateFlow()
    
    private var bookId: String = ""
    
    fun initialize(bookId: String) {
        this.bookId = bookId
        viewModelScope.launch {
            val memoCount = memoDao.getMemoCount(bookId)
            _uiState.value = _uiState.value.copy(currentMemoCount = memoCount)
        }
    }
    
    fun updateQuote(quote: String) {
        _uiState.value = _uiState.value.copy(
            quote = quote,
            quoteError = null
        )
        updateCanSave()
    }
    
    fun updateComment(comment: String) {
        _uiState.value = _uiState.value.copy(
            comment = comment,
            commentError = null
        )
        updateCanSave()
    }
    
    fun updatePageNumber(pageNumber: String) {
        // 数字のみを許可
        val filteredPageNumber = pageNumber.filter { it.isDigit() }
        _uiState.value = _uiState.value.copy(
            pageNumber = filteredPageNumber,
            pageNumberError = null
        )
    }
    
    private fun updateCanSave() {
        val state = _uiState.value
        val canSave = state.quote.isNotBlank() && 
                     state.comment.isNotBlank() && 
                     state.currentMemoCount < 10
        _uiState.value = state.copy(canSave = canSave)
    }
    
    fun saveMemo() {
        val state = _uiState.value
        
        // バリデーション
        var hasError = false
        
        if (state.quote.trim().isEmpty()) {
            _uiState.value = state.copy(quoteError = "引用文を入力してください")
            hasError = true
        }
        
        if (state.comment.trim().isEmpty()) {
            _uiState.value = state.copy(commentError = "感想を入力してください")
            hasError = true
        }
        
        if (state.currentMemoCount >= 10) {
            _uiState.value = state.copy(errorMessage = "メモは最大10個までです")
            hasError = true
        }
        
        if (hasError) return
        
        _uiState.value = state.copy(isSaving = true)
        
        viewModelScope.launch {
            try {
                val memo = Memo(
                    bookId = bookId,
                    quote = state.quote.trim(),
                    comment = state.comment.trim(),
                    pageNumber = state.pageNumber.toIntOrNull(),
                    createdAt = System.currentTimeMillis()
                )
                
                memoDao.insert(memo)
                _uiState.value = state.copy(
                    isSaving = false,
                    isSaved = true
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = state.copy(
                    isSaving = false,
                    errorMessage = "メモの保存に失敗しました"
                )
            }
        }
    }
}

data class MemoAddUiState(
    val quote: String = "",
    val comment: String = "",
    val pageNumber: String = "",
    val currentMemoCount: Int = 0,
    val canSave: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val quoteError: String? = null,
    val commentError: String? = null,
    val pageNumberError: String? = null,
    val errorMessage: String? = null
)