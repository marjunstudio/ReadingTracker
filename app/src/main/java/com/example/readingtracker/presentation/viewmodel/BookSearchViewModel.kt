package com.example.readingtracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readingtracker.data.api.model.BookData
import com.example.readingtracker.data.model.BookSearchResult
import com.example.readingtracker.data.repository.BookSearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookSearchViewModel @Inject constructor(
    private val bookSearchRepository: BookSearchRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BookSearchUiState())
    val uiState: StateFlow<BookSearchUiState> = _uiState.asStateFlow()
    
    fun searchBook(isbn: String) {
        if (isbn.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "ISBNを入力してください"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                bookData = null
            )
            
            when (val result = bookSearchRepository.searchByIsbn(isbn)) {
                is BookSearchResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        bookData = result.bookData,
                        errorMessage = null
                    )
                }
                BookSearchResult.NotFound -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "書籍が見つかりませんでした"
                    )
                }
                BookSearchResult.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "ネットワークエラーが発生しました"
                    )
                }
                BookSearchResult.RateLimitExceeded -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "リクエストが多すぎます。しばらくお待ちください"
                    )
                }
                BookSearchResult.DailyLimitExceeded -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "本日の検索上限に達しました"
                    )
                }
                is BookSearchResult.ApiError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "エラーが発生しました: ${result.message}"
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class BookSearchUiState(
    val isLoading: Boolean = false,
    val bookData: BookData? = null,
    val errorMessage: String? = null
)