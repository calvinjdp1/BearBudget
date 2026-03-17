package com.example.bearbudget.network

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("/categories")
    suspend fun getCategories(): List<String>

    @GET("/cards")
    suspend fun getCards(): List<CardOut>

    @GET("/transactions")
    suspend fun getTransactions(
        @Query("month") month: String? = null,
        @Query("category") category: String? = null
    ): List<Transaction>

    @POST("/transactions")
    suspend fun addTransaction(
        @Body transaction: Transaction
    ): Map<String, String>

    @PUT("/transactions/{transaction_id}")
    suspend fun updateTransaction(
        @Path("transaction_id") id: Int,
        @Body transaction: Transaction
    ): Map<String, String>

    @DELETE("/transactions/{transaction_id}")
    suspend fun deleteTransaction(
        @Path("transaction_id") id: Int
    ): Map<String, String>

    @GET("/summary")
    suspend fun getSummary(): List<SummaryItem>

    @GET("/categories/details")
    suspend fun getCategoryDetails(): List<CategoryConfig>

    @POST("/categories")
    suspend fun addCategory(
        @Body request: CategoryUpsertRequest
    ): CategoryConfig

    @PUT("/categories/{old_name}")
    suspend fun updateCategory(
        @Path("old_name") oldName: String,
        @Body request: CategoryUpsertRequest
    ): CategoryConfig

    @DELETE("/categories/{name}")
    suspend fun deleteCategory(
        @Path("name") name: String
    ): Map<String, String>

    @GET("/accounts")
    suspend fun getAccounts(): Map<String, Any>

    @POST("/banks")
    suspend fun addBank(
        @Body bank: BankBody
    ): Map<String, String>

    @DELETE("/banks/{name}")
    suspend fun deleteBank(
        @Path("name") name: String
    ): Map<String, String>

    @POST("/debts")
    suspend fun addDebt(
        @Body debt: DebtBody
    ): Map<String, String>

    @DELETE("/debts/{name}")
    suspend fun deleteDebt(
        @Path("name") name: String
    ): Map<String, String>

    @POST("/transfer")
    suspend fun makeTransfer(
        @Body request: TransferRequest
    ): Map<String, String>

    @POST("/accounts/{account_name}/adjust")
    suspend fun adjustAccountFunds(
        @Path("account_name") accountName: String,
        @Body request: AdjustmentRequest
    ): Map<String, String>

    @Multipart
    @POST("/upload-receipt")
    suspend fun uploadReceipt(
        @Part file: MultipartBody.Part
    ): UploadReceiptResponse

    @DELETE("/cards/{name}")
    suspend fun deleteCard(
        @Path("name") name: String
    ): Map<String, String>
}