package com.cashmath.app.`data`

import android.database.Cursor
import androidx.room.CoroutinesRoom
import androidx.room.EntityDeletionOrUpdateAdapter
import androidx.room.EntityInsertionAdapter
import androidx.room.RoomDatabase
import androidx.room.RoomSQLiteQuery
import androidx.room.RoomSQLiteQuery.Companion.acquire
import androidx.room.SharedSQLiteStatement
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.query
import androidx.sqlite.db.SupportSQLiteStatement
import java.lang.Class
import java.util.ArrayList
import java.util.concurrent.Callable
import javax.`annotation`.processing.Generated
import kotlin.Double
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
public class DuePartyDao_Impl(
  __db: RoomDatabase,
) : DuePartyDao {
  private val __db: RoomDatabase

  private val __insertionAdapterOfDuePartyEntity: EntityInsertionAdapter<DuePartyEntity>

  private val __updateAdapterOfDuePartyEntity: EntityDeletionOrUpdateAdapter<DuePartyEntity>

  private val __preparedStmtOfDelete: SharedSQLiteStatement
  init {
    this.__db = __db
    this.__insertionAdapterOfDuePartyEntity = object : EntityInsertionAdapter<DuePartyEntity>(__db)
        {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `due_parties` (`id`,`businessId`,`name`,`phone`,`toReceive`,`toPay`,`updatedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement, entity: DuePartyEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.businessId)
        statement.bindString(3, entity.name)
        val _tmpPhone: String? = entity.phone
        if (_tmpPhone == null) {
          statement.bindNull(4)
        } else {
          statement.bindString(4, _tmpPhone)
        }
        statement.bindDouble(5, entity.toReceive)
        statement.bindDouble(6, entity.toPay)
        statement.bindLong(7, entity.updatedAt)
      }
    }
    this.__updateAdapterOfDuePartyEntity = object :
        EntityDeletionOrUpdateAdapter<DuePartyEntity>(__db) {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `due_parties` SET `id` = ?,`businessId` = ?,`name` = ?,`phone` = ?,`toReceive` = ?,`toPay` = ?,`updatedAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SupportSQLiteStatement, entity: DuePartyEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.businessId)
        statement.bindString(3, entity.name)
        val _tmpPhone: String? = entity.phone
        if (_tmpPhone == null) {
          statement.bindNull(4)
        } else {
          statement.bindString(4, _tmpPhone)
        }
        statement.bindDouble(5, entity.toReceive)
        statement.bindDouble(6, entity.toPay)
        statement.bindLong(7, entity.updatedAt)
        statement.bindLong(8, entity.id)
      }
    }
    this.__preparedStmtOfDelete = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String = "DELETE FROM due_parties WHERE id = ?"
        return _query
      }
    }
  }

  public override suspend fun insert(party: DuePartyEntity): Long = CoroutinesRoom.execute(__db,
      true, object : Callable<Long> {
    public override fun call(): Long {
      __db.beginTransaction()
      try {
        val _result: Long = __insertionAdapterOfDuePartyEntity.insertAndReturnId(party)
        __db.setTransactionSuccessful()
        return _result
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun update(party: DuePartyEntity): Int = CoroutinesRoom.execute(__db,
      true, object : Callable<Int> {
    public override fun call(): Int {
      var _total: Int = 0
      __db.beginTransaction()
      try {
        _total += __updateAdapterOfDuePartyEntity.handle(party)
        __db.setTransactionSuccessful()
        return _total
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun delete(id: Long): Int = CoroutinesRoom.execute(__db, true, object :
      Callable<Int> {
    public override fun call(): Int {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfDelete.acquire()
      var _argIndex: Int = 1
      _stmt.bindLong(_argIndex, id)
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
        __preparedStmtOfDelete.release(_stmt)
      }
    }
  })

  public override fun getPartiesByBusiness(businessId: Long): Flow<List<DuePartyEntity>> {
    val _sql: String = "SELECT * FROM due_parties WHERE businessId = ? ORDER BY updatedAt DESC"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, businessId)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("due_parties"), object :
        Callable<List<DuePartyEntity>> {
      public override fun call(): List<DuePartyEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfBusinessId: Int = getColumnIndexOrThrow(_cursor, "businessId")
          val _cursorIndexOfName: Int = getColumnIndexOrThrow(_cursor, "name")
          val _cursorIndexOfPhone: Int = getColumnIndexOrThrow(_cursor, "phone")
          val _cursorIndexOfToReceive: Int = getColumnIndexOrThrow(_cursor, "toReceive")
          val _cursorIndexOfToPay: Int = getColumnIndexOrThrow(_cursor, "toPay")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _result: MutableList<DuePartyEntity> = ArrayList<DuePartyEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: DuePartyEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpBusinessId: Long
            _tmpBusinessId = _cursor.getLong(_cursorIndexOfBusinessId)
            val _tmpName: String
            _tmpName = _cursor.getString(_cursorIndexOfName)
            val _tmpPhone: String?
            if (_cursor.isNull(_cursorIndexOfPhone)) {
              _tmpPhone = null
            } else {
              _tmpPhone = _cursor.getString(_cursorIndexOfPhone)
            }
            val _tmpToReceive: Double
            _tmpToReceive = _cursor.getDouble(_cursorIndexOfToReceive)
            val _tmpToPay: Double
            _tmpToPay = _cursor.getDouble(_cursorIndexOfToPay)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            _item =
                DuePartyEntity(_tmpId,_tmpBusinessId,_tmpName,_tmpPhone,_tmpToReceive,_tmpToPay,_tmpUpdatedAt)
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

  public override fun getTotalToReceive(businessId: Long): Flow<Double?> {
    val _sql: String = "SELECT SUM(toReceive) FROM due_parties WHERE businessId = ?"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, businessId)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("due_parties"), object : Callable<Double?>
        {
      public override fun call(): Double? {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _result: Double?
          if (_cursor.moveToFirst()) {
            val _tmp: Double?
            if (_cursor.isNull(0)) {
              _tmp = null
            } else {
              _tmp = _cursor.getDouble(0)
            }
            _result = _tmp
          } else {
            _result = null
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

  public override fun getTotalToPay(businessId: Long): Flow<Double?> {
    val _sql: String = "SELECT SUM(toPay) FROM due_parties WHERE businessId = ?"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, businessId)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("due_parties"), object : Callable<Double?>
        {
      public override fun call(): Double? {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _result: Double?
          if (_cursor.moveToFirst()) {
            val _tmp: Double?
            if (_cursor.isNull(0)) {
              _tmp = null
            } else {
              _tmp = _cursor.getDouble(0)
            }
            _result = _tmp
          } else {
            _result = null
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

  public override fun getTotalDuesReceivable(): Flow<Double> {
    val _sql: String = "SELECT COALESCE(SUM(toReceive), 0.0) FROM due_parties WHERE toReceive > 0"
    val _statement: RoomSQLiteQuery = acquire(_sql, 0)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("due_parties"), object : Callable<Double>
        {
      public override fun call(): Double {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _result: Double
          if (_cursor.moveToFirst()) {
            val _tmp: Double
            _tmp = _cursor.getDouble(0)
            _result = _tmp
          } else {
            _result = 0.0
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

  public override fun getTotalDuesReceivable(businessId: Long): Flow<Double> {
    val _sql: String =
        "SELECT COALESCE(SUM(toReceive), 0.0) FROM due_parties WHERE businessId = ? AND toReceive > 0"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, businessId)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("due_parties"), object : Callable<Double>
        {
      public override fun call(): Double {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _result: Double
          if (_cursor.moveToFirst()) {
            val _tmp: Double
            _tmp = _cursor.getDouble(0)
            _result = _tmp
          } else {
            _result = 0.0
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

  public override fun getTotalDuesPayable(): Flow<Double> {
    val _sql: String = "SELECT COALESCE(SUM(toPay), 0.0) FROM due_parties WHERE toPay > 0"
    val _statement: RoomSQLiteQuery = acquire(_sql, 0)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("due_parties"), object : Callable<Double>
        {
      public override fun call(): Double {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _result: Double
          if (_cursor.moveToFirst()) {
            val _tmp: Double
            _tmp = _cursor.getDouble(0)
            _result = _tmp
          } else {
            _result = 0.0
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

  public override fun getTotalDuesPayable(businessId: Long): Flow<Double> {
    val _sql: String =
        "SELECT COALESCE(SUM(toPay), 0.0) FROM due_parties WHERE businessId = ? AND toPay > 0"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, businessId)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("due_parties"), object : Callable<Double>
        {
      public override fun call(): Double {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _result: Double
          if (_cursor.moveToFirst()) {
            val _tmp: Double
            _tmp = _cursor.getDouble(0)
            _result = _tmp
          } else {
            _result = 0.0
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

  public companion object {
    @JvmStatic
    public fun getRequiredConverters(): List<Class<*>> = emptyList()
  }
}
