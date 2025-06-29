package com.example.readingtracker.ui.screens.bookregistration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.readingtracker.data.database.dao.BookDao
import com.example.readingtracker.data.database.dao.ReadingPurposeDao
import com.example.readingtracker.data.database.entity.Book
import com.example.readingtracker.data.database.entity.ReadingPurpose
import com.example.readingtracker.data.model.BookStatus
import com.example.readingtracker.data.model.PurposeCategory
import com.example.readingtracker.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PurposeSettingViewModel @Inject constructor(
    private val bookDao: BookDao,
    private val readingPurposeDao: ReadingPurposeDao
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PurposeSettingUiState())
    val uiState: StateFlow<PurposeSettingUiState> = _uiState.asStateFlow()
    
    fun addPurpose(purpose: String) {
        val currentPurposes = _uiState.value.purposes.toMutableList()
        currentPurposes.add(purpose)
        _uiState.value = _uiState.value.copy(purposes = currentPurposes)
    }
    
    fun deletePurpose(purpose: String) {
        val currentPurposes = _uiState.value.purposes.toMutableList()
        currentPurposes.remove(purpose)
        _uiState.value = _uiState.value.copy(purposes = currentPurposes)
    }
    
    fun registerBook(navController: NavController) {
        viewModelScope.launch {
            try {
                // TODO: 実際の書籍情報を使用
                val book = Book(
                    title = "サンプル書籍",
                    author = "サンプル著者",
                    isbn = "9784000000000",
                    coverImageUrl = null,
                    status = BookStatus.READING
                )
                
                bookDao.insert(book)
                
                // 読書目的を保存
                _uiState.value.purposes.forEach { purpose ->
                    val readingPurpose = ReadingPurpose(
                        bookId = book.id,
                        purposeText = purpose,
                        category = PurposeCategory.OTHER // TODO: 適切なカテゴリを選択
                    )
                    readingPurposeDao.insert(readingPurpose)
                }
                
                // ホーム画面に戻る
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.HOME) { inclusive = true }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // TODO: エラーハンドリング
            }
        }
    }
}

data class PurposeSettingUiState(
    val purposes: List<String> = emptyList()
)