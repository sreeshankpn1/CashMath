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
public class PaymentModeDao_Impl(
  __db: RoomDatabase,
) : PaymentModeDao {
  private val __db: RoomDatabase

  private val __insertionAdapterOfPaymentModeEntity: EntityInsertionAdapter<PaymentModeEntity>

  private val __updateAdapterOfPaymentModeEntity: EntityDeletionOrUpdateAdapter<PaymentModeEntity>

  private val __preparedStmtOfDelete: SharedSQLiteStatement
  init {
    this.__db = __db
    this.__insertionAdapterOfPaymentModeEntity = object :
        EntityInsertionAdapter<PaymentModeEntity>(__db) {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `payment_modes` (`id`,`bookId`,`name`,`isDefault`,`displayOrder`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement, entity: PaymentModeEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.bookId)
        statement.bindString(3, entity.name)
        val _tmp: Int = if (entity.isDefault) 1 else 0
        statement.bindLong(4, _tmp.toLong())
        statement.bindLong(5, entity.displayOrder.toLong())
      }
    }
    this.__updateAdapterOfPaymentModeEntity = object :
        EntityDeletionOrUpdateAdapter<PaymentModeEntity>(__db) {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `payment_modes` SET `id` = ?,`bookId` = ?,`name` = ?,`isDefault` = ?,`displayOrder` = ? WHERE `id` = ?"

      protected override fun bind(statement: SupportSQLiteStatement, entity: PaymentModeEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.bookId)
        statement.bindString(3, entity.name)
        val _tmp: Int = if (entity.isDefault) 1 else 0
        statement.bindLong(4, _tmp.toLong())
        statement.bindLong(5, entity.displayOrder.toLong())
        statement.bindLong(6, entity.id)
      }
    }
    this.__preparedStmtOfDelete = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String = "DELETE FROM payment_modes WHERE id = ?"
        return _query
      }
    }
  }

  public override suspend fun insert(mode: PaymentModeEntity): Long = CoroutinesRoom.execute(__db,
      true, object : Callable<Long> {
    public override fun call(): Long {
      __db.beginTransaction()
      try {
        val _result: Long = __insertionAdapterOfPaymentModeEntity.insertAndReturnId(mode)
        __db.setTransactionSuccessful()
        return _result
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun update(mode: PaymentModeEntity): Int = CoroutinesRoom.execute(__db,
      true, object : Callable<Int> {
    public override fun call(): Int {
      var _total: Int = 0
      __db.beginTransaction()
      try {
        _total += __updateAdapterOfPaymentModeEntity.handle(mode)
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

  public override fun getModesByBook(bookId: Long): Flow<List<PaymentModeEntity>> {
    val _sql: String = "SELECT * FROM payment_modes WHERE bookId = ? ORDER BY displayOrder ASC"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, bookId)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("payment_modes"), object :
        Callable<List<PaymentModeEntity>> {
      public override fun call(): List<PaymentModeEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfBookId: Int = getColumnIndexOrThrow(_cursor, "bookId")
          val _cursorIndexOfName: Int = getColumnIndexOrThrow(_cursor, "name")
          val _cursorIndexOfIsDefault: Int = getColumnIndexOrThrow(_cursor, "isDefault")
          val _cursorIndexOfDisplayOrder: Int = getColumnIndexOrThrow(_cursor, "displayOrder")
          val _result: MutableList<PaymentModeEntity> =
              ArrayList<PaymentModeEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: PaymentModeEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpBookId: Long
            _tmpBookId = _cursor.getLong(_cursorIndexOfBookId)
            val _tmpName: String
            _tmpName = _cursor.getString(_cursorIndexOfName)
            val _tmpIsDefault: Boolean
            val _tmp: Int
            _tmp = _cursor.getInt(_cursorIndexOfIsDefault)
            _tmpIsDefault = _tmp != 0
            val _tmpDisplayOrder: Int
            _tmpDisplayOrder = _cursor.getInt(_cursorIndexOfDisplayOrder)
            _item = PaymentModeEntity(_tmpId,_tmpBookId,_tmpName,_tmpIsDefault,_tmpDisplayOrder)
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

  public companion object {
    @JvmStatic
    public fun getRequiredConverters(): List<Class<*>> = emptyList()
  }
}
