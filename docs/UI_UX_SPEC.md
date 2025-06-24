# 画面設計・UX仕様書

## UX設計の基本方針

### 核心価値の体現
「最後まで読まなくても、実践項目3つを見つけたら読書完了」という新しい読書体験を、UI/UXで明確に伝える。

### デザイン原則
1. **シンプルさ重視**: 読書ハードルを感じているユーザーに配慮した直感的UI
2. **成果の可視化**: 小さな成功体験を積み重ねる進捗表示
3. **心理的安心感**: プレッシャーを与えない優しいメッセージング
4. **継続的モチベーション**: 統計とフィードバックで継続をサポート

---

## 画面構成・ナビゲーション

### ナビゲーション構造
```
Bottom Navigation（4タブ）
├── Home（本棚）           - メイン画面
├── Statistics（統計）      - 読書成果確認  
├── Search（検索）※将来    - 書籍検索・発見
└── Profile（設定）※将来   - 設定・プロフィール

Modal Navigation
├── BookRegistration      - 書籍登録フロー
├── BookDetail           - 書籍詳細・メモ管理
├── MemoAdd             - メモ追加
└── ActionItemCreate    - 実践項目作成
```

### 画面遷移フロー
```
Home → BookRegistration → BookDetail → MemoAdd → ActionItemCreate
  ↓           ↓              ↓           ↓           ↓
Statistics ←←←←←←←←←←←←←←← 完了 ←←←←←←←←←←←←←←←←
```

---

## 1. ホーム画面（本棚）

### レイアウト構成
```
[TopAppBar]
├── アプリタイトル「Reading Tracker」  
├── 統計サマリー（今月読了数）
└── 検索アイコン（将来の拡張）

[Content]
├── 読書中セクション
│   ├── セクションタイトル「読書中の本」
│   └── 書籍カード一覧（縦スクロール）
├── 完了セクション  
│   ├── セクションタイトル「完了した本」
│   └── 完了書籍カード一覧
└── 空状態表示（書籍なし時）

[FloatingActionButton]
└── 「+」アイコン（書籍登録）
```

### 書籍カード設計
```kotlin
BookCard {
    Row {
        // 表紙画像（または代替アイコン）
        AsyncImage(
            model = book.coverImageUrl,
            modifier = Modifier.size(80.dp)
        )
        
        Column {
            // タイトル・著者
            Text(book.title, style = MaterialTheme.typography.titleMedium)
            Text(book.author, style = MaterialTheme.typography.bodyMedium)
            
            // 進捗表示
            ProgressSection {
                // メモ進捗バー
                LinearProgressIndicator(
                    progress = memoCount / 10f,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("メモ ${memoCount}/10")
                
                // 実践項目進捗バー  
                LinearProgressIndicator(
                    progress = actionItemCount / 3f,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("実践項目 ${actionItemCount}/3")
            }
            
            // ステータス表示
            if (book.status == COMPLETED) {
                Icon(Icons.Default.CheckCircle, tint = Color.Green)
                Text("完了", color = Color.Green)
            }
        }
    }
}
```

### UX配慮点
- **視覚的な成果**: 進捗バーで「読書が進んでいる」実感を提供
- **達成感**: 完了本にはチェックマークと色分けで達成感演出
- **空状態**: 「最初の1冊を登録してみましょう」優しいメッセージ

---

## 2. 書籍登録画面

### 登録方法選択画面
```
[TopAppBar]
└── 「書籍を登録」

[Content]
├── 登録方法選択
│   ├── バーコードスキャン（推奨）
│   │   ├── カメラアイコン
│   │   └── 「バーコードをスキャン」
│   ├── ISBN入力
│   │   ├── キーボードアイコン  
│   │   └── 「ISBNを入力」
│   └── 手動入力
│       ├── エディットアイコン
│       └── 「タイトル・著者を入力」
└── ヘルプテキスト
    └── 「本の裏表紙のバーコードをスキャンすると簡単です」
```

### バーコードスキャン画面
```kotlin
BarcodeScanner {
    CameraPreview(
        modifier = Modifier.fillMaxSize()
    )
    
    Overlay {
        // スキャンガイド
        Box(
            modifier = Modifier
                .size(250.dp)
                .border(2.dp, Color.White)
        )
        
        Text(
            text = "バーコードを枠内に合わせてください",
            color = Color.White,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
    
    // 手動入力への切り替え
    TextButton(
        onClick = { /* 手動入力画面へ */ },
        modifier = Modifier.align(Alignment.BottomCenter)
    ) {
        Text("手動で入力", color = Color.White)
    }
}
```

### 書籍情報確認画面
```kotlin
BookConfirmation {
    Column {
        // 書籍情報表示
        BookInfoCard {
            AsyncImage(book.coverImageUrl)
            Text(book.title)
            Text(book.author)
            if (book.isbn != null) {
                Text("ISBN: ${book.isbn}")
            }
        }
        
        // 確認メッセージ
        Text(
            "この本で間違いありませんか？",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // アクションボタン
        Button(
            onClick = { /* 目的設定画面へ */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("はい、この本です")
        }
        
        OutlinedButton(
            onClick = { /* 検索画面に戻る */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("別の本を探す")
        }
    }
}
```

### 目的設定画面（重要なUX）
```kotlin
PurposeSettingScreen {
    Column {
        // 説明メッセージ
        Card {
            Text(
                "この本を読む目的を教えてください",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "目的を明確にすることで、必要な情報により敏感になれます",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        // 心理的プレッシャー軽減
        Text(
            "※後から変更・追加もできます",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // カテゴリ選択
        LazyColumn {
            items(PurposeCategory.values()) { category ->
                PurposeCategoryItem(
                    category = category,
                    icon = getCategoryIcon(category),
                    onClick = { showPurposeInputDialog(category) }
                )
            }
        }
        
        // 入力済み目的一覧
        if (purposes.isNotEmpty()) {
            Text("設定した目的")
            purposes.forEach { purpose ->
                PurposeChip(
                    purpose = purpose,
                    onDelete = { deletePurpose(purpose) }
                )
            }
        }
        
        // 登録ボタン
        Button(
            onClick = { registerBook() },
            enabled = purposes.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("本を登録する")
        }
    }
}

// カテゴリアイコンマッピング
fun getCategoryIcon(category: PurposeCategory): ImageVector {
    return when (category) {
        WORK -> Icons.Default.Work
        MINDSET -> Icons.Default.Psychology  
        SKILL_UP -> Icons.Default.TrendingUp
        ACTION -> Icons.Default.Target
        INTEREST -> Icons.Default.QuestionMark
    }
}
```

---

## 3. 書籍詳細画面

### レイアウト構成
```kotlin
BookDetailScreen {
    LazyColumn {
        // 書籍情報ヘッダー
        item {
            BookHeader {
                AsyncImage(book.coverImageUrl)
                Column {
                    Text(book.title)
                    Text(book.author)
                    StatusChip(book.status)
                }
            }
        }
        
        // 読書目的セクション
        item {
            PurposeSection {
                Text("読書目的")
                purposes.forEach { purpose ->
                    PurposeItem(purpose)
                }
            }
        }
        
        // 読書開始ボタン（初回のみ）
        if (book.startedAt == null) {
            item {
                StartReadingButton {
                    Text("📚 読書を開始する")
                }
            }
        }
        
        // メモセクション
        item {
            MemoSection {
                SectionHeader {
                    Text("メモ")
                    Text("${memos.size}/10")
                    AddMemoButton()
                }
                
                if (memos.isEmpty()) {
                    EmptyMemoCard {
                        Text("まだメモがありません")
                        Text("気になった文章を記録してみましょう")
                    }
                } else {
                    memos.forEach { memo ->
                        MemoCard(memo)
                    }
                }
            }
        }
        
        // 実践項目セクション
        item {
            ActionItemSection {
                SectionHeader {
                    Text("実践項目")
                    Text("${actionItems.size}/3")
                }
                
                if (actionItems.isEmpty()) {
                    EmptyActionCard {
                        Text("メモから実践項目を作成しましょう")
                    }
                } else {
                    actionItems.forEach { item ->
                        ActionItemCard(item)
                    }
                }
                
                // 完了メッセージ
                if (actionItems.size == 3) {
                    CompletionCard {
                        Icon(Icons.Default.Celebration)
                        Text("🎉 読書完了！")
                        Text("3つの実践項目を見つけました")
                    }
                }
            }
        }
    }
}
```

### メモカード設計
```kotlin
MemoCard {
    Card {
        Column {
            // 引用文
            Text(
                memo.quote,
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic
            )
            
            Divider()
            
            // 個人コメント
            Text(
                memo.comment,
                style = MaterialTheme.typography.bodyMedium
            )
            
            // メタ情報
            Row {
                if (memo.pageNumber != null) {
                    Text("p.${memo.pageNumber}")
                }
                Spacer(Modifier.weight(1f))
                Text(formatDate(memo.createdAt))
            }
            
            // 実践項目作成ボタン
            if (actionItems.size < 3) {
                OutlinedButton(
                    onClick = { createActionItem(memo) }
                ) {
                    Text("実践項目に追加")
                }
            }
        }
    }
}
```

---

## 4. メモ追加画面

### フォーム設計
```kotlin
MemoAddScreen {
    Column {
        // 説明
        Text("印象に残った文章とあなたの感想を記録しましょう")
        
        // 引用文入力
        OutlinedTextField(
            value = quote,
            onValueChange = { quote = it },
            label = { Text("引用文 *") },
            placeholder = { Text("本から印象的だった文章を入力") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        // 個人コメント入力
        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("あなたの感想 *") },
            placeholder = { Text("この文章についてどう思いましたか？") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        // ページ番号（任意）
        OutlinedTextField(
            value = pageNumber,
            onValueChange = { pageNumber = it },
            label = { Text("ページ番号（任意）") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 保存ボタン
        Button(
            onClick = { saveMemo() },
            enabled = quote.isNotBlank() && comment.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("メモを保存")
        }
        
        // 制限表示
        Text(
            "あと${10 - currentMemoCount}件メモを追加できます",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
```

---

## 5. 実践項目作成画面

### 変換フロー重視の設計
```kotlin
ActionItemCreateScreen {
    Column {
        // 元メモ表示
        Card {
            Text("元のメモ")
            Text(memo.quote, fontStyle = FontStyle.Italic)
            Text(memo.comment)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 説明
        Text(
            "このメモから、実際にやってみたいことを具体的に書いてください",
            style = MaterialTheme.typography.titleMedium
        )
        
        // 例の提示
        Card {
            Text("例：")
            Text("メモ: 朝の習慣が重要。5分の瞑想で集中力向上")
            Text("↓")
            Text("実践項目: 平日の朝、起床後に5分間の瞑想を1週間続ける")
        }
        
        // 実践項目入力
        OutlinedTextField(
            value = actionText,
            onValueChange = { actionText = it },
            label = { Text("実践したいこと") },
            placeholder = { Text("具体的で実行可能な行動を書いてください") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        // 作成ボタン
        Button(
            onClick = { createActionItem() },
            enabled = actionText.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("実践項目を作成")
        }
        
        // 進捗表示
        LinearProgressIndicator(
            progress = currentActionCount / 3f,
            modifier = Modifier.fillMaxWidth()
        )
        Text("実践項目 ${currentActionCount + 1}/3")
    }
}
```

---

## 6. 統計画面

### 成果可視化重視
```kotlin
StatisticsScreen {
    LazyColumn {
        // 月次切り替え
        item {
            MonthSelector {
                IconButton(onClick = { previousMonth() }) {
                    Icon(Icons.Default.ChevronLeft)
                }
                Text("${selectedYear}年${selectedMonth}月")
                IconButton(onClick = { nextMonth() }) {
                    Icon(Icons.Default.ChevronRight)
                }
            }
        }
        
        // 主要統計カード
        item {
            StatisticsCard {
                Row {
                    StatItem {
                        Text("${completedBooks}冊", style = typography.headlineMedium)
                        Text("読了数")
                    }
                    StatItem {
                        Text("${totalMemos}個", style = typography.headlineMedium)
                        Text("総メモ数")
                    }
                }
            }
        }
        
        // 励ましメッセージ
        item {
            MotivationCard {
                when {
                    completedBooks == 0 -> Text("最初の1冊を完了させましょう！")
                    completedBooks < 5 -> Text("いいペースです！読書習慣が身についてきていますね")
                    else -> Text("素晴らしい！読書マスターですね🎉")
                }
            }
        }
        
        // 完了本一覧
        item {
            CompletedBooksSection {
                Text("完了した本")
                completedBooks.forEach { book ->
                    CompletedBookItem(book)
                }
            }
        }
    }
}
```

---

## バリデーション・エラーハンドリング

### 入力バリデーション（View層）
```kotlin
// 文字数制限
TextField(
    value = text,
    onValueChange = { if (it.length <= 500) text = it },
    supportingText = { Text("${text.length}/500") }
)

// 必須項目チェック
Button(
    enabled = quote.isNotBlank() && comment.isNotBlank()
) { Text("保存") }
```

### ビジネスルールバリデーション（ViewModel層）
```kotlin
fun addMemo(): ValidationResult {
    return when {
        quote.trim().isEmpty() -> ValidationResult.Error("引用文を入力してください")
        comment.trim().isEmpty() -> ValidationResult.Error("感想を入力してください")
        memos.size >= 10 -> ValidationResult.Error("メモは最大10個までです")
        else -> {
            // 正常処理
            ValidationResult.Success
        }
    }
}
```

### エラー表示（Toast統一）
```kotlin
// ViewModel
private val _toastMessage = MutableSharedFlow<String>()
val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

fun showError(message: String) {
    viewModelScope.launch {
        _toastMessage.emit(message)
    }
}

// Compose
val toastMessage by viewModel.toastMessage.collectAsState()
LaunchedEffect(toastMessage) {
    if (toastMessage.isNotEmpty()) {
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
    }
}
```

---

## アクセシビリティ対応

### 基本対応
```kotlin
// セマンティクス情報
Button(
    modifier = Modifier.semantics {
        contentDescription = "書籍を登録する"
    }
) { Text("登録") }

// コントラスト確保
Text(
    color = MaterialTheme.colorScheme.onSurface,
    style = MaterialTheme.typography.bodyLarge
)

// フォーカス管理
TextField(
    modifier = Modifier.focusRequester(focusRequester)
)
```

---

## マテリアルデザイン3対応

### カラーシステム
```kotlin
// 読書アプリに適したカラー
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),        // 学習・知識を表す紫
    secondary = Color(0xFF625B71),      // 落ち着いた読書環境
    success = Color(0xFF4CAF50),        // 完了・達成感
    warning = Color(0xFFFF9800)         // 注意・制限の警告
)
```

### タイポグラフィ
```kotlin
// 読みやすさ重視
val Typography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
)
```

---

この設計により、ユーザーが「読書のハードルを下げながら、実践的な成果を得られる」体験を提供できます。