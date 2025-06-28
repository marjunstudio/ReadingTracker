package com.example.readingtracker.data.api.service

import com.example.readingtracker.data.api.model.GoogleBooksSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksApiService {
    
    @GET("books/v1/volumes")
    suspend fun searchByIsbn(
        @Query("q") query: String,
        @Query("key") apiKey: String
    ): Response<GoogleBooksSearchResponse>
}