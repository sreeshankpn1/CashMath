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
public class TransactionDao_Impl(
  __db: RoomDatabase,
) : TransactionDao {
  private val __db: RoomDatabase

  private val __insertionAdapterOfTransactionEntity: EntityInsertionAdapter<TransactionEntity>

  private val __converters: Converters = Converters()

  private val __updateAdapterOfTransactionEntity: EntityDeletionOrUpdateAdapter<TransactionEntity>

  private val __preparedStmtOfDeleteTransaction: SharedSQLiteStatement

  private val __preparedStmtOfDeleteById: SharedSQLiteStatement

  private val __preparedStmtOfSoftDeleteTransaction: SharedSQLiteStatement
  init {
    this.__db = __db
    this.__insertionAdapterOfTransactionEntity = object :
        EntityInsertionAdapter<TransactionEntity>(__db) {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `transactions` (`id`,`bookId`,`amount`,`type`,`partyName`,`remark`,`category`,`paymentMode`,`attachmentUri`,`transactionTimestamp`,`createdAt`,`updatedAt`,`createdByName`,`createdByRole`,`isDeleted`,`deletedAt`,`deletionReason`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement, entity: TransactionEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.bookId)
        statement.bindDouble(3, entity.amount)
        val _tmp: String = __converters.fromTransactionType(entity.type)
        statement.bindString(4, _tmp)
        val _tmpPartyName: String? = entity.partyName
        if (_tmpPartyName == null) {
          statement.bindNull(5)
        } else {
          statement.bindString(5, _tmpPartyName)
        }
        val _tmpRemark: String? = entity.remark
        if (_tmpRemark == null) {
          statement.bindNull(6)
        } else {
          statement.bindString(6, _tmpRemark)
        }
        val _tmpCategory: String? = entity.category
        if (_tmpCategory == null) {
          statement.bindNull(7)
        } else {
          statement.bindString(7, _tmpCategory)
        }
        statement.bindString(8, entity.paymentMode)
        val _tmpAttachmentUri: String? = entity.attachmentUri
        if (_tmpAttachmentUri == null) {
          statement.bindNull(9)
        } else {
          statement.bindString(9, _tmpAttachmentUri)
        }
        statement.bindLong(10, entity.transactionTimestamp)
        statement.bindLong(11, entity.createdAt)
        statement.bindLong(12, entity.updatedAt)
        statement.bindString(13, entity.createdByName)
        statement.bindString(14, entity.createdByRole)
        val _tmp_1: Int = if (entity.isDeleted) 1 else 0
        statement.bindLong(15, _tmp_1.toLong())
        val _tmpDeletedAt: Long? = entity.deletedAt
        if (_tmpDeletedAt == null) {
          statement.bindNull(16)
        } else {
          statement.bindLong(16, _tmpDeletedAt)
        }
        val _tmpDeletionReason: String? = entity.deletionReason
        if (_tmpDeletionReason == null) {
          statement.bindNull(17)
        } else {
          statement.bindString(17, _tmpDeletionReason)
        }
      }
    }
    this.__updateAdapterOfTransactionEntity = object :
        EntityDeletionOrUpdateAdapter<TransactionEntity>(__db) {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `transactions` SET `id` = ?,`bookId` = ?,`amount` = ?,`type` = ?,`partyName` = ?,`remark` = ?,`category` = ?,`paymentMode` = ?,`attachmentUri` = ?,`transactionTimestamp` = ?,`createdAt` = ?,`updatedAt` = ?,`createdByName` = ?,`createdByRole` = ?,`isDeleted` = ?,`deletedAt` = ?,`deletionReason` = ? WHERE `id` = ?"

      protected override fun bind(statement: SupportSQLiteStatement, entity: TransactionEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.bookId)
        statement.bindDouble(3, entity.amount)
        val _tmp: String = __converters.fromTransactionType(entity.type)
        statement.bindString(4, _tmp)
        val _tmpPartyName: String? = entity.partyName
        if (_tmpPartyName == null) {
          statement.bindNull(5)
        } else {
          statement.bindString(5, _tmpPartyName)
        }
        val _tmpRemark: String? = entity.remark
        if (_tmpRemark == null) {
          statement.bindNull(6)
        } else {
          statement.bindString(6, _tmpRemark)
        }
        val _tmpCategory: String? = entity.category
        if (_tmpCategory == null) {
          statement.bindNull(7)
        } else {
          statement.bindString(7, _tmpCategory)
        }
        statement.bindString(8, entity.paymentMode)
        val _tmpAttachmentUri: String? = entity.attachmentUri
        if (_tmpAttachmentUri == null) {
          statement.bindNull(9)
        } else {
          statement.bindString(9, _tmpAttachmentUri)
        }
        statement.bindLong(10, entity.transactionTimestamp)
        statement.bindLong(11, entity.createdAt)
        statement.bindLong(12, entity.updatedAt)
        statement.bindString(13, entity.createdByName)
        statement.bindString(14, entity.createdByRole)
        val _tmp_1: Int = if (entity.isDeleted) 1 else 0
        statement.bindLong(15, _tmp_1.toLong())
        val _tmpDeletedAt: Long? = entity.deletedAt
        if (_tmpDeletedAt == null) {
          statement.bindNull(16)
        } else {
          statement.bindLong(16, _tmpDeletedAt)
        }
        val _tmpDeletionReason: String? = entity.deletionReason
        if (_tmpDeletionReason == null) {
          statement.bindNull(17)
        } else {
          statement.bindString(17, _tmpDeletionReason)
        }
        statement.bindLong(18, entity.id)
      }
    }
    this.__preparedStmtOfDeleteTransaction = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String = "DELETE FROM transactions WHERE id = ?"
        return _query
      }
    }
    this.__preparedStmtOfDeleteById = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String = "DELETE FROM transactions WHERE id = ?"
        return _query
      }
    }
    this.__preparedStmtOfSoftDeleteTransaction = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String =
            "UPDATE transactions SET isDeleted = 1, deletedAt = ?, deletionReason = ? WHERE id = ?"
        return _query
      }
    }
  }

  public override suspend fun insert(transaction: TransactionEntity): Long =
      CoroutinesRoom.execute(__db, true, object : Callable<Long> {
    public override fun call(): Long {
      __db.beginTransaction()
      try {
        val _result: Long = __insertionAdapterOfTransactionEntity.insertAndReturnId(transaction)
        __db.setTransactionSuccessful()
        return _result
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long>
      = CoroutinesRoom.execute(__db, true, object : Callable<List<Long>> {
    public override fun call(): List<Long> {
      __db.beginTransaction()
      try {
        val _result: List<Long> =
            __insertionAdapterOfTransactionEntity.insertAndReturnIdsList(transactions)
        __db.setTransactionSuccessful()
        return _result
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun update(transaction: TransactionEntity): Int =
      CoroutinesRoom.execute(__db, true, object : Callable<Int> {
    public override fun call(): Int {
      var _total: Int = 0
      __db.beginTransaction()
      try {
        _total += __updateAdapterOfTransactionEntity.handle(transaction)
        __db.setTransactionSuccessful()
        return _total
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun updateTransaction(transaction: TransactionEntity): Int =
      CoroutinesRoom.execute(__db, true, object : Callable<Int> {
    public override fun call(): Int {
      var _total: Int = 0
      __db.beginTransaction()
      try {
        _total += __updateAdapterOfTransactionEntity.handle(transaction)
        __db.setTransactionSuccessful()
        return _total
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun deleteTransaction(id: Long): Int = CoroutinesRoom.execute(__db, true,
      object : Callable<Int> {
    public override fun call(): Int {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfDeleteTransaction.acquire()
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
        __preparedStmtOfDeleteTransaction.release(_stmt)
      }
    }
  })

  public override suspend fun deleteById(transactionId: Long): Int = CoroutinesRoom.execute(__db,
      true, object : Callable<Int> {
    public override fun call(): Int {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfDeleteById.acquire()
      var _argIndex: Int = 1
      _stmt.bindLong(_argIndex, transactionId)
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
        __preparedStmtOfDeleteById.release(_stmt)
      }
    }
  })

  public override suspend fun softDeleteTransaction(
    id: Long,
    timestamp: Long,
    reason: String?,
  ): Int = CoroutinesRoom.execute(__db, true, object : Callable<Int> {
    public override fun call(): Int {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfSoftDeleteTransaction.acquire()
      var _argIndex: Int = 1
      _stmt.bindLong(_argIndex, timestamp)
      _argIndex = 2
      if (reason == null) {
        _stmt.bindNull(_argIndex)
      } else {
        _stmt.bindString(_argIndex, reason)
      }
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
        __preparedStmtOfSoftDeleteTransaction.release(_stmt)
      }
    }
  })

  public override fun getTransactionsByBook(bookId: Long): Flow<List<TransactionEntity>> {
    val _sql: String =
        "SELECT * FROM transactions WHERE bookId = ? AND isDeleted = 0 ORDER BY transactionTimestamp DESC"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, bookId)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("transactions"), object :
        Callable<List<TransactionEntity>> {
      public override fun call(): List<TransactionEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfBookId: Int = getColumnIndexOrThrow(_cursor, "bookId")
          val _cursorIndexOfAmount: Int = getColumnIndexOrThrow(_cursor, "amount")
          val _cursorIndexOfType: Int = getColumnIndexOrThrow(_cursor, "type")
          val _cursorIndexOfPartyName: Int = getColumnIndexOrThrow(_cursor, "partyName")
          val _cursorIndexOfRemark: Int = getColumnIndexOrThrow(_cursor, "remark")
          val _cursorIndexOfCategory: Int = getColumnIndexOrThrow(_cursor, "category")
          val _cursorIndexOfPaymentMode: Int = getColumnIndexOrThrow(_cursor, "paymentMode")
          val _cursorIndexOfAttachmentUri: Int = getColumnIndexOrThrow(_cursor, "attachmentUri")
          val _cursorIndexOfTransactionTimestamp: Int = getColumnIndexOrThrow(_cursor,
              "transactionTimestamp")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _cursorIndexOfCreatedByName: Int = getColumnIndexOrThrow(_cursor, "createdByName")
          val _cursorIndexOfCreatedByRole: Int = getColumnIndexOrThrow(_cursor, "createdByRole")
          val _cursorIndexOfIsDeleted: Int = getColumnIndexOrThrow(_cursor, "isDeleted")
          val _cursorIndexOfDeletedAt: Int = getColumnIndexOrThrow(_cursor, "deletedAt")
          val _cursorIndexOfDeletionReason: Int = getColumnIndexOrThrow(_cursor, "deletionReason")
          val _result: MutableList<TransactionEntity> =
              ArrayList<TransactionEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: TransactionEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpBookId: Long
            _tmpBookId = _cursor.getLong(_cursorIndexOfBookId)
            val _tmpAmount: Double
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount)
            val _tmpType: TransactionType
            val _tmp: String
            _tmp = _cursor.getString(_cursorIndexOfType)
            _tmpType = __converters.toTransactionType(_tmp)
            val _tmpPartyName: String?
            if (_cursor.isNull(_cursorIndexOfPartyName)) {
              _tmpPartyName = null
            } else {
              _tmpPartyName = _cursor.getString(_cursorIndexOfPartyName)
            }
            val _tmpRemark: String?
            if (_cursor.isNull(_cursorIndexOfRemark)) {
              _tmpRemark = null
            } else {
              _tmpRemark = _cursor.getString(_cursorIndexOfRemark)
            }
            val _tmpCategory: String?
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory)
            }
            val _tmpPaymentMode: String
            _tmpPaymentMode = _cursor.getString(_cursorIndexOfPaymentMode)
            val _tmpAttachmentUri: String?
            if (_cursor.isNull(_cursorIndexOfAttachmentUri)) {
              _tmpAttachmentUri = null
            } else {
              _tmpAttachmentUri = _cursor.getString(_cursorIndexOfAttachmentUri)
            }
            val _tmpTransactionTimestamp: Long
            _tmpTransactionTimestamp = _cursor.getLong(_cursorIndexOfTransactionTimestamp)
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            val _tmpCreatedByName: String
            _tmpCreatedByName = _cursor.getString(_cursorIndexOfCreatedByName)
            val _tmpCreatedByRole: String
            _tmpCreatedByRole = _cursor.getString(_cursorIndexOfCreatedByRole)
            val _tmpIsDeleted: Boolean
            val _tmp_1: Int
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted)
            _tmpIsDeleted = _tmp_1 != 0
            val _tmpDeletedAt: Long?
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt)
            }
            val _tmpDeletionReason: String?
            if (_cursor.isNull(_cursorIndexOfDeletionReason)) {
              _tmpDeletionReason = null
            } else {
              _tmpDeletionReason = _cursor.getString(_cursorIndexOfDeletionReason)
            }
            _item =
                TransactionEntity(_tmpId,_tmpBookId,_tmpAmount,_tmpType,_tmpPartyName,_tmpRemark,_tmpCategory,_tmpPaymentMode,_tmpAttachmentUri,_tmpTransactionTimestamp,_tmpCreatedAt,_tmpUpdatedAt,_tmpCreatedByName,_tmpCreatedByRole,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletionReason)
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

  public override fun getTransactionsForBook(bookId: Long): Flow<List<TransactionEntity>> {
    val _sql: String =
        "SELECT * FROM transactions WHERE bookId = ? AND isDeleted = 0 ORDER BY transactionTimestamp DESC"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, bookId)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("transactions"), object :
        Callable<List<TransactionEntity>> {
      public override fun call(): List<TransactionEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfBookId: Int = getColumnIndexOrThrow(_cursor, "bookId")
          val _cursorIndexOfAmount: Int = getColumnIndexOrThrow(_cursor, "amount")
          val _cursorIndexOfType: Int = getColumnIndexOrThrow(_cursor, "type")
          val _cursorIndexOfPartyName: Int = getColumnIndexOrThrow(_cursor, "partyName")
          val _cursorIndexOfRemark: Int = getColumnIndexOrThrow(_cursor, "remark")
          val _cursorIndexOfCategory: Int = getColumnIndexOrThrow(_cursor, "category")
          val _cursorIndexOfPaymentMode: Int = getColumnIndexOrThrow(_cursor, "paymentMode")
          val _cursorIndexOfAttachmentUri: Int = getColumnIndexOrThrow(_cursor, "attachmentUri")
          val _cursorIndexOfTransactionTimestamp: Int = getColumnIndexOrThrow(_cursor,
              "transactionTimestamp")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _cursorIndexOfCreatedByName: Int = getColumnIndexOrThrow(_cursor, "createdByName")
          val _cursorIndexOfCreatedByRole: Int = getColumnIndexOrThrow(_cursor, "createdByRole")
          val _cursorIndexOfIsDeleted: Int = getColumnIndexOrThrow(_cursor, "isDeleted")
          val _cursorIndexOfDeletedAt: Int = getColumnIndexOrThrow(_cursor, "deletedAt")
          val _cursorIndexOfDeletionReason: Int = getColumnIndexOrThrow(_cursor, "deletionReason")
          val _result: MutableList<TransactionEntity> =
              ArrayList<TransactionEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: TransactionEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpBookId: Long
            _tmpBookId = _cursor.getLong(_cursorIndexOfBookId)
            val _tmpAmount: Double
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount)
            val _tmpType: TransactionType
            val _tmp: String
            _tmp = _cursor.getString(_cursorIndexOfType)
            _tmpType = __converters.toTransactionType(_tmp)
            val _tmpPartyName: String?
            if (_cursor.isNull(_cursorIndexOfPartyName)) {
              _tmpPartyName = null
            } else {
              _tmpPartyName = _cursor.getString(_cursorIndexOfPartyName)
            }
            val _tmpRemark: String?
            if (_cursor.isNull(_cursorIndexOfRemark)) {
              _tmpRemark = null
            } else {
              _tmpRemark = _cursor.getString(_cursorIndexOfRemark)
            }
            val _tmpCategory: String?
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory)
            }
            val _tmpPaymentMode: String
            _tmpPaymentMode = _cursor.getString(_cursorIndexOfPaymentMode)
            val _tmpAttachmentUri: String?
            if (_cursor.isNull(_cursorIndexOfAttachmentUri)) {
              _tmpAttachmentUri = null
            } else {
              _tmpAttachmentUri = _cursor.getString(_cursorIndexOfAttachmentUri)
            }
            val _tmpTransactionTimestamp: Long
            _tmpTransactionTimestamp = _cursor.getLong(_cursorIndexOfTransactionTimestamp)
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            val _tmpCreatedByName: String
            _tmpCreatedByName = _cursor.getString(_cursorIndexOfCreatedByName)
            val _tmpCreatedByRole: String
            _tmpCreatedByRole = _cursor.getString(_cursorIndexOfCreatedByRole)
            val _tmpIsDeleted: Boolean
            val _tmp_1: Int
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted)
            _tmpIsDeleted = _tmp_1 != 0
            val _tmpDeletedAt: Long?
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt)
            }
            val _tmpDeletionReason: String?
            if (_cursor.isNull(_cursorIndexOfDeletionReason)) {
              _tmpDeletionReason = null
            } else {
              _tmpDeletionReason = _cursor.getString(_cursorIndexOfDeletionReason)
            }
            _item =
                TransactionEntity(_tmpId,_tmpBookId,_tmpAmount,_tmpType,_tmpPartyName,_tmpRemark,_tmpCategory,_tmpPaymentMode,_tmpAttachmentUri,_tmpTransactionTimestamp,_tmpCreatedAt,_tmpUpdatedAt,_tmpCreatedByName,_tmpCreatedByRole,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletionReason)
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

  public override fun getTotalExpenses(bookId: Long): Flow<Double?> {
    val _sql: String =
        "SELECT SUM(amount) FROM transactions WHERE bookId = ? AND type = 'EXPENSE' AND isDeleted = 0"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, bookId)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("transactions"), object :
        Callable<Double?> {
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

  public override fun getTotalIncome(bookId: Long): Flow<Double?> {
    val _sql: String =
        "SELECT SUM(amount) FROM transactions WHERE bookId = ? AND type = 'INCOME' AND isDeleted = 0"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, bookId)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("transactions"), object :
        Callable<Double?> {
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

  public override fun getTransactionsInRange(
    bookId: Long,
    start: Long,
    end: Long,
  ): Flow<List<TransactionEntity>> {
    val _sql: String =
        "SELECT * FROM transactions WHERE bookId = ? AND transactionTimestamp BETWEEN ? AND ? AND isDeleted = 0 ORDER BY transactionTimestamp DESC"
    val _statement: RoomSQLiteQuery = acquire(_sql, 3)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, bookId)
    _argIndex = 2
    _statement.bindLong(_argIndex, start)
    _argIndex = 3
    _statement.bindLong(_argIndex, end)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("transactions"), object :
        Callable<List<TransactionEntity>> {
      public override fun call(): List<TransactionEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfBookId: Int = getColumnIndexOrThrow(_cursor, "bookId")
          val _cursorIndexOfAmount: Int = getColumnIndexOrThrow(_cursor, "amount")
          val _cursorIndexOfType: Int = getColumnIndexOrThrow(_cursor, "type")
          val _cursorIndexOfPartyName: Int = getColumnIndexOrThrow(_cursor, "partyName")
          val _cursorIndexOfRemark: Int = getColumnIndexOrThrow(_cursor, "remark")
          val _cursorIndexOfCategory: Int = getColumnIndexOrThrow(_cursor, "category")
          val _cursorIndexOfPaymentMode: Int = getColumnIndexOrThrow(_cursor, "paymentMode")
          val _cursorIndexOfAttachmentUri: Int = getColumnIndexOrThrow(_cursor, "attachmentUri")
          val _cursorIndexOfTransactionTimestamp: Int = getColumnIndexOrThrow(_cursor,
              "transactionTimestamp")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
          val _cursorIndexOfCreatedByName: Int = getColumnIndexOrThrow(_cursor, "createdByName")
          val _cursorIndexOfCreatedByRole: Int = getColumnIndexOrThrow(_cursor, "createdByRole")
          val _cursorIndexOfIsDeleted: Int = getColumnIndexOrThrow(_cursor, "isDeleted")
          val _cursorIndexOfDeletedAt: Int = getColumnIndexOrThrow(_cursor, "deletedAt")
          val _cursorIndexOfDeletionReason: Int = getColumnIndexOrThrow(_cursor, "deletionReason")
          val _result: MutableList<TransactionEntity> =
              ArrayList<TransactionEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: TransactionEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpBookId: Long
            _tmpBookId = _cursor.getLong(_cursorIndexOfBookId)
            val _tmpAmount: Double
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount)
            val _tmpType: TransactionType
            val _tmp: String
            _tmp = _cursor.getString(_cursorIndexOfType)
            _tmpType = __converters.toTransactionType(_tmp)
            val _tmpPartyName: String?
            if (_cursor.isNull(_cursorIndexOfPartyName)) {
              _tmpPartyName = null
            } else {
              _tmpPartyName = _cursor.getString(_cursorIndexOfPartyName)
            }
            val _tmpRemark: String?
            if (_cursor.isNull(_cursorIndexOfRemark)) {
              _tmpRemark = null
            } else {
              _tmpRemark = _cursor.getString(_cursorIndexOfRemark)
            }
            val _tmpCategory: String?
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory)
            }
            val _tmpPaymentMode: String
            _tmpPaymentMode = _cursor.getString(_cursorIndexOfPaymentMode)
            val _tmpAttachmentUri: String?
            if (_cursor.isNull(_cursorIndexOfAttachmentUri)) {
              _tmpAttachmentUri = null
            } else {
              _tmpAttachmentUri = _cursor.getString(_cursorIndexOfAttachmentUri)
            }
            val _tmpTransactionTimestamp: Long
            _tmpTransactionTimestamp = _cursor.getLong(_cursorIndexOfTransactionTimestamp)
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            val _tmpUpdatedAt: Long
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
            val _tmpCreatedByName: String
            _tmpCreatedByName = _cursor.getString(_cursorIndexOfCreatedByName)
            val _tmpCreatedByRole: String
            _tmpCreatedByRole = _cursor.getString(_cursorIndexOfCreatedByRole)
            val _tmpIsDeleted: Boolean
            val _tmp_1: Int
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted)
            _tmpIsDeleted = _tmp_1 != 0
            val _tmpDeletedAt: Long?
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt)
            }
            val _tmpDeletionReason: String?
            if (_cursor.isNull(_cursorIndexOfDeletionReason)) {
              _tmpDeletionReason = null
            } else {
              _tmpDeletionReason = _cursor.getString(_cursorIndexOfDeletionReason)
            }
            _item =
                TransactionEntity(_tmpId,_tmpBookId,_tmpAmount,_tmpType,_tmpPartyName,_tmpRemark,_tmpCategory,_tmpPaymentMode,_tmpAttachmentUri,_tmpTransactionTimestamp,_tmpCreatedAt,_tmpUpdatedAt,_tmpCreatedByName,_tmpCreatedByRole,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletionReason)
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

  public override fun getCategorySummaries(
    bookId: Long,
    start: Long,
    end: Long,
  ): Flow<List<CategorySummary>> {
    val _sql: String =
        "SELECT category, SUM(amount) as total FROM transactions WHERE bookId = ? AND isDeleted = 0 AND transactionTimestamp BETWEEN ? AND ? GROUP BY category ORDER BY total DESC"
    val _statement: RoomSQLiteQuery = acquire(_sql, 3)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, bookId)
    _argIndex = 2
    _statement.bindLong(_argIndex, start)
    _argIndex = 3
    _statement.bindLong(_argIndex, end)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("transactions"), object :
        Callable<List<CategorySummary>> {
      public override fun call(): List<CategorySummary> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfCategory: Int = 0
          val _cursorIndexOfTotal: Int = 1
          val _result: MutableList<CategorySummary> = ArrayList<CategorySummary>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: CategorySummary
            val _tmpCategory: String
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory)
            val _tmpTotal: Double
            _tmpTotal = _cursor.getDouble(_cursorIndexOfTotal)
            _item = CategorySummary(_tmpCategory,_tmpTotal)
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

  public override fun getPartySummaries(
    bookId: Long,
    start: Long,
    end: Long,
  ): Flow<List<PartySummary>> {
    val _sql: String =
        "SELECT partyName as merchantOrPayee, SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as totalIn, SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as totalOut FROM transactions WHERE bookId = ? AND isDeleted = 0 AND transactionTimestamp BETWEEN ? AND ? GROUP BY partyName ORDER BY (totalIn + totalOut) DESC"
    val _statement: RoomSQLiteQuery = acquire(_sql, 3)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, bookId)
    _argIndex = 2
    _statement.bindLong(_argIndex, start)
    _argIndex = 3
    _statement.bindLong(_argIndex, end)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("transactions"), object :
        Callable<List<PartySummary>> {
      public override fun call(): List<PartySummary> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfMerchantOrPayee: Int = 0
          val _cursorIndexOfTotalIn: Int = 1
          val _cursorIndexOfTotalOut: Int = 2
          val _result: MutableList<PartySummary> = ArrayList<PartySummary>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: PartySummary
            val _tmpMerchantOrPayee: String
            _tmpMerchantOrPayee = _cursor.getString(_cursorIndexOfMerchantOrPayee)
            val _tmpTotalIn: Double
            _tmpTotalIn = _cursor.getDouble(_cursorIndexOfTotalIn)
            val _tmpTotalOut: Double
            _tmpTotalOut = _cursor.getDouble(_cursorIndexOfTotalOut)
            _item = PartySummary(_tmpMerchantOrPayee,_tmpTotalIn,_tmpTotalOut)
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

  public override fun getPaymentModeSummaries(
    bookId: Long,
    start: Long,
    end: Long,
  ): Flow<List<PaymentModeSummary>> {
    val _sql: String =
        "SELECT paymentMode as sourceApp, SUM(amount) as total FROM transactions WHERE bookId = ? AND isDeleted = 0 AND transactionTimestamp BETWEEN ? AND ? GROUP BY paymentMode ORDER BY total DESC"
    val _statement: RoomSQLiteQuery = acquire(_sql, 3)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, bookId)
    _argIndex = 2
    _statement.bindLong(_argIndex, start)
    _argIndex = 3
    _statement.bindLong(_argIndex, end)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("transactions"), object :
        Callable<List<PaymentModeSummary>> {
      public override fun call(): List<PaymentModeSummary> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfSourceApp: Int = 0
          val _cursorIndexOfTotal: Int = 1
          val _result: MutableList<PaymentModeSummary> =
              ArrayList<PaymentModeSummary>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: PaymentModeSummary
            val _tmpSourceApp: String
            _tmpSourceApp = _cursor.getString(_cursorIndexOfSourceApp)
            val _tmpTotal: Double
            _tmpTotal = _cursor.getDouble(_cursorIndexOfTotal)
            _item = PaymentModeSummary(_tmpSourceApp,_tmpTotal)
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

  public override fun getExpensesInRange(
    bookId: Long,
    start: Long,
    end: Long,
  ): Flow<Double?> {
    val _sql: String =
        "SELECT SUM(amount) FROM transactions WHERE bookId = ? AND type = 'EXPENSE' AND isDeleted = 0 AND transactionTimestamp BETWEEN ? AND ?"
    val _statement: RoomSQLiteQuery = acquire(_sql, 3)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, bookId)
    _argIndex = 2
    _statement.bindLong(_argIndex, start)
    _argIndex = 3
    _statement.bindLong(_argIndex, end)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("transactions"), object :
        Callable<Double?> {
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

  public override fun getIncomeInRange(
    bookId: Long,
    start: Long,
    end: Long,
  ): Flow<Double?> {
    val _sql: String =
        "SELECT SUM(amount) FROM transactions WHERE bookId = ? AND type = 'INCOME' AND isDeleted = 0 AND transactionTimestamp BETWEEN ? AND ?"
    val _statement: RoomSQLiteQuery = acquire(_sql, 3)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, bookId)
    _argIndex = 2
    _statement.bindLong(_argIndex, start)
    _argIndex = 3
    _statement.bindLong(_argIndex, end)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("transactions"), object :
        Callable<Double?> {
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

  public override fun getTransactionsBetweenSync(start: Long, end: Long): List<TransactionEntity> {
    val _sql: String =
        "SELECT * FROM transactions WHERE transactionTimestamp BETWEEN ? AND ? AND isDeleted = 0"
    val _statement: RoomSQLiteQuery = acquire(_sql, 2)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, start)
    _argIndex = 2
    _statement.bindLong(_argIndex, end)
    __db.assertNotSuspendingTransaction()
    val _cursor: Cursor = query(__db, _statement, false, null)
    try {
      val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
      val _cursorIndexOfBookId: Int = getColumnIndexOrThrow(_cursor, "bookId")
      val _cursorIndexOfAmount: Int = getColumnIndexOrThrow(_cursor, "amount")
      val _cursorIndexOfType: Int = getColumnIndexOrThrow(_cursor, "type")
      val _cursorIndexOfPartyName: Int = getColumnIndexOrThrow(_cursor, "partyName")
      val _cursorIndexOfRemark: Int = getColumnIndexOrThrow(_cursor, "remark")
      val _cursorIndexOfCategory: Int = getColumnIndexOrThrow(_cursor, "category")
      val _cursorIndexOfPaymentMode: Int = getColumnIndexOrThrow(_cursor, "paymentMode")
      val _cursorIndexOfAttachmentUri: Int = getColumnIndexOrThrow(_cursor, "attachmentUri")
      val _cursorIndexOfTransactionTimestamp: Int = getColumnIndexOrThrow(_cursor,
          "transactionTimestamp")
      val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
      val _cursorIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_cursor, "updatedAt")
      val _cursorIndexOfCreatedByName: Int = getColumnIndexOrThrow(_cursor, "createdByName")
      val _cursorIndexOfCreatedByRole: Int = getColumnIndexOrThrow(_cursor, "createdByRole")
      val _cursorIndexOfIsDeleted: Int = getColumnIndexOrThrow(_cursor, "isDeleted")
      val _cursorIndexOfDeletedAt: Int = getColumnIndexOrThrow(_cursor, "deletedAt")
      val _cursorIndexOfDeletionReason: Int = getColumnIndexOrThrow(_cursor, "deletionReason")
      val _result: MutableList<TransactionEntity> = ArrayList<TransactionEntity>(_cursor.getCount())
      while (_cursor.moveToNext()) {
        val _item: TransactionEntity
        val _tmpId: Long
        _tmpId = _cursor.getLong(_cursorIndexOfId)
        val _tmpBookId: Long
        _tmpBookId = _cursor.getLong(_cursorIndexOfBookId)
        val _tmpAmount: Double
        _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount)
        val _tmpType: TransactionType
        val _tmp: String
        _tmp = _cursor.getString(_cursorIndexOfType)
        _tmpType = __converters.toTransactionType(_tmp)
        val _tmpPartyName: String?
        if (_cursor.isNull(_cursorIndexOfPartyName)) {
          _tmpPartyName = null
        } else {
          _tmpPartyName = _cursor.getString(_cursorIndexOfPartyName)
        }
        val _tmpRemark: String?
        if (_cursor.isNull(_cursorIndexOfRemark)) {
          _tmpRemark = null
        } else {
          _tmpRemark = _cursor.getString(_cursorIndexOfRemark)
        }
        val _tmpCategory: String?
        if (_cursor.isNull(_cursorIndexOfCategory)) {
          _tmpCategory = null
        } else {
          _tmpCategory = _cursor.getString(_cursorIndexOfCategory)
        }
        val _tmpPaymentMode: String
        _tmpPaymentMode = _cursor.getString(_cursorIndexOfPaymentMode)
        val _tmpAttachmentUri: String?
        if (_cursor.isNull(_cursorIndexOfAttachmentUri)) {
          _tmpAttachmentUri = null
        } else {
          _tmpAttachmentUri = _cursor.getString(_cursorIndexOfAttachmentUri)
        }
        val _tmpTransactionTimestamp: Long
        _tmpTransactionTimestamp = _cursor.getLong(_cursorIndexOfTransactionTimestamp)
        val _tmpCreatedAt: Long
        _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
        val _tmpUpdatedAt: Long
        _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt)
        val _tmpCreatedByName: String
        _tmpCreatedByName = _cursor.getString(_cursorIndexOfCreatedByName)
        val _tmpCreatedByRole: String
        _tmpCreatedByRole = _cursor.getString(_cursorIndexOfCreatedByRole)
        val _tmpIsDeleted: Boolean
        val _tmp_1: Int
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsDeleted)
        _tmpIsDeleted = _tmp_1 != 0
        val _tmpDeletedAt: Long?
        if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
          _tmpDeletedAt = null
        } else {
          _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt)
        }
        val _tmpDeletionReason: String?
        if (_cursor.isNull(_cursorIndexOfDeletionReason)) {
          _tmpDeletionReason = null
        } else {
          _tmpDeletionReason = _cursor.getString(_cursorIndexOfDeletionReason)
        }
        _item =
            TransactionEntity(_tmpId,_tmpBookId,_tmpAmount,_tmpType,_tmpPartyName,_tmpRemark,_tmpCategory,_tmpPaymentMode,_tmpAttachmentUri,_tmpTransactionTimestamp,_tmpCreatedAt,_tmpUpdatedAt,_tmpCreatedByName,_tmpCreatedByRole,_tmpIsDeleted,_tmpDeletedAt,_tmpDeletionReason)
        _result.add(_item)
      }
      return _result
    } finally {
      _cursor.close()
      _statement.release()
    }
  }

  public companion object {
    @JvmStatic
    public fun getRequiredConverters(): List<Class<*>> = emptyList()
  }
}
