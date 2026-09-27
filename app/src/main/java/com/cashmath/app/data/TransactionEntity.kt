package com.cashmath.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME
}

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookId"])]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val amount: Double,
    val type: TransactionType,
    val partyName: String? = null,
    val remark: String? = null,
    val category: String? = "General",
    val paymentMode: String = "Cash",
    val attachmentUri: String? = null,
    val transactionTimestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdByName: String = "You",
    val createdByRole: String = "OWNER",
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletionReason: String? = null
)
