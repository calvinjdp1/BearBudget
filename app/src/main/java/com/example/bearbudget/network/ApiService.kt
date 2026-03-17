package com.example.bearbudget.network

import retrofit2.http.*

data class BankBody(val name: String, val balance: Double)
data class DebtBody(val name: String, val balance: Double)

interface ApiService {
    @GET("cards")
    suspend fun getCards(): List<Card>

    @GET("transactions")
    suspend fun getTransactions(): List<Transaction>

    // Existing simple list
    @GET("categories")
    suspend fun getCategories(): List<String>

    // ✅ NEW: details for edit UI
    @GET("categories/details")
    suspend fun getCategoryDetails(): List<CategoryConfig>

    // ✅ NEW: add category
    @POST("categories")
    suspend fun addCategory(@Body req: CategoryUpsertRequest): CategoryConfig

    // ✅ NEW: update/rename category
    @PUT("categories/{oldName}")
    suspend fun updateCategory(
        @Path("oldName") oldName: String,
        @Body req: CategoryUpsertRequest
    ): CategoryConfig

    // ✅ NEW: delete category
    @DELETE("categories/{name}")
    suspend fun deleteCategory(@Path("name") name: String)

    @POST("transactions")
    suspend fun addTransaction(@Body transaction: Transaction)

    @PUT("transactions/{id}")
    suspend fun updateTransaction(@Path("id") id: Int, @Body transaction: Transaction)

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: Int)

    @GET("summary")
    suspend fun getSummary(): List<SummaryItem>

    @GET("accounts")
    suspend fun getAccounts(): Map<String, Any>

    @POST("banks")
    suspend fun addBank(@Body bank: BankBody)

    @POST("debts")
    suspend fun addDebt(@Body debt: DebtBody)

    @DELETE("cards/{name}")
    suspend fun deleteCard(@Path("name") name: String)

    @POST("accounts/{accountName}/adjust")
    suspend fun adjustAccountFunds(
        @Path("accountName") accountName: String,
        @Body request: AdjustmentRequest
    )

    @DELETE("banks/{name}")
    suspend fun deleteBank(@Path("name") name: String)

    @DELETE("debts/{name}")
    suspend fun deleteDebt(@Path("name") name: String)

    // I recommend removing the leading "/" here (safer in Retrofit)
    @POST("transfer")
    suspend fun makeTransfer(@Body transfer: TransferRequest)
}
