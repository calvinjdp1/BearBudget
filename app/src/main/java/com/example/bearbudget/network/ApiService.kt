package com.example.bearbudget.network

import retrofit2.http.*

interface ApiService {
    @GET("cards")
    suspend fun getCards(): List<Card>

    @GET("transactions")
    suspend fun getTransactions(): List<Transaction>

    @GET("categories")
    suspend fun getCategories(): List<String>

    @POST("transactions")
    suspend fun addTransaction(@Body transaction: Transaction)

    @PUT("transactions/{id}")
    suspend fun updateTransaction(@Path("id") id: Int, @Body transaction: Transaction)

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: Int)

    @GET("summary")
    suspend fun getSummary(): List<SummaryItem>
}
