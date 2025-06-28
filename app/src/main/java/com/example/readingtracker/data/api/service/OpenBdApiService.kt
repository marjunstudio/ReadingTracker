package com.example.readingtracker.data.api.service

import com.example.readingtracker.data.api.model.OpenBdResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenBdApiService {
    
    @GET("v1/get")
    suspend fun getBookByIsbn(
        @Query("isbn") isbn: String
    ): Response<List<OpenBdResponse?>>
}