package com.example.readingtracker.data.model

import com.example.readingtracker.data.api.model.BookData

sealed class BookSearchResult {
    data class Success(val bookData: BookData) : BookSearchResult()
    object NotFound : BookSearchResult()
    object RateLimitExceeded : BookSearchResult()
    object NetworkError : BookSearchResult()
    object DailyLimitExceeded : BookSearchResult()
    data class ApiError(val message: String) : BookSearchResult()
}