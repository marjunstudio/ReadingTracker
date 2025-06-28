package com.example.readingtracker.data.api.model

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