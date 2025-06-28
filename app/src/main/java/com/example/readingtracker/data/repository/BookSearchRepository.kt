package com.example.readingtracker.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.example.readingtracker.BuildConfig
import com.example.readingtracker.data.api.model.*
import com.example.readingtracker.data.api.ratelimit.ApiRateLimiter
import com.example.readingtracker.data.api.ratelimit.DailyRequestTracker
import com.example.readingtracker.data.api.service.GoogleBooksApiService
import com.example.readingtracker.data.api.service.OpenBdApiService
import com.example.readingtracker.data.database.dao.BookCacheDao
import com.example.readingtracker.data.database.entity.BookCache
import com.example.readingtracker.data.model.BookSearchResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookSearchRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val openBdApiService: OpenBdApiService,
    private val googleBooksApiService: GoogleBooksApiService,
    private val bookCacheDao: BookCacheDao,
    private val rateLimiter: ApiRateLimiter,
    private val requestTracker: DailyRequestTracker
) {
    
    private val cacheValidDuration = 7 * 24 * 60 * 60 * 1000L // 1週間
    
    suspend fun searchByIsbn(isbn: String): BookSearchResult = withContext(Dispatchers.IO) {
        try {
            // ネットワーク接続確認
            if (!isNetworkAvailable()) {
                return@withContext BookSearchResult.NetworkError
            }
            
            val cleanIsbn = isbn.replace("-", "").trim()
            
            // 1. ローカルキャッシュチェック
            getCachedBook(cleanIsbn)?.let { bookData ->
                return@withContext BookSearchResult.Success(bookData)
            }
            
            // 2. API選択
            val isJapanese = cleanIsbn.startsWith("9784")
            
            if (isJapanese) {
                searchJapaneseBook(cleanIsbn)
            } else {
                searchForeignBook(cleanIsbn)
            }
        } catch (e: Exception) {
            Log.e("BookSearchRepository", "Unexpected error", e)
            BookSearchResult.ApiError(e.message ?: "不明なエラー")
        }
    }
    
    private suspend fun getCachedBook(isbn: String): BookData? {
        val cached = bookCacheDao.findByIsbn(isbn) ?: return null
        
        val now = System.currentTimeMillis()
        return if (now - cached.cachedAt < cacheValidDuration) {
            BookData(
                title = cached.title,
                author = cached.author,
                isbn = cached.isbn,
                coverImageUrl = cached.coverImageUrl,
                description = cached.description,
                publisher = cached.publisher,
                publishedDate = cached.publishedDate,
                apiSource = ApiSource.valueOf(cached.apiSource)
            )
        } else {
            // 期限切れキャッシュを削除
            bookCacheDao.deleteExpired(now - cacheValidDuration)
            null
        }
    }
    
    private suspend fun searchJapaneseBook(isbn: String): BookSearchResult {
        // openBD優先
        try {
            val response = openBdApiService.getBookByIsbn(isbn)
            if (response.isSuccessful) {
                response.body()?.firstOrNull()?.toBookData()?.let { bookData ->
                    cacheBook(bookData)
                    return BookSearchResult.Success(bookData)
                }
            }
        } catch (e: Exception) {
            Log.e("BookSearchRepository", "OpenBD API error", e)
        }
        
        // フォールバック: Google Books
        return searchWithGoogleBooks(isbn)
    }
    
    private suspend fun searchForeignBook(isbn: String): BookSearchResult {
        // Google Books優先（Rate Limit適用）
        return searchWithGoogleBooks(isbn)
    }
    
    private suspend fun searchWithGoogleBooks(isbn: String): BookSearchResult {
        // 1日の制限チェック
        if (!requestTracker.canMakeRequest()) {
            return BookSearchResult.DailyLimitExceeded
        }
        
        return rateLimiter.withLimit(ApiSource.GOOGLE_BOOKS) {
            try {
                val query = "isbn:$isbn"
                val apiKey = BuildConfig.GOOGLE_BOOKS_API_KEY
                val response = googleBooksApiService.searchByIsbn(query, apiKey)
                
                if (response.isSuccessful) {
                    requestTracker.recordRequest()
                    response.body()?.items?.firstOrNull()?.toBookData()?.let { bookData ->
                        cacheBook(bookData)
                        BookSearchResult.Success(bookData)
                    } ?: BookSearchResult.NotFound
                } else {
                    Log.e("BookSearchRepository", "Google Books API error: ${response.code()}")
                    BookSearchResult.ApiError("API error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("BookSearchRepository", "Google Books search error", e)
                BookSearchResult.ApiError(e.message ?: "不明なエラー")
            }
        } ?: BookSearchResult.RateLimitExceeded
    }
    
    private suspend fun cacheBook(bookData: BookData) {
        val cache = BookCache(
            isbn = bookData.isbn,
            title = bookData.title,
            author = bookData.author,
            coverImageUrl = bookData.coverImageUrl,
            description = bookData.description,
            publisher = bookData.publisher,
            publishedDate = bookData.publishedDate,
            cachedAt = System.currentTimeMillis(),
            apiSource = bookData.apiSource.name
        )
        bookCacheDao.insert(cache)
    }
    
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo?.isConnected == true
        }
    }
}