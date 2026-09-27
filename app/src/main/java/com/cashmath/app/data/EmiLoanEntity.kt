package com.cashmath.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "emi_loans",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["businessId"])]
)
data class EmiLoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessId: Long,
    val loanName: String,
    val lenderBank: String,
    val monthlyEmi: Double,
    val dueDayOfMonth: Int,
    val totalTenureMonths: Int,
    val paidMonths: Int = 0,
    val startDate: Long = System.currentTimeMillis()
)
