package com.cashmath.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentModeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mode: PaymentModeEntity): Long

    @Update
    suspend fun update(mode: PaymentModeEntity): Int

    @Query("SELECT * FROM payment_modes WHERE bookId = :bookId ORDER BY displayOrder ASC")
    fun getModesByBook(bookId: Long): Flow<List<PaymentModeEntity>>

    @Query("DELETE FROM payment_modes WHERE id = :id")
    suspend fun delete(id: Long): Int
}
