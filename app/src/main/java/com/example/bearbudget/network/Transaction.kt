package com.example.bearbudget.network

data class Transaction(
    val id: Int? = null,
    val date: String,
    val amount: Double,
    val description: String? = "",
    val card: String? = "",
    val category: String? = "",
    val notes: String? = "",
    val transaction_type: String? = "expense",
    val related_account: String? = null
)


