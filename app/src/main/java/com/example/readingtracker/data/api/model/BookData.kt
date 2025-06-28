package com.example.readingtracker.data.api.model

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

enum class ApiSource {
    OPEN_BD,
    GOOGLE_BOOKS
}