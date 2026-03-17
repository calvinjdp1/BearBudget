package com.example.bearbudget.network

data class CategoryConfig(
    val name: String,
    val monthly_budget: Double,
    val rollover: Boolean
)

data class CategoryUpsertRequest(
    val name: String,
    val monthly_budget: Double,
    val rollover: Boolean
)
