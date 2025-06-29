package com.example.readingtracker.ui.screens.bookregistration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.readingtracker.data.repository.BookSearchRepository
import com.example.readingtracker.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BarcodeScannerViewModel @Inject constructor(
    private val bookSearchRepository: BookSearchRepository
) : ViewModel() {
    
    private var isProcessing = false
    
    fun handleBarcodeDetected(isbn: String, navController: NavController) {
        if (isProcessing) return
        isProcessing = true
        
        viewModelScope.launch {
            try {
                val bookInfo = bookSearchRepository.searchByIsbn(isbn)
                if (bookInfo != null) {
                    // TODO: 一時的に書籍情報を保存して確認画面へ遷移
                    navController.navigate(Routes.BOOK_CONFIRMATION)
                } else {
                    // 書籍が見つからない場合は手動入力へ
                    navController.popBackStack()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                navController.popBackStack()
            } finally {
                isProcessing = false
            }
        }
    }
}