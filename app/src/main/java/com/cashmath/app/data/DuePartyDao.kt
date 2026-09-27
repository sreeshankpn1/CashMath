package com.cashmath.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DuePartyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(party: DuePartyEntity): Long

    @Update
    suspend fun update(party: DuePartyEntity): Int

    @Query("SELECT * FROM due_parties WHERE businessId = :businessId ORDER BY updatedAt DESC")
    fun getPartiesByBusiness(businessId: Long): Flow<List<DuePartyEntity>>

    @Query("SELECT SUM(toReceive) FROM due_parties WHERE businessId = :businessId")
    fun getTotalToReceive(businessId: Long): Flow<Double?>

    @Query("SELECT SUM(toPay) FROM due_parties WHERE businessId = :businessId")
    fun getTotalToPay(businessId: Long): Flow<Double?>

    @Query("SELECT COALESCE(SUM(toReceive), 0.0) FROM due_parties WHERE toReceive > 0")
    fun getTotalDuesReceivable(): Flow<Double>

    @Query("SELECT COALESCE(SUM(toReceive), 0.0) FROM due_parties WHERE businessId = :businessId AND toReceive > 0")
    fun getTotalDuesReceivable(businessId: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(toPay), 0.0) FROM due_parties WHERE toPay > 0")
    fun getTotalDuesPayable(): Flow<Double>

    @Query("SELECT COALESCE(SUM(toPay), 0.0) FROM due_parties WHERE businessId = :businessId AND toPay > 0")
    fun getTotalDuesPayable(businessId: Long): Flow<Double>

    @Query("DELETE FROM due_parties WHERE id = :id")
    suspend fun delete(id: Long): Int
}
