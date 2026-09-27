package com.cashmath.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(business: BusinessEntity): Long

    @Update
    suspend fun update(business: BusinessEntity): Int

    @Query("SELECT * FROM businesses ORDER BY createdAt DESC")
    fun getAllBusinesses(): Flow<List<BusinessEntity>>

    @Query("SELECT * FROM businesses WHERE id = :id")
    suspend fun getBusinessById(id: Long): BusinessEntity?

    @Query("UPDATE businesses SET name = :newName WHERE id = :businessId")
    suspend fun updateName(businessId: Long, newName: String): Int

    @Query("DELETE FROM businesses WHERE id = :id")
    suspend fun delete(id: Long): Int

    @Query("DELETE FROM businesses WHERE id = :businessId")
    suspend fun deleteBusinessCascade(businessId: Long): Int
}
