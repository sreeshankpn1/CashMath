package com.cashmath.app.`data`

import android.database.Cursor
import android.os.CancellationSignal
import androidx.room.CoroutinesRoom
import androidx.room.CoroutinesRoom.Companion.execute
import androidx.room.EntityDeletionOrUpdateAdapter
import androidx.room.EntityInsertionAdapter
import androidx.room.RoomDatabase
import androidx.room.RoomSQLiteQuery
import androidx.room.RoomSQLiteQuery.Companion.acquire
import androidx.room.SharedSQLiteStatement
import androidx.room.util.createCancellationSignal
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.query
import androidx.sqlite.db.SupportSQLiteStatement
import com.cashmath.app.`data`.model.AccessScope
import com.cashmath.app.`data`.model.BusinessMemberEntity
import com.cashmath.app.`data`.model.MemberRole
import java.lang.Class
import java.util.ArrayList
import java.util.concurrent.Callable
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.jvm.JvmStatic
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION"])
public class TeamMemberDao_Impl(
  __db: RoomDatabase,
) : TeamMemberDao {
  private val __db: RoomDatabase

  private val __insertionAdapterOfBusinessMemberEntity: EntityInsertionAdapter<BusinessMemberEntity>

  private val __converters: Converters = Converters()

  private val __updateAdapterOfBusinessMemberEntity:
      EntityDeletionOrUpdateAdapter<BusinessMemberEntity>

  private val __preparedStmtOfMarkConnected: SharedSQLiteStatement

  private val __preparedStmtOfUpdateMemberScope: SharedSQLiteStatement

  private val __preparedStmtOfDeleteMemberById: SharedSQLiteStatement
  init {
    this.__db = __db
    this.__insertionAdapterOfBusinessMemberEntity = object :
        EntityInsertionAdapter<BusinessMemberEntity>(__db) {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `business_members` (`id`,`businessId`,`name`,`phone`,`email`,`role`,`scope`,`assignedBookIds`,`canAccessDuesEmi`,`inviteCode`,`isInviteAccepted`,`isConnected`,`status`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement, entity: BusinessMemberEntity) {
        statement.bindString(1, entity.id)
        statement.bindString(2, entity.businessId)
        statement.bindString(3, entity.name)
        val _tmpPhone: String? = entity.phone
        if (_tmpPhone == null) {
          statement.bindNull(4)
        } else {
          statement.bindString(4, _tmpPhone)
        }
        val _tmpEmail: String? = entity.email
        if (_tmpEmail == null) {
          statement.bindNull(5)
        } else {
          statement.bindString(5, _tmpEmail)
        }
        val _tmp: String = __converters.fromMemberRole(entity.role)
        statement.bindString(6, _tmp)
        val _tmp_1: String = __converters.fromAccessScope(entity.scope)
        statement.bindString(7, _tmp_1)
        val _tmp_2: String = __converters.fromStringList(entity.assignedBookIds)
        statement.bindString(8, _tmp_2)
        val _tmp_3: Int = if (entity.canAccessDuesEmi) 1 else 0
        statement.bindLong(9, _tmp_3.toLong())
        statement.bindString(10, entity.inviteCode)
        val _tmp_4: Int = if (entity.isInviteAccepted) 1 else 0
        statement.bindLong(11, _tmp_4.toLong())
        val _tmp_5: Int = if (entity.isConnected) 1 else 0
        statement.bindLong(12, _tmp_5.toLong())
        statement.bindString(13, entity.status)
      }
    }
    this.__updateAdapterOfBusinessMemberEntity = object :
        EntityDeletionOrUpdateAdapter<BusinessMemberEntity>(__db) {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `business_members` SET `id` = ?,`businessId` = ?,`name` = ?,`phone` = ?,`email` = ?,`role` = ?,`scope` = ?,`assignedBookIds` = ?,`canAccessDuesEmi` = ?,`inviteCode` = ?,`isInviteAccepted` = ?,`isConnected` = ?,`status` = ? WHERE `id` = ?"

      protected override fun bind(statement: SupportSQLiteStatement, entity: BusinessMemberEntity) {
        statement.bindString(1, entity.id)
        statement.bindString(2, entity.businessId)
        statement.bindString(3, entity.name)
        val _tmpPhone: String? = entity.phone
        if (_tmpPhone == null) {
          statement.bindNull(4)
        } else {
          statement.bindString(4, _tmpPhone)
        }
        val _tmpEmail: String? = entity.email
        if (_tmpEmail == null) {
          statement.bindNull(5)
        } else {
          statement.bindString(5, _tmpEmail)
        }
        val _tmp: String = __converters.fromMemberRole(entity.role)
        statement.bindString(6, _tmp)
        val _tmp_1: String = __converters.fromAccessScope(entity.scope)
        statement.bindString(7, _tmp_1)
        val _tmp_2: String = __converters.fromStringList(entity.assignedBookIds)
        statement.bindString(8, _tmp_2)
        val _tmp_3: Int = if (entity.canAccessDuesEmi) 1 else 0
        statement.bindLong(9, _tmp_3.toLong())
        statement.bindString(10, entity.inviteCode)
        val _tmp_4: Int = if (entity.isInviteAccepted) 1 else 0
        statement.bindLong(11, _tmp_4.toLong())
        val _tmp_5: Int = if (entity.isConnected) 1 else 0
        statement.bindLong(12, _tmp_5.toLong())
        statement.bindString(13, entity.status)
        statement.bindString(14, entity.id)
      }
    }
    this.__preparedStmtOfMarkConnected = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String =
            "UPDATE business_members SET isConnected = ?, status = 'ACTIVE' WHERE inviteCode = ?"
        return _query
      }
    }
    this.__preparedStmtOfUpdateMemberScope = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String =
            "UPDATE business_members SET role = ?, scope = ?, assignedBookIds = ? WHERE id = ?"
        return _query
      }
    }
    this.__preparedStmtOfDeleteMemberById = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String = "DELETE FROM business_members WHERE id = ?"
        return _query
      }
    }
  }

  public override suspend fun insertMember(member: BusinessMemberEntity): Long =
      CoroutinesRoom.execute(__db, true, object : Callable<Long> {
    public override fun call(): Long {
      __db.beginTransaction()
      try {
        val _result: Long = __insertionAdapterOfBusinessMemberEntity.insertAndReturnId(member)
        __db.setTransactionSuccessful()
        return _result
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun updateMember(member: BusinessMemberEntity): Int =
      CoroutinesRoom.execute(__db, true, object : Callable<Int> {
    public override fun call(): Int {
      var _total: Int = 0
      __db.beginTransaction()
      try {
        _total += __updateAdapterOfBusinessMemberEntity.handle(member)
        __db.setTransactionSuccessful()
        return _total
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun markConnected(code: String, isConnected: Boolean): Int =
      CoroutinesRoom.execute(__db, true, object : Callable<Int> {
    public override fun call(): Int {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfMarkConnected.acquire()
      var _argIndex: Int = 1
      val _tmp: Int = if (isConnected) 1 else 0
      _stmt.bindLong(_argIndex, _tmp.toLong())
      _argIndex = 2
      _stmt.bindString(_argIndex, code)
      try {
        __db.beginTransaction()
        try {
          val _result: Int = _stmt.executeUpdateDelete()
          __db.setTransactionSuccessful()
          return _result
        } finally {
          __db.endTransaction()
        }
      } finally {
        __preparedStmtOfMarkConnected.release(_stmt)
      }
    }
  })

  public override suspend fun updateMemberScope(
    memberId: String,
    newRole: String,
    newScope: String,
    newBookIds: String,
  ): Int = CoroutinesRoom.execute(__db, true, object : Callable<Int> {
    public override fun call(): Int {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfUpdateMemberScope.acquire()
      var _argIndex: Int = 1
      _stmt.bindString(_argIndex, newRole)
      _argIndex = 2
      _stmt.bindString(_argIndex, newScope)
      _argIndex = 3
      _stmt.bindString(_argIndex, newBookIds)
      _argIndex = 4
      _stmt.bindString(_argIndex, memberId)
      try {
        __db.beginTransaction()
        try {
          val _result: Int = _stmt.executeUpdateDelete()
          __db.setTransactionSuccessful()
          return _result
        } finally {
          __db.endTransaction()
        }
      } finally {
        __preparedStmtOfUpdateMemberScope.release(_stmt)
      }
    }
  })

  public override suspend fun deleteMemberById(id: String): Int = CoroutinesRoom.execute(__db, true,
      object : Callable<Int> {
    public override fun call(): Int {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfDeleteMemberById.acquire()
      var _argIndex: Int = 1
      _stmt.bindString(_argIndex, id)
      try {
        __db.beginTransaction()
        try {
          val _result: Int = _stmt.executeUpdateDelete()
          __db.setTransactionSuccessful()
          return _result
        } finally {
          __db.endTransaction()
        }
      } finally {
        __preparedStmtOfDeleteMemberById.release(_stmt)
      }
    }
  })

  public override fun getMembersByBusiness(businessId: String): Flow<List<BusinessMemberEntity>> {
    val _sql: String = "SELECT * FROM business_members WHERE businessId = ? ORDER BY name ASC"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindString(_argIndex, businessId)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("business_members"), object :
        Callable<List<BusinessMemberEntity>> {
      public override fun call(): List<BusinessMemberEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfBusinessId: Int = getColumnIndexOrThrow(_cursor, "businessId")
          val _cursorIndexOfName: Int = getColumnIndexOrThrow(_cursor, "name")
          val _cursorIndexOfPhone: Int = getColumnIndexOrThrow(_cursor, "phone")
          val _cursorIndexOfEmail: Int = getColumnIndexOrThrow(_cursor, "email")
          val _cursorIndexOfRole: Int = getColumnIndexOrThrow(_cursor, "role")
          val _cursorIndexOfScope: Int = getColumnIndexOrThrow(_cursor, "scope")
          val _cursorIndexOfAssignedBookIds: Int = getColumnIndexOrThrow(_cursor, "assignedBookIds")
          val _cursorIndexOfCanAccessDuesEmi: Int = getColumnIndexOrThrow(_cursor,
              "canAccessDuesEmi")
          val _cursorIndexOfInviteCode: Int = getColumnIndexOrThrow(_cursor, "inviteCode")
          val _cursorIndexOfIsInviteAccepted: Int = getColumnIndexOrThrow(_cursor,
              "isInviteAccepted")
          val _cursorIndexOfIsConnected: Int = getColumnIndexOrThrow(_cursor, "isConnected")
          val _cursorIndexOfStatus: Int = getColumnIndexOrThrow(_cursor, "status")
          val _result: MutableList<BusinessMemberEntity> =
              ArrayList<BusinessMemberEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: BusinessMemberEntity
            val _tmpId: String
            _tmpId = _cursor.getString(_cursorIndexOfId)
            val _tmpBusinessId: String
            _tmpBusinessId = _cursor.getString(_cursorIndexOfBusinessId)
            val _tmpName: String
            _tmpName = _cursor.getString(_cursorIndexOfName)
            val _tmpPhone: String?
            if (_cursor.isNull(_cursorIndexOfPhone)) {
              _tmpPhone = null
            } else {
              _tmpPhone = _cursor.getString(_cursorIndexOfPhone)
            }
            val _tmpEmail: String?
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail)
            }
            val _tmpRole: MemberRole
            val _tmp: String
            _tmp = _cursor.getString(_cursorIndexOfRole)
            _tmpRole = __converters.toMemberRole(_tmp)
            val _tmpScope: AccessScope
            val _tmp_1: String
            _tmp_1 = _cursor.getString(_cursorIndexOfScope)
            _tmpScope = __converters.toAccessScope(_tmp_1)
            val _tmpAssignedBookIds: List<String>
            val _tmp_2: String
            _tmp_2 = _cursor.getString(_cursorIndexOfAssignedBookIds)
            _tmpAssignedBookIds = __converters.toStringList(_tmp_2)
            val _tmpCanAccessDuesEmi: Boolean
            val _tmp_3: Int
            _tmp_3 = _cursor.getInt(_cursorIndexOfCanAccessDuesEmi)
            _tmpCanAccessDuesEmi = _tmp_3 != 0
            val _tmpInviteCode: String
            _tmpInviteCode = _cursor.getString(_cursorIndexOfInviteCode)
            val _tmpIsInviteAccepted: Boolean
            val _tmp_4: Int
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsInviteAccepted)
            _tmpIsInviteAccepted = _tmp_4 != 0
            val _tmpIsConnected: Boolean
            val _tmp_5: Int
            _tmp_5 = _cursor.getInt(_cursorIndexOfIsConnected)
            _tmpIsConnected = _tmp_5 != 0
            val _tmpStatus: String
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus)
            _item =
                BusinessMemberEntity(_tmpId,_tmpBusinessId,_tmpName,_tmpPhone,_tmpEmail,_tmpRole,_tmpScope,_tmpAssignedBookIds,_tmpCanAccessDuesEmi,_tmpInviteCode,_tmpIsInviteAccepted,_tmpIsConnected,_tmpStatus)
            _result.add(_item)
          }
          return _result
        } finally {
          _cursor.close()
        }
      }

      protected fun finalize() {
        _statement.release()
      }
    })
  }

  public override suspend fun getMemberByInviteCode(code: String): BusinessMemberEntity? {
    val _sql: String = "SELECT * FROM business_members WHERE inviteCode = ? LIMIT 1"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindString(_argIndex, code)
    val _cancellationSignal: CancellationSignal? = createCancellationSignal()
    return execute(__db, false, _cancellationSignal, object : Callable<BusinessMemberEntity?> {
      public override fun call(): BusinessMemberEntity? {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfBusinessId: Int = getColumnIndexOrThrow(_cursor, "businessId")
          val _cursorIndexOfName: Int = getColumnIndexOrThrow(_cursor, "name")
          val _cursorIndexOfPhone: Int = getColumnIndexOrThrow(_cursor, "phone")
          val _cursorIndexOfEmail: Int = getColumnIndexOrThrow(_cursor, "email")
          val _cursorIndexOfRole: Int = getColumnIndexOrThrow(_cursor, "role")
          val _cursorIndexOfScope: Int = getColumnIndexOrThrow(_cursor, "scope")
          val _cursorIndexOfAssignedBookIds: Int = getColumnIndexOrThrow(_cursor, "assignedBookIds")
          val _cursorIndexOfCanAccessDuesEmi: Int = getColumnIndexOrThrow(_cursor,
              "canAccessDuesEmi")
          val _cursorIndexOfInviteCode: Int = getColumnIndexOrThrow(_cursor, "inviteCode")
          val _cursorIndexOfIsInviteAccepted: Int = getColumnIndexOrThrow(_cursor,
              "isInviteAccepted")
          val _cursorIndexOfIsConnected: Int = getColumnIndexOrThrow(_cursor, "isConnected")
          val _cursorIndexOfStatus: Int = getColumnIndexOrThrow(_cursor, "status")
          val _result: BusinessMemberEntity?
          if (_cursor.moveToFirst()) {
            val _tmpId: String
            _tmpId = _cursor.getString(_cursorIndexOfId)
            val _tmpBusinessId: String
            _tmpBusinessId = _cursor.getString(_cursorIndexOfBusinessId)
            val _tmpName: String
            _tmpName = _cursor.getString(_cursorIndexOfName)
            val _tmpPhone: String?
            if (_cursor.isNull(_cursorIndexOfPhone)) {
              _tmpPhone = null
            } else {
              _tmpPhone = _cursor.getString(_cursorIndexOfPhone)
            }
            val _tmpEmail: String?
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail)
            }
            val _tmpRole: MemberRole
            val _tmp: String
            _tmp = _cursor.getString(_cursorIndexOfRole)
            _tmpRole = __converters.toMemberRole(_tmp)
            val _tmpScope: AccessScope
            val _tmp_1: String
            _tmp_1 = _cursor.getString(_cursorIndexOfScope)
            _tmpScope = __converters.toAccessScope(_tmp_1)
            val _tmpAssignedBookIds: List<String>
            val _tmp_2: String
            _tmp_2 = _cursor.getString(_cursorIndexOfAssignedBookIds)
            _tmpAssignedBookIds = __converters.toStringList(_tmp_2)
            val _tmpCanAccessDuesEmi: Boolean
            val _tmp_3: Int
            _tmp_3 = _cursor.getInt(_cursorIndexOfCanAccessDuesEmi)
            _tmpCanAccessDuesEmi = _tmp_3 != 0
            val _tmpInviteCode: String
            _tmpInviteCode = _cursor.getString(_cursorIndexOfInviteCode)
            val _tmpIsInviteAccepted: Boolean
            val _tmp_4: Int
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsInviteAccepted)
            _tmpIsInviteAccepted = _tmp_4 != 0
            val _tmpIsConnected: Boolean
            val _tmp_5: Int
            _tmp_5 = _cursor.getInt(_cursorIndexOfIsConnected)
            _tmpIsConnected = _tmp_5 != 0
            val _tmpStatus: String
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus)
            _result =
                BusinessMemberEntity(_tmpId,_tmpBusinessId,_tmpName,_tmpPhone,_tmpEmail,_tmpRole,_tmpScope,_tmpAssignedBookIds,_tmpCanAccessDuesEmi,_tmpInviteCode,_tmpIsInviteAccepted,_tmpIsConnected,_tmpStatus)
          } else {
            _result = null
          }
          return _result
        } finally {
          _cursor.close()
          _statement.release()
        }
      }
    })
  }

  public companion object {
    @JvmStatic
    public fun getRequiredConverters(): List<Class<*>> = emptyList()
  }
}
