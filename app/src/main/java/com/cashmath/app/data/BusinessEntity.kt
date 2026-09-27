package com.cashmath.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "businesses")
data class BusinessEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String = "Primary Admin",
    val logoUri: String? = null,
    val gstNumber: String? = null,
    val category: String? = "Retail",
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
