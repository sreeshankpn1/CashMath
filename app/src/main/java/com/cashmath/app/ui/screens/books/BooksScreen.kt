package com.cashmath.app.ui.screens.books

import androidx.compose.runtime.Composable
import com.cashmath.app.data.BookEntity
import com.cashmath.app.data.BookMemberEntity
import com.cashmath.app.data.TransactionEntity
import com.cashmath.app.ui.screens.BookLedgerScreen

@Composable
fun BooksScreen(
    book: BookEntity?,
    transactions: List<TransactionEntity>,
    netBalance: Double,
    totalIn: Double,
    totalOut: Double,
    myRole: String,
    members: List<BookMemberEntity>,
    hasPermission: Boolean,
    onBack: () -> Unit,
    onRequestPermission: () -> Unit,
    onSaveTransaction: (TransactionEntity) -> Unit,
    onUpdateTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onShowReports: () -> Unit,
    onAddMember: (userIdentifier: String, displayName: String, role: String) -> Unit
) {
    BookLedgerScreen(
        book = book,
        transactions = transactions,
        netBalance = netBalance,
        totalIn = totalIn,
        totalOut = totalOut,
        myRole = myRole,
        members = members,
        hasPermission = hasPermission,
        onBack = onBack,
        onRequestPermission = onRequestPermission,
        onSaveTransaction = onSaveTransaction,
        onUpdateTransaction = onUpdateTransaction,
        onDeleteTransaction = onDeleteTransaction,
        onShowReports = onShowReports,
        onAddMember = onAddMember
    )
}
