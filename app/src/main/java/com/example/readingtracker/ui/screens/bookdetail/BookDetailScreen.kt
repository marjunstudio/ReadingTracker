package com.example.readingtracker.ui.screens.bookdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.example.readingtracker.ui.components.AppTopBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.readingtracker.data.database.entity.ActionItem
import com.example.readingtracker.data.database.entity.Book
import com.example.readingtracker.data.database.entity.Memo
import com.example.readingtracker.data.model.BookStatus
import com.example.readingtracker.ui.navigation.Routes
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    navController: NavController,
    bookId: String,
    viewModel: BookDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "書籍詳細",
                navController = navController,
                showBackButton = true
            )
        }
    ) { paddingValues ->
        uiState.book?.let { book ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // 書籍情報ヘッダー
                item {
                    BookHeader(book = book)
                }
                
                // 読書目的セクション
                item {
                    PurposeSection(purposes = uiState.purposes)
                }
                
                // 読書開始ボタン（初回のみ）
                if (book.startedAt == null) {
                    item {
                        StartReadingButton(
                            onClick = { viewModel.startReading() }
                        )
                    }
                }
                
                // メモセクション
                item {
                    MemoSection(
                        memos = uiState.memos,
                        memoCount = uiState.memos.size,
                        onAddMemoClick = {
                            navController.navigate(Routes.memoAdd(bookId))
                        },
                        onCreateActionItemClick = { memo ->
                            navController.navigate(Routes.actionItemCreate(bookId, memo.id))
                        },
                        canCreateActionItem = uiState.actionItems.size < 3
                    )
                }
                
                // 実践項目セクション
                item {
                    ActionItemSection(
                        actionItems = uiState.actionItems,
                        isCompleted = uiState.actionItems.size == 3
                    )
                }
            }
        }
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun BookHeader(book: Book) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 表紙画像
            if (book.coverImageUrl != null) {
                AsyncImage(
                    model = book.coverImageUrl,
                    contentDescription = "表紙",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            } else {
                Card(
                    modifier = Modifier.size(120.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Face,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 書籍情報
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleLarge
                )
                book.author?.let { author ->
                    Text(
                        text = author,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                StatusChip(status = book.status)
            }
        }
    }
}

@Composable
private fun StatusChip(status: BookStatus) {
    val (text, color) = when (status) {
        BookStatus.READING -> "読書中" to MaterialTheme.colorScheme.primary
        BookStatus.COMPLETED -> "完了" to Color(0xFF4CAF50)
        BookStatus.WANT_TO_READ -> "読みたい" to MaterialTheme.colorScheme.secondary
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (status == BookStatus.COMPLETED) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

@Composable
private fun PurposeSection(purposes: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "読書目的",
                style = MaterialTheme.typography.titleMedium
            )
            
            if (purposes.isEmpty()) {
                Text(
                    text = "目的が設定されていません",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                purposes.forEach { purpose ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("•", color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = purpose,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StartReadingButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("📚 読書を開始する")
            }
        }
    }
}

@Composable
private fun MemoSection(
    memos: List<Memo>,
    memoCount: Int,
    onAddMemoClick: () -> Unit,
    onCreateActionItemClick: (Memo) -> Unit,
    canCreateActionItem: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ヘッダー
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "メモ",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${memoCount}/10",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (memos.isEmpty()) {
                EmptyMemoCard(onAddMemoClick = onAddMemoClick)
            } else {
                memos.forEach { memo ->
                    MemoCard(
                        memo = memo,
                        onCreateActionItemClick = { onCreateActionItemClick(memo) },
                        canCreateActionItem = canCreateActionItem
                    )
                }
                
                if (memoCount < 10) {
                    Button(
                        onClick = onAddMemoClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("メモを追加")
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMemoCard(onAddMemoClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "まだメモがありません",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "気になった文章を記録してみましょう",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAddMemoClick) {
                Text("最初のメモを作成")
            }
        }
    }
}

@Composable
private fun MemoCard(
    memo: Memo,
    onCreateActionItemClick: () -> Unit,
    canCreateActionItem: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 引用文
            Text(
                text = memo.quote,
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic
            )
            
            Divider()
            
            // 個人コメント
            Text(
                text = memo.comment,
                style = MaterialTheme.typography.bodyMedium
            )
            
            // メタ情報
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = memo.pageNumber?.let { "p.$it" } ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDate(memo.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 実践項目作成ボタン
            if (canCreateActionItem) {
                OutlinedButton(
                    onClick = onCreateActionItemClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("実践項目に追加")
                }
            }
        }
    }
}

@Composable
private fun ActionItemSection(
    actionItems: List<ActionItem>,
    isCompleted: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ヘッダー
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "実践項目",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${actionItems.size}/3",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (actionItems.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "メモから実践項目を作成しましょう",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                actionItems.forEach { item ->
                    ActionItemCard(actionItem = item)
                }
            }
            
            // 完了メッセージ
            if (isCompleted) {
                CompletionCard()
            }
        }
    }
}

@Composable
private fun ActionItemCard(actionItem: ActionItem) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.MailOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = actionItem.actionText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun CompletionCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50)
            )
            Column {
                Text(
                    text = "🎉 読書完了！",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF4CAF50)
                )
                Text(
                    text = "3つの実践項目を見つけました",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4CAF50).copy(alpha = 0.8f)
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val dateTime = java.time.LocalDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(timestamp),
        java.time.ZoneId.systemDefault()
    )
    return dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
}