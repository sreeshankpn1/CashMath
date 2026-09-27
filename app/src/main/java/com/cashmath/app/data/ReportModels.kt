package com.cashmath.app.data

data class CategorySummary(
    val category: String,
    val total: Double
)

data class PartySummary(
    val merchantOrPayee: String,
    val totalIn: Double,
    val totalOut: Double
)

data class PaymentModeSummary(
    val sourceApp: String,
    val total: Double
)
