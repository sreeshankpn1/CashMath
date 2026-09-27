package com.cashmath.app.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object PendingTransactionManager {

    data class PendingTransaction(
        val id: String = UUID.randomUUID().toString(),
        val amount: Double,
        val isCashIn: Boolean,
        val senderOrBank: String,
        val rawMessage: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _pendingTransaction = MutableStateFlow<PendingTransaction?>(null)
    val pendingTransaction: StateFlow<PendingTransaction?> = _pendingTransaction.asStateFlow()

    fun postTransaction(transaction: PendingTransaction) {
        _pendingTransaction.value = transaction
    }

    fun dismiss() {
        _pendingTransaction.value = null
    }
}
