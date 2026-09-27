package com.cashmath.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class MemberRole {
    OWNER,       // Full administrative & billing control
    ADMIN,       // Add/edit entries & view reports on assigned books
    STAFF,       // Add entries & attachments only (cannot delete books/entries)
    VIEWER       // Read-only statement & ledger viewer (CA/Partner)
}

enum class AccessScope {
    ALL_BOOKS,
    SPECIFIC_BOOKS
}

@Entity(tableName = "business_members")
data class BusinessMemberEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val role: MemberRole = MemberRole.STAFF,
    val scope: AccessScope = AccessScope.ALL_BOOKS,
    val assignedBookIds: List<String> = emptyList(),
    val canAccessDuesEmi: Boolean = false,
    val inviteCode: String = UUID.randomUUID().toString().take(6).uppercase(),
    val isInviteAccepted: Boolean = false,
    val isConnected: Boolean = false,
    val status: String = "INVITED"
)
