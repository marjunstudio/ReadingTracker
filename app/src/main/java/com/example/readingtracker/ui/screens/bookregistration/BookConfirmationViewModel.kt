package com.example.readingtracker.ui.screens.bookregistration

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.readingtracker.data.model.BookInfo
import com.example.readingtracker.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class BookConfirmationViewModel @Inject constructor(
    // TODO: BookRegistrationStateを追加してDIで注入
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BookConfirmationUiState())
    val uiState: StateFlow<BookConfirmationUiState> = _uiState.asStateFlow()
    
    init {
        // TODO: 一時保存された書籍情報を読み込み
        loadBookInfo()
    }
    
    private fun loadBookInfo() {
        // TODO: 実装
        // 仮のデータを設定
        _uiState.value = _uiState.value.copy(
            bookInfo = BookInfo(
                title = "サンプル書籍",
                author = "サンプル著者",
                isbn = "9784000000000",
                coverImageUrl = null
            ),
            isLoading = false
        )
    }
    
    fun confirmBook(navController: NavController) {
        navController.navigate(Routes.PURPOSE_SETTING)
    }
}

data class BookConfirmationUiState(
    val bookInfo: BookInfo? = null,
    val isLoading: Boolean = true
)