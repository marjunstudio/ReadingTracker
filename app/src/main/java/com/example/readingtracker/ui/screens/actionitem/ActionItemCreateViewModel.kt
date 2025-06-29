package com.example.readingtracker.ui.screens.actionitem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingtracker.data.database.dao.ActionItemDao
import com.example.readingtracker.data.database.dao.BookDao
import com.example.readingtracker.data.database.dao.MemoDao
import com.example.readingtracker.data.database.entity.ActionItem
import com.example.readingtracker.data.database.entity.Memo
import com.example.readingtracker.data.model.BookStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActionItemCreateViewModel @Inject constructor(
    private val memoDao: MemoDao,
    private val actionItemDao: ActionItemDao,
    private val bookDao: BookDao
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ActionItemCreateUiState())
    val uiState: StateFlow<ActionItemCreateUiState> = _uiState.asStateFlow()
    
    private var bookId: String = ""
    private var memoId: String = ""
    
    fun initialize(bookId: String, memoId: String) {
        this.bookId = bookId
        this.memoId = memoId
        
        viewModelScope.launch {
            try {
                val memo = memoDao.findById(memoId)
                val actionCount = actionItemDao.getActionItemCountByBookId(bookId)
                
                _uiState.value = _uiState.value.copy(
                    memo = memo,
                    currentActionCount = actionCount
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    errorMessage = "データの読み込みに失敗しました"
                )
            }
        }
    }
    
    fun updateActionText(actionText: String) {
        _uiState.value = _uiState.value.copy(
            actionText = actionText,
            actionTextError = null,
            canSave = actionText.isNotBlank()
        )
    }
    
    fun createActionItem() {
        val state = _uiState.value
        
        // バリデーション
        if (state.actionText.trim().isEmpty()) {
            _uiState.value = state.copy(actionTextError = "実践項目を入力してください")
            return
        }
        
        if (state.currentActionCount >= 3) {
            _uiState.value = state.copy(errorMessage = "実践項目は最大3個までです")
            return
        }
        
        _uiState.value = state.copy(isSaving = true)
        
        viewModelScope.launch {
            try {
                val actionItem = ActionItem(
                    bookId = bookId,
                    memoId = memoId,
                    actionText = state.actionText.trim(),
                    createdAt = System.currentTimeMillis()
                )
                
                actionItemDao.insert(actionItem)
                
                // 実践項目が3個に達した場合、本を完了状態にする
                val newActionCount = state.currentActionCount + 1
                if (newActionCount >= 3) {
                    val book = bookDao.findById(bookId)
                    if (book != null && book.status != BookStatus.COMPLETED) {
                        val completedBook = book.copy(
                            status = BookStatus.COMPLETED,
                            completedAt = System.currentTimeMillis()
                        )
                        bookDao.update(completedBook)
                    }
                }
                
                _uiState.value = state.copy(
                    isSaving = false,
                    isSaved = true
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = state.copy(
                    isSaving = false,
                    errorMessage = "実践項目の作成に失敗しました"
                )
            }
        }
    }
}

data class ActionItemCreateUiState(
    val memo: Memo? = null,
    val actionText: String = "",
    val currentActionCount: Int = 0,
    val canSave: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val actionTextError: String? = null,
    val errorMessage: String? = null
)