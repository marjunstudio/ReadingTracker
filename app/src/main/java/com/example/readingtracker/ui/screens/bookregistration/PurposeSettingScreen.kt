package com.example.readingtracker.ui.screens.bookregistration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.readingtracker.data.model.PurposeCategory
import com.example.readingtracker.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurposeSettingScreen(
    navController: NavController,
    viewModel: PurposeSettingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPurposeDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<PurposeCategory?>(null) }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "読書目的の設定",
                navController = navController,
                showBackButton = true
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 説明メッセージ
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "この本を読む目的を教えてください",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "目的を明確にすることで、必要な情報により敏感になれます",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            
            // 心理的プレッシャー軽減
            Text(
                text = "※後から変更・追加もできます",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            // カテゴリ選択
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(PurposeCategory.values()) { category ->
                    PurposeCategoryItem(
                        category = category,
                        icon = getCategoryIcon(category),
                        onClick = {
                            selectedCategory = category
                            showPurposeDialog = true
                        }
                    )
                }
            }
            
            // 入力済み目的一覧
            if (uiState.purposes.isNotEmpty()) {
                Text(
                    text = "設定した目的",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.purposes.forEach { purpose ->
                        PurposeChip(
                            purpose = purpose,
                            onDelete = { viewModel.deletePurpose(purpose) }
                        )
                    }
                }
            }
            
            // 登録ボタン
            Button(
                onClick = { viewModel.registerBook(navController) },
                enabled = uiState.purposes.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("本を登録する")
            }
        }
    }
    
    // 目的入力ダイアログ
    if (showPurposeDialog && selectedCategory != null) {
        PurposeInputDialog(
            category = selectedCategory!!,
            onConfirm = { purpose ->
                viewModel.addPurpose(purpose)
                showPurposeDialog = false
                selectedCategory = null
            },
            onDismiss = {
                showPurposeDialog = false
                selectedCategory = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PurposeCategoryItem(
    category: PurposeCategory,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getCategoryDisplayName(category),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = getCategoryDescription(category),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PurposeChip(
    purpose: String,
    onDelete: () -> Unit
) {
    InputChip(
        onClick = { },
        label = { Text(purpose) },
        selected = false,
        trailingIcon = {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(18.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "削除",
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    )
}

@Composable
private fun PurposeInputDialog(
    category: PurposeCategory,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("${getCategoryDisplayName(category)}の目的を入力")
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("具体的な目的") },
                placeholder = { Text(getCategoryPlaceholder(category)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onConfirm(text.trim())
                    }
                },
                enabled = text.isNotBlank()
            ) {
                Text("追加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

private fun getCategoryIcon(category: PurposeCategory): ImageVector {
    return when (category) {
        PurposeCategory.SKILL -> Icons.Default.Build
        PurposeCategory.KNOWLEDGE -> Icons.Default.Close
        PurposeCategory.MINDSET -> Icons.Default.Edit
        PurposeCategory.OTHER -> Icons.Default.Star
    }
}

private fun getCategoryDisplayName(category: PurposeCategory): String {
    return when (category) {
        PurposeCategory.SKILL -> "スキルアップ"
        PurposeCategory.KNOWLEDGE -> "知識・教養"
        PurposeCategory.MINDSET -> "考え方・マインドセット"
        PurposeCategory.OTHER -> "その他"
    }
}

private fun getCategoryDescription(category: PurposeCategory): String {
    return when (category) {
        PurposeCategory.SKILL -> "新しいスキルや技術を身につけたい"
        PurposeCategory.KNOWLEDGE -> "教養や知識を深めたい"
        PurposeCategory.MINDSET -> "物事の捉え方や考え方を変えたい"
        PurposeCategory.OTHER -> "上記以外の目的"
    }
}

private fun getCategoryPlaceholder(category: PurposeCategory): String {
    return when (category) {
        PurposeCategory.SKILL -> "プログラミングの基礎を学びたい"
        PurposeCategory.KNOWLEDGE -> "歴史について詳しく知りたい"
        PurposeCategory.MINDSET -> "ポジティブ思考を身につけたい"
        PurposeCategory.OTHER -> "その他の目的を入力してください"
    }
}