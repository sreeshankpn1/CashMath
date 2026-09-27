package com.cashmath.app.ui.screens

import androidx.compose.runtime.Composable
import com.cashmath.app.data.TransactionEntity
import com.cashmath.app.data.TransactionType
import com.cashmath.app.ui.screens.transaction.TransactionEntryBottomSheet

@Composable
fun AddEntryBottomSheet(
    bookId: Long,
    entryType: TransactionType,
    transactionToEdit: TransactionEntity? = null,
    onDismiss: () -> Unit,
    onSaveEntry: (TransactionEntity) -> Unit
) {
    TransactionEntryBottomSheet(
        isEditMode = transactionToEdit != null,
        initialTransaction = transactionToEdit,
        bookId = bookId,
        entryType = entryType,
        onDismiss = onDismiss,
        onSave = onSaveEntry
    )
}
