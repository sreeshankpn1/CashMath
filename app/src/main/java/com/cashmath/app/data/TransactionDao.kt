package com.cashmath.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long>

    @Update
    suspend fun update(transaction: TransactionEntity): Int

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity): Int

    @Query("SELECT * FROM transactions WHERE bookId = :bookId AND isDeleted = 0 ORDER BY transactionTimestamp DESC")
    fun getTransactionsByBook(bookId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE bookId = :bookId AND isDeleted = 0 ORDER BY transactionTimestamp DESC")
    fun getTransactionsForBook(bookId: Long): Flow<List<TransactionEntity>>

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long): Int

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteById(transactionId: Long): Int

    @Query("UPDATE transactions SET isDeleted = 1, deletedAt = :timestamp, deletionReason = :reason WHERE id = :id")
    suspend fun softDeleteTransaction(id: Long, timestamp: Long, reason: String?): Int

    @Query("SELECT SUM(amount) FROM transactions WHERE bookId = :bookId AND type = 'EXPENSE' AND isDeleted = 0")
    fun getTotalExpenses(bookId: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE bookId = :bookId AND type = 'INCOME' AND isDeleted = 0")
    fun getTotalIncome(bookId: Long): Flow<Double?>

    @Query("SELECT * FROM transactions WHERE bookId = :bookId AND transactionTimestamp BETWEEN :start AND :end AND isDeleted = 0 ORDER BY transactionTimestamp DESC")
    fun getTransactionsInRange(bookId: Long, start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE bookId = :bookId AND isDeleted = 0 AND transactionTimestamp BETWEEN :start AND :end GROUP BY category ORDER BY total DESC")
    fun getCategorySummaries(bookId: Long, start: Long, end: Long): Flow<List<CategorySummary>>

    @Query("SELECT partyName as merchantOrPayee, SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as totalIn, SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as totalOut FROM transactions WHERE bookId = :bookId AND isDeleted = 0 AND transactionTimestamp BETWEEN :start AND :end GROUP BY partyName ORDER BY (totalIn + totalOut) DESC")
    fun getPartySummaries(bookId: Long, start: Long, end: Long): Flow<List<PartySummary>>

    @Query("SELECT paymentMode as sourceApp, SUM(amount) as total FROM transactions WHERE bookId = :bookId AND isDeleted = 0 AND transactionTimestamp BETWEEN :start AND :end GROUP BY paymentMode ORDER BY total DESC")
    fun getPaymentModeSummaries(bookId: Long, start: Long, end: Long): Flow<List<PaymentModeSummary>>

    @Query("SELECT SUM(amount) FROM transactions WHERE bookId = :bookId AND type = 'EXPENSE' AND isDeleted = 0 AND transactionTimestamp BETWEEN :start AND :end")
    fun getExpensesInRange(bookId: Long, start: Long, end: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE bookId = :bookId AND type = 'INCOME' AND isDeleted = 0 AND transactionTimestamp BETWEEN :start AND :end")
    fun getIncomeInRange(bookId: Long, start: Long, end: Long): Flow<Double?>

    @Query("SELECT * FROM transactions WHERE transactionTimestamp BETWEEN :start AND :end AND isDeleted = 0")
    fun getTransactionsBetweenSync(start: Long, end: Long): List<TransactionEntity>
}
