# データベース設計仕様書

## 概要

### データベース構成
- **使用技術**: Room Database (SQLite)
- **設計方針**: 将来のクラウドDB移行を考慮
- **主キー**: UUID文字列（重複回避）
- **タイムスタンプ**: Long型（エポック秒）で同期対応

---

## Entity設計

### 1. Book Entity（書籍情報）

```kotlin
@Entity(tableName = "books")
data class Book(
    @PrimaryKey 
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "author")
    val author: String,
    
    @ColumnInfo(name = "isbn")
    val isbn: String? = null,
    
    @ColumnInfo(name = "cover_image_url")
    val coverImageUrl: String? = null,
    
    @ColumnInfo(name = "status")
    val status: BookStatus,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "started_at")
    val startedAt: Long? = null,
    
    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,
    
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis()
)

enum class BookStatus {
    READING,    // 読書中
    COMPLETED   // 完了
}
```

**フィールド説明:**
- `id`: 一意識別子（UUID）
- `title`: 書籍タイトル（API取得またはユーザー入力）
- `author`: 著者名
- `isbn`: ISBN番号（任意、API検索に使用）
- `coverImageUrl`: 表紙画像URL（API取得）
- `status`: 読書ステータス
- `createdAt`: 登録日時
- `startedAt`: 読書開始日時（「読書を開始する」ボタン押下時）
- `completedAt`: 読書完了日時（実践項目3つ達成時）
- `cachedAt`: API情報取得日時

### 2. ReadingPurpose Entity（読書目的）

```kotlin
@Entity(
    tableName = "reading_purposes",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["book_id"])]
)
data class ReadingPurpose(
    @PrimaryKey 
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "book_id")
    val bookId: String,
    
    @ColumnInfo(name = "purpose")
    val purpose: String,
    
    @ColumnInfo(name = "category")
    val category: PurposeCategory,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

enum class PurposeCategory {
    WORK,        // 💼 仕事に活かしたい
    MINDSET,     // 🧠 考え方を変えたい
    SKILL_UP,    // 📈 スキルアップしたい
    ACTION,      // 🎯 具体的な行動を見つけたい
    INTEREST     // ❓ なんとなく興味がある
}
```

**ビジネス制約:**
- 書籍ごとに最低1件、最大3件
- 削除は書籍削除時に連動（CASCADE）

### 3. Memo Entity（メモ）

```kotlin
@Entity(
    tableName = "memos",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["book_id"])]
)
data class Memo(
    @PrimaryKey 
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "book_id")
    val bookId: String,
    
    @ColumnInfo(name = "quote")
    val quote: String,
    
    @ColumnInfo(name = "comment")
    val comment: String,
    
    @ColumnInfo(name = "page_number")
    val pageNumber: Int? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
```

**フィールド説明:**
- `quote`: 引用文（書籍からの抜粋）
- `comment`: 個人的なコメント・感想
- `pageNumber`: ページ番号（任意）

**ビジネス制約:**
- 書籍ごとに最大10件
- quote、comment両方必須

### 4. ActionItem Entity（実践項目）

```kotlin
@Entity(
    tableName = "action_items",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Memo::class,
            parentColumns = ["id"],
            childColumns = ["memo_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["book_id"]),
        Index(value = ["memo_id"])
    ]
)
data class ActionItem(
    @PrimaryKey 
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "book_id")
    val bookId: String,
    
    @ColumnInfo(name = "memo_id")
    val memoId: String,
    
    @ColumnInfo(name = "action_text")
    val actionText: String,
    
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
```

**フィールド説明:**
- `memoId`: 元となったメモのID（トレーサビリティ）
- `actionText`: 実践する具体的な行動
- `isCompleted`: 実践完了フラグ（将来の拡張用）

**ビジネス制約:**
- 書籍ごとに最大3件
- 3件達成時に書籍ステータスが自動的にCOMPLETEDに変更

---

## DAO（Data Access Object）設計

### 1. BookDao

```kotlin
@Dao
interface BookDao {
    
    // 基本CRUD操作
    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun findById(id: String): Book?
    
    @Query("SELECT * FROM books WHERE isbn = :isbn")
    suspend fun findByIsbn(isbn: String): Book?
    
    @Insert
    suspend fun insert(book: Book)
    
    @Update
    suspend fun update(book: Book)
    
    @Delete
    suspend fun delete(book: Book)
    
    // ステータス別取得
    @Query("SELECT * FROM books WHERE status = :status ORDER BY created_at DESC")
    fun getBooksByStatus(status: BookStatus): Flow<List<Book>>
    
    @Query("SELECT * FROM books WHERE status = 'READING' ORDER BY created_at DESC")
    fun getReadingBooks(): Flow<List<Book>>
    
    @Query("SELECT * FROM books WHERE status = 'COMPLETED' ORDER BY completed_at DESC")
    fun getCompletedBooks(): Flow<List<Book>>
    
    // 統計用クエリ
    @Query("SELECT COUNT(*) FROM books WHERE status = 'COMPLETED'")
    suspend fun getCompletedBooksCount(): Int
    
    @Query("""
        SELECT COUNT(*) FROM books 
        WHERE status = 'COMPLETED' 
        AND completed_at BETWEEN :startOfMonth AND :endOfMonth
    """)
    suspend fun getCompletedBooksInMonth(startOfMonth: Long, endOfMonth: Long): Int
    
    // 読書開始
    @Query("UPDATE books SET started_at = :startedAt WHERE id = :bookId")
    suspend fun updateStartedAt(bookId: String, startedAt: Long)
    
    // 読書完了
    @Query("UPDATE books SET status = 'COMPLETED', completed_at = :completedAt WHERE id = :bookId")
    suspend fun completeBook(bookId: String, completedAt: Long)
}
```

### 2. ReadingPurposeDao

```kotlin
@Dao
interface ReadingPurposeDao {
    
    @Query("SELECT * FROM reading_purposes WHERE book_id = :bookId ORDER BY created_at ASC")
    suspend fun getPurposesByBookId(bookId: String): List<ReadingPurpose>
    
    @Query("SELECT * FROM reading_purposes WHERE book_id = :bookId ORDER BY created_at ASC")
    fun getPurposesByBookIdFlow(bookId: String): Flow<List<ReadingPurpose>>
    
    @Query("SELECT COUNT(*) FROM reading_purposes WHERE book_id = :bookId")
    suspend fun getPurposeCount(bookId: String): Int
    
    @Insert
    suspend fun insert(purpose: ReadingPurpose)
    
    @Insert
    suspend fun insertAll(purposes: List<ReadingPurpose>)
    
    @Update
    suspend fun update(purpose: ReadingPurpose)
    
    @Delete
    suspend fun delete(purpose: ReadingPurpose)
    
    @Query("DELETE FROM reading_purposes WHERE book_id = :bookId")
    suspend fun deleteByBookId(bookId: String)
}
```

### 3. MemoDao

```kotlin
@Dao
interface MemoDao {
    
    @Query("SELECT * FROM memos WHERE book_id = :bookId ORDER BY created_at DESC")
    suspend fun getMemosByBookId(bookId: String): List<Memo>
    
    @Query("SELECT * FROM memos WHERE book_id = :bookId ORDER BY created_at DESC")
    fun getMemosByBookIdFlow(bookId: String): Flow<List<Memo>>
    
    @Query("SELECT COUNT(*) FROM memos WHERE book_id = :bookId")
    suspend fun getMemoCount(bookId: String): Int
    
    @Query("SELECT COUNT(*) FROM memos")
    suspend fun getTotalMemoCount(): Int
    
    @Query("""
        SELECT COUNT(*) FROM memos 
        WHERE created_at BETWEEN :startOfMonth AND :endOfMonth
    """)
    suspend fun getMemoCountInMonth(startOfMonth: Long, endOfMonth: Long): Int
    
    @Insert
    suspend fun insert(memo: Memo)
    
    @Update
    suspend fun update(memo: Memo)
    
    @Delete
    suspend fun delete(memo: Memo)
    
    @Query("SELECT * FROM memos WHERE id = :id")
    suspend fun findById(id: String): Memo?
}
```

### 4. ActionItemDao

```kotlin
@Dao
interface ActionItemDao {
    
    @Query("SELECT * FROM action_items WHERE book_id = :bookId ORDER BY created_at DESC")
    suspend fun getActionItemsByBookId(bookId: String): List<ActionItem>
    
    @Query("SELECT * FROM action_items WHERE book_id = :bookId ORDER BY created_at DESC")
    fun getActionItemsByBookIdFlow(bookId: String): Flow<List<ActionItem>>
    
    @Query("SELECT COUNT(*) FROM action_items WHERE book_id = :bookId")
    suspend fun getActionItemCount(bookId: String): Int
    
    @Insert
    suspend fun insert(actionItem: ActionItem)
    
    @Update
    suspend fun update(actionItem: ActionItem)
    
    @Delete
    suspend fun delete(actionItem: ActionItem)
    
    // メモとの関連
    @Query("""
        SELECT ai.*, m.quote, m.comment 
        FROM action_items ai 
        INNER JOIN memos m ON ai.memo_id = m.id 
        WHERE ai.book_id = :bookId 
        ORDER BY ai.created_at DESC
    """)
    suspend fun getActionItemsWithMemo(bookId: String): List<ActionItemWithMemo>
}

// 結合結果用データクラス
data class ActionItemWithMemo(
    @Embedded val actionItem: ActionItem,
    @ColumnInfo(name = "quote") val quote: String,
    @ColumnInfo(name = "comment") val comment: String
)
```

---

## Database設定

### AppDatabase クラス

```kotlin
@Database(
    entities = [
        Book::class,
        ReadingPurpose::class,
        Memo::class,
        ActionItem::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun bookDao(): BookDao
    abstract fun readingPurposeDao(): ReadingPurposeDao
    abstract fun memoDao(): MemoDao
    abstract fun actionItemDao(): ActionItemDao
    
    companion object {
        const val DATABASE_NAME = "reading_tracker_database"
    }
}
```

### Type Converters

```kotlin
class Converters {
    
    @TypeConverter
    fun fromBookStatus(status: BookStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toBookStatus(status: String): BookStatus {
        return BookStatus.valueOf(status)
    }
    
    @TypeConverter
    fun fromPurposeCategory(category: PurposeCategory): String {
        return category.name
    }
    
    @TypeConverter
    fun toPurposeCategory(category: String): PurposeCategory {
        return PurposeCategory.valueOf(category)
    }
}
```

---

## ビジネスロジック制約

### データ整合性チェック

```kotlin
// Repository層での制約チェック例
class BookRepositoryImpl {
    
    suspend fun addMemo(bookId: String, quote: String, comment: String, pageNumber: Int?): Result<Memo> {
        // 1. メモ数制限チェック
        val currentMemoCount = memoDao.getMemoCount(bookId)
        if (currentMemoCount >= 10) {
            return Result.failure(IllegalStateException("メモは最大10件です"))
        }
        
        // 2. 書籍存在チェック
        val book = bookDao.findById(bookId) 
            ?: return Result.failure(IllegalArgumentException("書籍が見つかりません"))
        
        // 3. 書籍ステータスチェック
        if (book.status != BookStatus.READING) {
            return Result.failure(IllegalStateException("読書中の書籍にのみメモを追加できます"))
        }
        
        // 4. データ保存
        val memo = Memo(
            bookId = bookId,
            quote = quote.trim(),
            comment = comment.trim(),
            pageNumber = pageNumber
        )
        memoDao.insert(memo)
        return Result.success(memo)
    }
    
    suspend fun createActionItem(bookId: String, memoId: String, actionText: String): Result<ActionItem> {
        // 1. 実践項目数制限チェック
        val currentActionCount = actionItemDao.getActionItemCount(bookId)
        if (currentActionCount >= 3) {
            return Result.failure(IllegalStateException("実践項目は最大3件です"))
        }
        
        // 2. メモ存在チェック
        val memo = memoDao.findById(memoId)
            ?: return Result.failure(IllegalArgumentException("メモが見つかりません"))
        
        // 3. データ保存
        val actionItem = ActionItem(
            bookId = bookId,
            memoId = memoId,
            actionText = actionText.trim()
        )
        actionItemDao.insert(actionItem)
        
        // 4. 3件達成時の自動完了処理
        if (currentActionCount + 1 == 3) {
            bookDao.completeBook(bookId, System.currentTimeMillis())
        }
        
        return Result.success(actionItem)
    }
}
```

---

## インデックス設計

### パフォーマンス最適化

```kotlin
// 自動生成されるインデックス（Foreign Key）
- reading_purposes.book_id
- memos.book_id  
- action_items.book_id
- action_items.memo_id

// 追加検討インデックス（必要に応じて）
@Entity(indices = [
    Index(value = ["status", "created_at"]),  // ステータス別ソート用
    Index(value = ["isbn"])                   // ISBN検索用
])
```

---

## 将来の拡張対応

### マイグレーション準備
```kotlin
// version 2への移行例（将来の機能追加時）
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 新機能追加時のマイグレーション処理
    }
}
```

### クラウドDB移行対応
- UUID主キーにより、複数デバイス間でのデータ重複回避
- created_at フィールドによる同期処理対応
- 外部キー制約により、データ整合性保証

---

この設計により、効率的で拡張性の高いデータベース層を構築できます。