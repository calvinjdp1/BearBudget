package com.example.bearbudget.network

data class TransferRequest(
    val date: String,
    val from_account: String,
    val to_account: String,
    val amount: Double,
    val description: String? = "",
    val notes: String? = ""
)
