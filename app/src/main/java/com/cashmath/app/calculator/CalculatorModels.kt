package com.cashmath.app.calculator

data class CalculationHistoryItem(
    val id: Long = System.currentTimeMillis(),
    val expression: String,
    val result: Double,
    val timestamp: Long = System.currentTimeMillis()
)

enum class GstRate(val percentage: Double, val label: String) {
    GST_5(5.0, "5%"),
    GST_12(12.0, "12%"),
    GST_18(18.0, "18%"),
    GST_28(28.0, "28%")
}
