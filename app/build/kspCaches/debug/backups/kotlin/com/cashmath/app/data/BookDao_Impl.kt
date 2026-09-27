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
import androidx.room.util.appendPlaceholders
import androidx.room.util.createCancellationSignal
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.newStringBuilder
import androidx.room.util.query
import androidx.sqlite.db.SupportSQLiteStatement
import java.lang.Class
import java.lang.StringBuilder
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
public class BookDao_Impl(
  __db: RoomDatabase,
) : BookDao {
  private val __db: RoomDatabase

  private val __insertionAdapterOfBookEntity: EntityInsertionAdapter<BookEntity>

  private val __insertionAdapterOfBookMemberEntity: EntityInsertionAdapter<BookMemberEntity>

  private val __updateAdapterOfBookEntity: EntityDeletionOrUpdateAdapter<BookEntity>

  private val __preparedStmtOfDeleteBook: SharedSQLiteStatement

  private val __preparedStmtOfUpdateBalance: SharedSQLiteStatement

  private val __preparedStmtOfMoveBook: SharedSQLiteStatement

  private val __preparedStmtOfRecalculateBookBalance: SharedSQLiteStatement

  private val __preparedStmtOfRemoveMember: SharedSQLiteStatement
  init {
    this.__db = __db
    this.__insertionAdapterOfBookEntity = object : EntityInsertionAdapter<BookEntity>(__db) {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `books` (`id`,`businessId`,`name`,`balance`,`updatedAt`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement, entity: BookEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.businessId)
        statement.bindString(3, entity.name)
        statement.bindDouble(4, entity.balance)
        statement.bindLong(5, entity.updatedAt)
        statement.bindLong(6, entity.createdAt)
      }
    }
    this.__insertionAdapterOfBookMemberEntity = object :
        EntityInsertionAdapter<BookMemberEntity>(__db) {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `book_members` (`id`,`bookId`,`userIdentifier`,`displayName`,`role`,`joinedAt`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement, entity: BookMemberEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.bookId)
        statement.bindString(3, entity.userIdentifier)
        statement.bindString(4, entity.displayName)
        statement.bindString(5, entity.role)
        statement.bindLong(6, entity.joinedAt)
      }
    }
    this.__updateAdapterOfBookEntity = object : EntityDeletionOrUpdateAdapter<BookEntity>(__db) {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `books` SET `id` = ?,`businessId` = ?,`name` = ?,`balance` = ?,`updatedAt` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SupportSQLiteStatement, entity: BookEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.businessId)
        statement.bindString(3, entity.name)
        statement.bindDouble(4, entity.balance)
        statement.bindLong(5, entity.updatedAt)
        statement.bindLong(6, entity.createdAt)
        statement.bindLong(7, entity.id)
      }
    }
    this.__preparedStmtOfDeleteBook = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String = "DELETE FROM books WHERE id = ?"
        return _query
      }
    }
    this.__preparedStmtOfUpdateBalance = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String = "UPDATE books SET balance = ?, updatedAt = ? WHERE id = ?"
        return _query
      }
    }
    this.__preparedStmtOfMoveBook = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String = "UPDATE books SET businessId = ?, updatedAt = ? WHERE id = ?"
        return _query
      }
    }
    this.__preparedStmtOfRecalculateBookBalance = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String =
            "UPDATE books SET balance = (SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END), 0.0) FROM transactions WHERE bookId = ? AND isDeleted = 0), updatedAt = (strftime('%s','now') * 1000) WHERE id = ?"
        return _query
      }
    }
    this.__preparedStmtOfRemoveMember = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String = "DELETE FROM book_members WHERE bookId = ? AND userIdentifier = ?"
        return _query
      }
    }
  }

  public override suspend fun insertBook(book: BookEntity): Long = CoroutinesRoom.execute(__db,
      true, object : Callable<Long> {
    public override fun call(): Long {
      __db.beginTransaction()
      try {
        val _result: Long = __insertionAdapterOfBookEntity.insertAndReturnId(book)
        __db.setTransactionSuccessful()
        return _result
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun insertMember(member: BookMemberEntity): Long =
      CoroutinesRoom.execute(__db, true, object : Callable<Long> {
    public override fun call(): Long {
      __db.beginTransaction()
      try {
        val _result: Long = __insertionAdapterOfBookMemberEntity.insertAndReturnId(member)
        __db.setTransactionSuccessful()
        return _result
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun updateBook(book: BookEntity): Int = CoroutinesRoom.execute(__db, true,
      object : Callable<Int> {
    public override fun call(): Int {
      var _total: Int = 0
      __db.beginTransaction()
      try {
        _total += __updateAdapterOfBookEntity.handle(book)
        __db.setTransactionSuccessful()
        return _total
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun deleteBook(id: Long): Int = CoroutinesRoom.execute(__db, true, object
      : Callable<Int> {
    public override fun call(): Int {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfDeleteBook.acquire()
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
        __preparedStmtOfDeleteBook.release(_stmt)
      }
    }
  })

  public override suspend fun updateBalance(
    id: Long,
    balance: Double,
    updatedAt: Long,
  ): Int = CoroutinesRoom.execute(__db, true, object : Callable<Int> {
    public override fun call(): Int {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfUpdateBalance.acquire()
      var _argIndex: Int = 1
      _stmt.bindDouble(_argIndex, balance)
      _argIndex = 2
      _stmt.bindLong(_argIndex, updatedAt)
      _argIndex = 3
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
        __preparedStmtOfUpdateBalance.release(_stmt)
      }
    }
  })

  public override suspend fun moveBook(
    bookId: Long,
    targetBusinessId: Long,
    updatedAt: Long,
  ): Int = CoroutinesRoom.execute(__db, true, object : Callable<Int> {
    public override fun call(): Int {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfMoveBook.acquire()
      var _argIndex: Int = 1
      _stmt.bindLong(_argIndex, targetBusinessId)
      _argIndex = 2
      _stmt.bindLong(_argIndex, updatedAt)
      _argIndex = 3
      _stmt.bindLong(_argIndex, bookId)
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
        __preparedStmtOfMoveBook.release(_stmt)
      }
    }
  })

  public override suspend fun recalculateBookBalance(bookId: Long): Int =
      CoroutinesRoom.execute(__db, true, object : Callable<Int> {
    public override fun call(): Int {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfRecalculateBookBalance.acquire()
      var _argIndex: Int = 1
      _stmt.bindLong(_argIndex, bookId)
      _argIndex = 2
      _stmt.bindLong(_argIndex, bookId)
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
        __preparedStmtOfRecalculateBookBalance.release(_stmt)
      }
    }
  })

  public override suspend fun removeMember(bookId: Long, userIdentifier: String): Int =
      CoroutinesRoom.execute(__db, true, object : Callable<Int> {
    public override fun call(): Int {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfRemoveMember.acquire()
      var _argIndex: Int = 1
      _stmt.bindLong(_argIndex, bookId)
      _argIndex = 2
      _stmt.bindString(_argIndex, userIdentifier)
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
        __preparedStmtOfRemoveMember.release(_stmt)
      }
    }
  })

  public override fun getBooksByBusiness(businessId: Long): Flow<List<BookEntity>> {
    val _sql: String = "SELECT * FROM books WHERE businessId = ? ORDER BY updatedAt DESC"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, businessId)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("books"), object :
        Callable<List<BookEntity>> {
      public override fun call(): List<BookEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfBusinessId: Int = getColumnIndexOrThrow(_cursor, "businessId")
          val _cursorIndexOfName: Int = getColumnIndexOrThrow(_cursor, "name")
          val _cursorIndexOfBalance: Int = getColumnIndexOrThrow(_cursor, "balance")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _result: MutableList<BookEntity> = ArrayList<BookEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: BookEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpBusinessId: Long
            _tmpBusinessId = _cursor.getLong(_cursorIndexOfBusinessId)
            val _tmpName: String
            _tmpName = _cursor.getString(_cursorIndexOfName)
            val _tmpBalance: Double
            _tmpBalance = _cursor.getDouble(_cursorIndexOfBalance)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            _item =
                BookEntity(_tmpId,_tmpBusinessId,_tmpName,_tmpBalance,_tmpUpdatedAt,_tmpCreatedAt)
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

  public override fun getBooksByBusinessAndAllowedIds(businessId: Long, allowedBookIds: List<Long>):
      Flow<List<BookEntity>> {
    val _stringBuilder: StringBuilder = newStringBuilder()
    _stringBuilder.append("SELECT * FROM books WHERE businessId = ")
    _stringBuilder.append("?")
    _stringBuilder.append(" AND id IN (")
    val _inputSize: Int = allowedBookIds.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(") ORDER BY updatedAt DESC")
    val _sql: String = _stringBuilder.toString()
    val _argCount: Int = 1 + _inputSize
    val _statement: RoomSQLiteQuery = acquire(_sql, _argCount)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, businessId)
    _argIndex = 2
    for (_item: Long in allowedBookIds) {
      _statement.bindLong(_argIndex, _item)
      _argIndex++
    }
    return CoroutinesRoom.createFlow(__db, false, arrayOf("books"), object :
        Callable<List<BookEntity>> {
      public override fun call(): List<BookEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfBusinessId: Int = getColumnIndexOrThrow(_cursor, "businessId")
          val _cursorIndexOfName: Int = getColumnIndexOrThrow(_cursor, "name")
          val _cursorIndexOfBalance: Int = getColumnIndexOrThrow(_cursor, "balance")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _result: MutableList<BookEntity> = ArrayList<BookEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item_1: BookEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpBusinessId: Long
            _tmpBusinessId = _cursor.getLong(_cursorIndexOfBusinessId)
            val _tmpName: String
            _tmpName = _cursor.getString(_cursorIndexOfName)
            val _tmpBalance: Double
            _tmpBalance = _cursor.getDouble(_cursorIndexOfBalance)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            _item_1 =
                BookEntity(_tmpId,_tmpBusinessId,_tmpName,_tmpBalance,_tmpUpdatedAt,_tmpCreatedAt)
            _result.add(_item_1)
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

  public override fun getBooksByIds(bizId: Long, bookIds: List<Long>): Flow<List<BookEntity>> {
    val _stringBuilder: StringBuilder = newStringBuilder()
    _stringBuilder.append("SELECT * FROM books WHERE businessId = ")
    _stringBuilder.append("?")
    _stringBuilder.append(" AND id IN (")
    val _inputSize: Int = bookIds.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(") ORDER BY id DESC")
    val _sql: String = _stringBuilder.toString()
    val _argCount: Int = 1 + _inputSize
    val _statement: RoomSQLiteQuery = acquire(_sql, _argCount)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, bizId)
    _argIndex = 2
    for (_item: Long in bookIds) {
      _statement.bindLong(_argIndex, _item)
      _argIndex++
    }
    return CoroutinesRoom.createFlow(__db, false, arrayOf("books"), object :
        Callable<List<BookEntity>> {
      public override fun call(): List<BookEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfBusinessId: Int = getColumnIndexOrThrow(_cursor, "businessId")
          val _cursorIndexOfName: Int = getColumnIndexOrThrow(_cursor, "name")
          val _cursorIndexOfBalance: Int = getColumnIndexOrThrow(_cursor, "balance")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _result: MutableList<BookEntity> = ArrayList<BookEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item_1: BookEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpBusinessId: Long
            _tmpBusinessId = _cursor.getLong(_cursorIndexOfBusinessId)
            val _tmpName: String
            _tmpName = _cursor.getString(_cursorIndexOfName)
            val _tmpBalance: Double
            _tmpBalance = _cursor.getDouble(_cursorIndexOfBalance)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            _item_1 =
                BookEntity(_tmpId,_tmpBusinessId,_tmpName,_tmpBalance,_tmpUpdatedAt,_tmpCreatedAt)
            _result.add(_item_1)
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

  public override suspend fun getBookById(id: Long): BookEntity? {
    val _sql: String = "SELECT * FROM books WHERE id = ?"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, id)
    val _cancellationSignal: CancellationSignal? = createCancellationSignal()
    return execute(__db, false, _cancellationSignal, object : Callable<BookEntity?> {
      public override fun call(): BookEntity? {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfBusinessId: Int = getColumnIndexOrThrow(_cursor, "businessId")
          val _cursorIndexOfName: Int = getColumnIndexOrThrow(_cursor, "name")
          val _cursorIndexOfBalance: Int = getColumnIndexOrThrow(_cursor, "balance")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _result: BookEntity?
          if (_cursor.moveToFirst()) {
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpBusinessId: Long
            _tmpBusinessId = _cursor.getLong(_cursorIndexOfBusinessId)
            val _tmpName: String
            _tmpName = _cursor.getString(_cursorIndexOfName)
            val _tmpBalance: Double
            _tmpBalance = _cursor.getDouble(_cursorIndexOfBalance)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            _result =
                BookEntity(_tmpId,_tmpBusinessId,_tmpName,_tmpBalance,_tmpUpdatedAt,_tmpCreatedAt)
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

  public override fun getMembersForBook(bookId: Long): Flow<List<BookMemberEntity>> {
    val _sql: String = "SELECT * FROM book_members WHERE bookId = ?"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, bookId)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("book_members"), object :
        Callable<List<BookMemberEntity>> {
      public override fun call(): List<BookMemberEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfBookId: Int = getColumnIndexOrThrow(_cursor, "bookId")
          val _cursorIndexOfUserIdentifier: Int = getColumnIndexOrThrow(_cursor, "userIdentifier")
          val _cursorIndexOfDisplayName: Int = getColumnIndexOrThrow(_cursor, "displayName")
          val _cursorIndexOfRole: Int = getColumnIndexOrThrow(_cursor, "role")
          val _cursorIndexOfJoinedAt: Int = getColumnIndexOrThrow(_cursor, "joinedAt")
          val _result: MutableList<BookMemberEntity> =
              ArrayList<BookMemberEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: BookMemberEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpBookId: Long
            _tmpBookId = _cursor.getLong(_cursorIndexOfBookId)
            val _tmpUserIdentifier: String
            _tmpUserIdentifier = _cursor.getString(_cursorIndexOfUserIdentifier)
            val _tmpDisplayName: String
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName)
            val _tmpRole: String
            _tmpRole = _cursor.getString(_cursorIndexOfRole)
            val _tmpJoinedAt: Long
            _tmpJoinedAt = _cursor.getLong(_cursorIndexOfJoinedAt)
            _item =
                BookMemberEntity(_tmpId,_tmpBookId,_tmpUserIdentifier,_tmpDisplayName,_tmpRole,_tmpJoinedAt)
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

  public override suspend fun getUserRole(bookId: Long, userIdentifier: String): String? {
    val _sql: String = "SELECT role FROM book_members WHERE bookId = ? AND userIdentifier = ?"
    val _statement: RoomSQLiteQuery = acquire(_sql, 2)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, bookId)
    _argIndex = 2
    _statement.bindString(_argIndex, userIdentifier)
    val _cancellationSignal: CancellationSignal? = createCancellationSignal()
    return execute(__db, false, _cancellationSignal, object : Callable<String?> {
      public override fun call(): String? {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _result: String?
          if (_cursor.moveToFirst()) {
            if (_cursor.isNull(0)) {
              _result = null
            } else {
              _result = _cursor.getString(0)
            }
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
