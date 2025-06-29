package com.example.readingtracker.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.readingtracker.data.database.entity.Book
import com.example.readingtracker.data.model.BookStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookCard(
    book: Book,
    memoCount: Int,
    actionItemCount: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 表紙画像
            if (book.coverImageUrl != null) {
                AsyncImage(
                    model = book.coverImageUrl,
                    contentDescription = "表紙",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Crop
                )
            } else {
                Card(
                    modifier = Modifier.size(80.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 書籍情報
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // タイトル・著者
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                book.author?.let { author ->
                    Text(
                        text = author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 進捗表示
                Spacer(modifier = Modifier.height(4.dp))
                ProgressSection(
                    memoCount = memoCount,
                    actionItemCount = actionItemCount
                )

                // ステータス表示
                if (book.status == BookStatus.COMPLETED) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF4CAF50)
                        )
                        Text(
                            text = "完了",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressSection(
    memoCount: Int,
    actionItemCount: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // メモ進捗
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LinearProgressIndicator(
                progress = memoCount / 10f,
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
            )
            Text(
                text = "メモ $memoCount/10",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 実践項目進捗
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LinearProgressIndicator(
                progress = actionItemCount / 3f,
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.tertiaryContainer
            )
            Text(
                text = "実践項目 $actionItemCount/3",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}