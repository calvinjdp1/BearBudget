package com.example.bearbudget.network

data class SummaryItem(
    val category: String,
    val amount_used: Double,
    val rollover: Double,
    val budget: Double,
    val remaining: Double
)
