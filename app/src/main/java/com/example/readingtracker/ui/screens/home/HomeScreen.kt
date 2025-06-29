package com.example.readingtracker.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import com.example.readingtracker.ui.components.AppTopBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.readingtracker.data.database.entity.Book
import com.example.readingtracker.data.model.BookStatus
import com.example.readingtracker.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Reading Tracker",
                actions = {
                    IconButton(onClick = { /* TODO: 検索実装 */ }) {
                        Icon(Icons.Default.Search, contentDescription = "検索")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Routes.BOOK_REGISTRATION_METHOD)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "書籍を追加")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            // 読書中セクション
            item {
                BookSection(
                    title = "読書中の本",
                    books = uiState.readingBooks,
                    emptyMessage = "読書中の本はありません",
                    onBookClick = { book ->
                        navController.navigate(Routes.bookDetail(book.id))
                    }
                )
            }

            // 完了セクション
            item {
                BookSection(
                    title = "完了した本",
                    books = uiState.completedBooks,
                    emptyMessage = "完了した本はありません",
                    onBookClick = { book ->
                        navController.navigate(Routes.bookDetail(book.id))
                    }
                )
            }

            // 空状態
            if (uiState.readingBooks.isEmpty() && uiState.completedBooks.isEmpty()) {
                item {
                    EmptyState()
                }
            }
        }
    }
}

@Composable
private fun BookSection(
    title: String,
    books: List<Book>,
    emptyMessage: String,
    onBookClick: (Book) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (books.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = emptyMessage,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                books.forEach { book ->
                    BookCard(
                        book = book,
                        memoCount = 0, // TODO: ViewModelから取得
                        actionItemCount = 0, // TODO: ViewModelから取得
                        onClick = { onBookClick(book) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📚",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "最初の1冊を登録してみましょう",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "読書の目的を設定して、実践的な学びを得ましょう",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}