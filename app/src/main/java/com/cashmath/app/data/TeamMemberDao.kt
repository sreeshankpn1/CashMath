package com.cashmath.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cashmath.app.data.model.BusinessMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamMemberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: BusinessMemberEntity): Long

    @Update
    suspend fun updateMember(member: BusinessMemberEntity): Int

    @Query("SELECT * FROM business_members WHERE businessId = :businessId ORDER BY name ASC")
    fun getMembersByBusiness(businessId: String): Flow<List<BusinessMemberEntity>>

    @Query("SELECT * FROM business_members WHERE inviteCode = :code LIMIT 1")
    suspend fun getMemberByInviteCode(code: String): BusinessMemberEntity?

    @Query("UPDATE business_members SET isConnected = :isConnected, status = 'ACTIVE' WHERE inviteCode = :code")
    suspend fun markConnected(code: String, isConnected: Boolean = true): Int

    @Query("UPDATE business_members SET role = :newRole, scope = :newScope, assignedBookIds = :newBookIds WHERE id = :memberId")
    suspend fun updateMemberScope(memberId: String, newRole: String, newScope: String, newBookIds: String): Int

    @Query("DELETE FROM business_members WHERE id = :id")
    suspend fun deleteMemberById(id: String): Int
}
