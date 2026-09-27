package com.cashmath.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class PartyType {
    CUSTOMER,
    SUPPLIER
}

@Entity(tableName = "parties")
data class PartyEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val name: String,
    val phone: String? = null,
    val type: PartyType = PartyType.CUSTOMER,
    val netBalance: Double = 0.0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "party_transactions")
data class PartyTransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val partyId: String,
    val amount: Double,
    val isCredit: Boolean,
    val date: Long = System.currentTimeMillis(),
    val remarks: String = "",
    val paymentMode: String = "Cash"
)
