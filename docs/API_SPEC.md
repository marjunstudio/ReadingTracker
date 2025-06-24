# API連携仕様書

## API戦略概要

### 目的
書籍情報を効率的に取得し、ユーザーの登録体験を向上させる

### 基本方針
1. **複数API併用**: openBD（日本語書籍）+ Google Books API（洋書・補完）
2. **効率的リクエスト**: ISBN判定による適切なAPI選択
3. **Rate Limiting**: Google Books APIの制限回避
4. **キャッシュ戦略**: 取得データのローカル保存で高速化
5. **エラー耐性**: API障害時のフォールバック対応

---

## 使用API詳細

### 1. openBD（メインAPI - 日本語書籍）

**概要:**
- 日本書籍出版協会が提供する無料API
- 完全無料、リクエスト制限なし
- 日本語書籍の情報が豊富

**エンドポイント:**
```
GET https://api.openbd.jp/v1/get?isbn={isbn}
```

**リクエスト例:**
```
GET https://api.openbd.jp/v1/get?isbn=9784797393651
```

**レスポンス構造:**
```json
[
  {
    "summary": {
      "isbn": "9784797393651",
      "title": "リーダブルコード",
      "author": "Dustin Boswell",
      "publisher": "オライリージャパン",
      "pubdate": "2012-06",
      "cover": "https://cover.openbd.jp/9784797393651.jpg"
    },
    "onix": {
      "DescriptiveDetail": {
        "TitleDetail": {
          "TitleElement": {
            "TitleText": {
              "content": "リーダブルコード"
            }
          }
        },
        "Contributor": [
          {
            "PersonName": {
              "content": "Dustin Boswell"
            }
          }
        ]
      }
    }
  }
]
```

**特徴:**
- 日本語書籍に特化
- 表紙画像URLを提供
- 詳細な書誌情報
- APIキー不要

### 2. Google Books API（サブAPI - 洋書・補完）

**概要:**
- Googleが提供する世界規模の書籍検索API
- 1日1000リクエスト制限（無料枠）
- 洋書に強い、日本語書籍も一部対応

**エンドポイント:**
```
GET https://www.googleapis.com/books/v1/volumes?q=isbn:{isbn}&key={apiKey}
```

**リクエスト例:**
```
GET https://www.googleapis.com/books/v1/volumes?q=isbn:9780596517748&key=YOUR_API_KEY
```

**レスポンス構造:**
```json
{
  "totalItems": 1,
  "items": [
    {
      "id": "keOCAwAAQBAJ",
      "volumeInfo": {
        "title": "The Art of Readable Code",
        "authors": ["Dustin Boswell", "Trevor Foucher"],
        "publisher": "O'Reilly Media",
        "publishedDate": "2011-11-03",
        "description": "As programmers, we've all seen source code...",
        "industryIdentifiers": [
          {
            "type": "ISBN_13",
            "identifier": "9780596517748"
          }
        ],
        "imageLinks": {
          "smallThumbnail": "http://books.google.com/books/content?id=keOCAwAAQBAJ&printsec=frontcover&img=1&zoom=5&source=gbs_api",
          "thumbnail": "http://books.google.com/books/content?id=keOCAwAAQBAJ&printsec=frontcover&img=1&zoom=1&source=gbs_api"
        }
      }
    }
  ]
}
```

**特徴:**
- 世界中の書籍をカバー
- 詳細な説明文
- 複数サイズの表紙画像
- APIキー必要

---

## API選択ロジック

### ISBNによる自動判定

```kotlin
class BookApiSelector {
    
    fun selectPrimaryApi(isbn: String): ApiType {
        return when {
            // 日本語書籍の判定（ISBN-13の国コード）
            isbn.startsWith("978-4") || isbn.startsWith("9784") -> ApiType.OPEN_BD
            // 洋書・その他
            else -> ApiType.GOOGLE_BOOKS
        }
    }
    
    private enum class ApiType {
        OPEN_BD,
        GOOGLE_BOOKS
    }
}
```

### 検索フロー

```kotlin
class BookSearchService {
    
    suspend fun searchByIsbn(isbn: String): BookSearchResult {
        val cleanIsbn = isbn.replace("-", "").trim()
        
        // 1. ローカルキャッシュチェック
        bookDao.findByIsbn(cleanIsbn)?.let { cachedBook ->
            return BookSearchResult.Success(cachedBook)
        }
        
        // 2. API選択
        val isJapanese = cleanIsbn.startsWith("9784")
        
        return if (isJapanese) {
            searchJapaneseBook(cleanIsbn)
        } else {
            searchForeignBook(cleanIsbn)
        }
    }
    
    private suspend fun searchJapaneseBook(isbn: String): BookSearchResult {
        // openBD優先
        openBdApi.searchByIsbn(isbn)?.let { response ->
            val book = response.toBook()
            bookDao.insert(book) // キャッシュ
            return BookSearchResult.Success(book)
        }
        
        // フォールバック: Google Books
        return searchWithGoogleBooks(isbn)
    }
    
    private suspend fun searchForeignBook(isbn: String): BookSearchResult {
        // Google Books優先（Rate Limit適用）
        return searchWithGoogleBooks(isbn)
    }
    
    private suspend fun searchWithGoogleBooks(isbn: String): BookSearchResult {
        return rateLimiter.withLimit(ApiSource.GOOGLE_BOOKS) {
            googleBooksApi.searchByIsbn(isbn)?.let { response ->
                val book = response.toBook()
                bookDao.insert(book) // キャッシュ
                BookSearchResult.Success(book)
            } ?: BookSearchResult.NotFound
        } ?: BookSearchResult.RateLimitExceeded
    }
}
```

---

## Rate Limiting実装

### 自作Rate Limiter

```kotlin
class ApiRateLimiter {
    
    private val googleBooksLastRequest = AtomicLong(0)
    private val requestInterval = 2000L // 2秒間隔（安全マージン含む）
    
    suspend fun <T> withLimit(
        apiSource: ApiSource,
        request: suspend () -> T
    ): T? {
        return when (apiSource) {
            ApiSource.GOOGLE_BOOKS -> withGoogleBooksLimit(request)
            ApiSource.OPEN_BD -> request() // 制限なし
        }
    }
    
    private suspend fun <T> withGoogleBooksLimit(request: suspend () -> T): T? {
        val now = System.currentTimeMillis()
        val lastRequest = googleBooksLastRequest.get()
        
        // 前回リクエストから十分時間が経過していない場合は待機
        val timeSinceLastRequest = now - lastRequest
        if (timeSinceLastRequest < requestInterval) {
            val waitTime = requestInterval - timeSinceLastRequest
            delay(waitTime)
        }
        
        // リクエスト実行前に時刻を更新
        googleBooksLastRequest.set(System.currentTimeMillis())
        
        return try {
            request()
        } catch (e: Exception) {
            // レート制限エラーの場合はnullを返す
            if (e is HttpException && e.code() == 429) {
                null
            } else {
                throw e
            }
        }
    }
}

enum class ApiSource {
    OPEN_BD,
    GOOGLE_BOOKS
}
```

### 1日の制限管理

```kotlin
class DailyRequestTracker {
    
    private val sharedPrefs = context.getSharedPreferences("api_usage", Context.MODE_PRIVATE)
    private val maxDailyRequests = 900 // 安全マージン100リクエスト
    
    fun canMakeRequest(): Boolean {
        val today = getTodayDateString()
        val todayRequests = sharedPrefs.getInt("requests_$today", 0)
        return todayRequests < maxDailyRequests
    }
    
    fun recordRequest() {
        val today = getTodayDateString()
        val currentCount = sharedPrefs.getInt("requests_$today", 0)
        sharedPrefs.edit()
            .putInt("requests_$today", currentCount + 1)
            .apply()
    }
    
    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date())
    }
}
```

---

## API Service実装

### 1. openBD API Service

```kotlin
interface OpenBdApiService {
    
    @GET("v1/get")
    suspend fun getBookByIsbn(
        @Query("isbn") isbn: String
    ): Response<List<OpenBdResponse?>>
}

@Retrofit(baseUrl = "https://api.openbd.jp/")
class OpenBdApiServiceImpl @Inject constructor(
    private val retrofit: Retrofit
) : OpenBdApiService {
    
    private val api = retrofit.create(OpenBdApiService::class.java)
    
    suspend fun searchByIsbn(isbn: String): OpenBdBookData? {
        return try {
            val response = api.getBookByIsbn(isbn)
            if (response.isSuccessful) {
                response.body()?.firstOrNull()?.let { openBdResponse ->
                    openBdResponse.toBookData()
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("OpenBdApi", "Error searching book: ${e.message}")
            null
        }
    }
}
```

### 2. Google Books API Service

```kotlin
interface GoogleBooksApiService {
    
    @GET("books/v1/volumes")
    suspend fun searchByIsbn(
        @Query("q") query: String,
        @Query("key") apiKey: String
    ): Response<GoogleBooksSearchResponse>
}

@Retrofit(baseUrl = "https://www.googleapis.com/")
class GoogleBooksApiServiceImpl @Inject constructor(
    private val retrofit: Retrofit,
    private val rateLimiter: ApiRateLimiter,
    private val requestTracker: DailyRequestTracker
) : GoogleBooksApiService {
    
    private val api = retrofit.create(GoogleBooksApiService::class.java)
    private val apiKey = BuildConfig.GOOGLE_BOOKS_API_KEY
    
    suspend fun searchByIsbn(isbn: String): GoogleBooksBookData? {
        // 1日の制限チェック
        if (!requestTracker.canMakeRequest()) {
            Log.w("GoogleBooksApi", "Daily request limit reached")
            return null
        }
        
        return rateLimiter.withLimit(ApiSource.GOOGLE_BOOKS) {
            try {
                val query = "isbn:$isbn"
                val response = api.searchByIsbn(query, apiKey)
                
                if (response.isSuccessful) {
                    requestTracker.recordRequest()
                    response.body()?.items?.firstOrNull()?.let { item ->
                        item.toBookData()
                    }
                } else {
                    Log.e("GoogleBooksApi", "API error: ${response.code()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("GoogleBooksApi", "Error searching book: ${e.message}")
                null
            }
        }
    }
}
```

---

## データ変換・正規化

### 共通BookDataクラス

```kotlin
data class BookData(
    val title: String,
    val author: String,
    val isbn: String,
    val coverImageUrl: String?,
    val description: String?,
    val publisher: String?,
    val publishedDate: String?,
    val apiSource: ApiSource
)
```

### openBDレスポンス変換

```kotlin
data class OpenBdResponse(
    val summary: OpenBdSummary?,
    val onix: OpenBdOnix?
)

data class OpenBdSummary(
    val isbn: String?,
    val title: String?,
    val author: String?,
    val publisher: String?,
    val pubdate: String?,
    val cover: String?
)

fun OpenBdResponse.toBookData(): BookData? {
    val summary = this.summary ?: return null
    
    return BookData(
        title = summary.title ?: return null,
        author = summary.author ?: "著者不明",
        isbn = summary.isbn ?: return null,
        coverImageUrl = summary.cover,
        description = null, // openBDは概要なし
        publisher = summary.publisher,
        publishedDate = summary.pubdate,
        apiSource = ApiSource.OPEN_BD
    )
}
```

### Google Booksレスポンス変換

```kotlin
data class GoogleBooksSearchResponse(
    val totalItems: Int,
    val items: List<GoogleBooksItem>?
)

data class GoogleBooksItem(
    val id: String,
    val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    val title: String?,
    val authors: List<String>?,
    val publisher: String?,
    val publishedDate: String?,
    val description: String?,
    val industryIdentifiers: List<IndustryIdentifier>?,
    val imageLinks: ImageLinks?
)

data class ImageLinks(
    val smallThumbnail: String?,
    val thumbnail: String?
)

fun GoogleBooksItem.toBookData(): BookData? {
    val volumeInfo = this.volumeInfo
    
    return BookData(
        title = volumeInfo.title ?: return null,
        author = volumeInfo.authors?.joinToString(", ") ?: "著者不明",
        isbn = volumeInfo.industryIdentifiers
            ?.find { it.type == "ISBN_13" }
            ?.identifier ?: return null,
        coverImageUrl = volumeInfo.imageLinks?.thumbnail,
        description = volumeInfo.description,
        publisher = volumeInfo.publisher,
        publishedDate = volumeInfo.publishedDate,
        apiSource = ApiSource.GOOGLE_BOOKS
    )
}
```

---

## エラーハンドリング

### 結果クラス

```kotlin
sealed class BookSearchResult {
    data class Success(val book: Book) : BookSearchResult()
    object NotFound : BookSearchResult()
    object RateLimitExceeded : BookSearchResult()
    object NetworkError : BookSearchResult()
    object DailyLimitExceeded : BookSearchResult()
    data class ApiError(val message: String) : BookSearchResult()
}
```

### エラー処理実装

```kotlin
class BookSearchRepository {
    
    suspend fun searchBook(isbn: String): BookSearchResult {
        return try {
            when {
                !isNetworkAvailable() -> BookSearchResult.NetworkError
                !requestTracker.canMakeRequest() -> BookSearchResult.DailyLimitExceeded
                else -> performSearch(isbn)
            }
        } catch (e: Exception) {
            Log.e("BookSearch", "Unexpected error", e)
            BookSearchResult.ApiError(e.message ?: "不明なエラー")
        }
    }
    
    private suspend fun performSearch(isbn: String): BookSearchResult {
        // 検索ロジック実装
        // ...
        
        return BookSearchResult.NotFound
    }
    
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }
}
```

---

## キャッシュ戦略

### データ永続化

```kotlin
// 取得した書籍データをRoom DBにキャッシュ
@Entity(tableName = "book_cache")
data class BookCache(
    @PrimaryKey val isbn: String,
    val title: String,
    val author: String,
    val coverImageUrl: String?,
    val cachedAt: Long,
    val apiSource: String
)

@Dao
interface BookCacheDao {
    
    @Query("SELECT * FROM book_cache WHERE isbn = :isbn")
    suspend fun findByIsbn(isbn: String): BookCache?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookCache: BookCache)
    
    // 1週間以上古いキャッシュを削除
    @Query("DELETE FROM book_cache WHERE cached_at < :expiredTime")
    suspend fun deleteExpired(expiredTime: Long)
}
```

### キャッシュ有効期限

```kotlin
class BookCacheManager {
    
    private val cacheValidDuration = 7 * 24 * 60 * 60 * 1000L // 1週間
    
    suspend fun getCachedBook(isbn: String): Book? {
        val cached = bookCacheDao.findByIsbn(isbn) ?: return null
        
        val now = System.currentTimeMillis()
        return if (now - cached.cachedAt < cacheValidDuration) {
            cached.toBook()
        } else {
            // 期限切れキャッシュを削除
            bookCacheDao.deleteExpired(now - cacheValidDuration)
            null
        }
    }
    
    suspend fun cacheBook(book: Book) {
        val cache = BookCache(
            isbn = book.isbn ?: return,
            title = book.title,
            author = book.author,
            coverImageUrl = book.coverImageUrl,
            cachedAt = System.currentTimeMillis(),
            apiSource = "api"
        )
        bookCacheDao.insert(cache)
    }
}
```

---

## Hilt DI設定

### NetworkModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                }
            }
            .build()
    }
    
    @Provides
    @Singleton
    @Named("openbd")
    fun provideOpenBdRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openbd.jp/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    @Named("googlebooks")
    fun provideGoogleBooksRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideOpenBdApiService(@Named("openbd") retrofit: Retrofit): OpenBdApiService {
        return retrofit.create(OpenBdApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideGoogleBooksApiService(@Named("googlebooks") retrofit: Retrofit): GoogleBooksApiService {
        return retrofit.create(GoogleBooksApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideApiRateLimiter(): ApiRateLimiter {
        return ApiRateLimiter()
    }
}
```

---

## テスト戦略

### APIレスポンスのモック

```kotlin
// テスト用のモックレスポンス
object MockApiResponses {
    
    val openBdSuccessResponse = """
        [
          {
            "summary": {
              "isbn": "9784797393651",
              "title": "リーダブルコード",
              "author": "Dustin Boswell",
              "cover": "https://cover.openbd.jp/9784797393651.jpg"
            }
          }
        ]
    """.trimIndent()
    
    val googleBooksSuccessResponse = """
        {
          "totalItems": 1,
          "items": [
            {
              "volumeInfo": {
                "title": "The Art of Readable Code",
                "authors": ["Dustin Boswell"],
                "industryIdentifiers": [
                  {
                    "type": "ISBN_13",
                    "identifier": "9780596517748"
                  }
                ]
              }
            }
          ]
        }
    """.trimIndent()
}
```

---

## パフォーマンス最適化

### 画像読み込み最適化

```kotlin
// Coilでの表紙画像キャッシュ
@Singleton
class ImageLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    val coilImageLoader = ImageLoader.Builder(context)
        .memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(0.25) // メモリの25%まで使用
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("image_cache"))
                .maxSizeBytes(50 * 1024 * 1024) // 50MB
                .build()
        }
        .build()
}
```

### バックグラウンド処理

```kotlin
// WorkManagerを使った定期的なキャッシュクリーンアップ
@HiltWorker
class CacheCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val bookCacheDao: BookCacheDao
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            val expiredTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
            bookCacheDao.deleteExpired(expiredTime)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
```

---

この設計により、効率的で信頼性の高い書籍検索機能を実現できます。