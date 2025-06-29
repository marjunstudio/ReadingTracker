package com.example.readingtracker.ui.screens.actionitem

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.readingtracker.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionItemCreateScreen(
    navController: NavController,
    bookId: String,
    memoId: String,
    viewModel: ActionItemCreateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(bookId, memoId) {
        viewModel.initialize(bookId, memoId)
    }
    
    // 保存成功時の処理
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "実践項目を作成",
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
            // 元メモ表示
            uiState.memo?.let { memo ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "元のメモ",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = memo.quote,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic
                        )
                        
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        
                        Text(
                            text = memo.comment,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // 説明
            Text(
                text = "このメモから、実際にやってみたいことを具体的に書いてください",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            
            // 例の提示
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "例：",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "メモ: 朝の習慣が重要。5分の瞑想で集中力向上",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "↓",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "実践項目: 平日の朝、起床後に5分間の瞑想を1週間続ける",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // 実践項目入力
            OutlinedTextField(
                value = uiState.actionText,
                onValueChange = viewModel::updateActionText,
                label = { Text("実践したいこと *") },
                placeholder = { Text("具体的で実行可能な行動を書いてください") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                isError = uiState.actionTextError != null,
                supportingText = uiState.actionTextError?.let { { Text(it) } }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 進捗表示
            LinearProgressIndicator(
                progress = (uiState.currentActionCount + 1) / 3f,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "実践項目 ${uiState.currentActionCount + 1}/3",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            // 作成ボタン
            Button(
                onClick = { viewModel.createActionItem() },
                enabled = uiState.canSave && !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("実践項目を作成")
            }
        }
    }
    
    // エラーメッセージの表示
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // TODO: Snackbar or Toast でエラーメッセージを表示
        }
    }
}