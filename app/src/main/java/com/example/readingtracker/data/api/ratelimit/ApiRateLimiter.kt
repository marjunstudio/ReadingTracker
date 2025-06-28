package com.example.readingtracker.data.api.ratelimit

import com.example.readingtracker.data.api.model.ApiSource
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiRateLimiter @Inject constructor() {
    
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