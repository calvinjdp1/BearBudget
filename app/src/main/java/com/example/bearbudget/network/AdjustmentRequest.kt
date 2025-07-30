package com.example.bearbudget.network

data class AdjustmentRequest(
    val action: String,
    val amount: Double,
    val description: String? = null
)
