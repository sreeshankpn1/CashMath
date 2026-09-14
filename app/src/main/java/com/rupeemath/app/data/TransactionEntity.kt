package com.rupeemath.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val merchantOrPayee: String,
    val sourceApp: String,
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis(),
    val rawText: String
)
