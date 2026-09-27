package com.cashmath.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EmiLoanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loan: EmiLoanEntity): Long

    @Update
    suspend fun update(loan: EmiLoanEntity): Int

    @Query("SELECT * FROM emi_loans WHERE businessId = :businessId ORDER BY dueDayOfMonth ASC")
    fun getLoansByBusiness(businessId: Long): Flow<List<EmiLoanEntity>>

    @Query("SELECT SUM(monthlyEmi) FROM emi_loans WHERE businessId = :businessId")
    fun getTotalMonthlyEmi(businessId: Long): Flow<Double?>

    @Query("SELECT COALESCE(SUM(monthlyEmi), 0.0) FROM emi_loans WHERE (totalTenureMonths - paidMonths) > 0")
    fun getActiveMonthlyEmiTotal(): Flow<Double>

    @Query("SELECT COALESCE(SUM(monthlyEmi), 0.0) FROM emi_loans WHERE businessId = :businessId AND (totalTenureMonths - paidMonths) > 0")
    fun getActiveMonthlyEmiTotal(businessId: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(monthlyEmi * (totalTenureMonths - paidMonths)), 0.0) FROM emi_loans WHERE (totalTenureMonths - paidMonths) > 0")
    fun getTotalOutstandingEmi(): Flow<Double>

    @Query("SELECT COALESCE(SUM(monthlyEmi * (totalTenureMonths - paidMonths)), 0.0) FROM emi_loans WHERE businessId = :businessId AND (totalTenureMonths - paidMonths) > 0")
    fun getTotalOutstandingEmi(businessId: Long): Flow<Double>

    @Query("UPDATE emi_loans SET paidMonths = paidMonths + 1 WHERE id = :id AND paidMonths < totalTenureMonths")
    suspend fun incrementPaidMonth(id: Long): Int

    @Query("DELETE FROM emi_loans WHERE id = :id")
    suspend fun delete(id: Long): Int
}
