package com.example.readingtracker.ui.screens.memo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoAddScreen(
    navController: NavController,
    bookId: String,
    viewModel: MemoAddViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(bookId) {
        viewModel.initialize(bookId)
    }
    
    // 保存成功時の処理
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("メモを追加") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                }
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
            // 説明
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "印象に残った文章とあなたの感想を記録しましょう",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // 引用文入力
            OutlinedTextField(
                value = uiState.quote,
                onValueChange = viewModel::updateQuote,
                label = { Text("引用文 *") },
                placeholder = { Text("本から印象的だった文章を入力") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                isError = uiState.quoteError != null,
                supportingText = uiState.quoteError?.let { { Text(it) } }
            )
            
            // 個人コメント入力
            OutlinedTextField(
                value = uiState.comment,
                onValueChange = viewModel::updateComment,
                label = { Text("あなたの感想 *") },
                placeholder = { Text("この文章についてどう思いましたか？") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                isError = uiState.commentError != null,
                supportingText = uiState.commentError?.let { { Text(it) } }
            )
            
            // ページ番号（任意）
            OutlinedTextField(
                value = uiState.pageNumber,
                onValueChange = viewModel::updatePageNumber,
                label = { Text("ページ番号（任意）") },
                placeholder = { Text("例: 123") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.pageNumberError != null,
                supportingText = uiState.pageNumberError?.let { { Text(it) } }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 制限表示
            Text(
                text = "あと${10 - uiState.currentMemoCount}件メモを追加できます",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // 保存ボタン
            Button(
                onClick = { viewModel.saveMemo() },
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
                Text("メモを保存")
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