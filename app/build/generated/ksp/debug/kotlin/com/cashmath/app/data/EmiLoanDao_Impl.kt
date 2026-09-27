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
public class EmiLoanDao_Impl(
  __db: RoomDatabase,
) : EmiLoanDao {
  private val __db: RoomDatabase

  private val __insertionAdapterOfEmiLoanEntity: EntityInsertionAdapter<EmiLoanEntity>

  private val __updateAdapterOfEmiLoanEntity: EntityDeletionOrUpdateAdapter<EmiLoanEntity>

  private val __preparedStmtOfIncrementPaidMonth: SharedSQLiteStatement

  private val __preparedStmtOfDelete: SharedSQLiteStatement
  init {
    this.__db = __db
    this.__insertionAdapterOfEmiLoanEntity = object : EntityInsertionAdapter<EmiLoanEntity>(__db) {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `emi_loans` (`id`,`businessId`,`loanName`,`lenderBank`,`monthlyEmi`,`dueDayOfMonth`,`totalTenureMonths`,`paidMonths`,`startDate`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement, entity: EmiLoanEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.businessId)
        statement.bindString(3, entity.loanName)
        statement.bindString(4, entity.lenderBank)
        statement.bindDouble(5, entity.monthlyEmi)
        statement.bindLong(6, entity.dueDayOfMonth.toLong())
        statement.bindLong(7, entity.totalTenureMonths.toLong())
        statement.bindLong(8, entity.paidMonths.toLong())
        statement.bindLong(9, entity.startDate)
      }
    }
    this.__updateAdapterOfEmiLoanEntity = object :
        EntityDeletionOrUpdateAdapter<EmiLoanEntity>(__db) {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `emi_loans` SET `id` = ?,`businessId` = ?,`loanName` = ?,`lenderBank` = ?,`monthlyEmi` = ?,`dueDayOfMonth` = ?,`totalTenureMonths` = ?,`paidMonths` = ?,`startDate` = ? WHERE `id` = ?"

      protected override fun bind(statement: SupportSQLiteStatement, entity: EmiLoanEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.businessId)
        statement.bindString(3, entity.loanName)
        statement.bindString(4, entity.lenderBank)
        statement.bindDouble(5, entity.monthlyEmi)
        statement.bindLong(6, entity.dueDayOfMonth.toLong())
        statement.bindLong(7, entity.totalTenureMonths.toLong())
        statement.bindLong(8, entity.paidMonths.toLong())
        statement.bindLong(9, entity.startDate)
        statement.bindLong(10, entity.id)
      }
    }
    this.__preparedStmtOfIncrementPaidMonth = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String =
            "UPDATE emi_loans SET paidMonths = paidMonths + 1 WHERE id = ? AND paidMonths < totalTenureMonths"
        return _query
      }
    }
    this.__preparedStmtOfDelete = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String = "DELETE FROM emi_loans WHERE id = ?"
        return _query
      }
    }
  }

  public override suspend fun insert(loan: EmiLoanEntity): Long = CoroutinesRoom.execute(__db, true,
      object : Callable<Long> {
    public override fun call(): Long {
      __db.beginTransaction()
      try {
        val _result: Long = __insertionAdapterOfEmiLoanEntity.insertAndReturnId(loan)
        __db.setTransactionSuccessful()
        return _result
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun update(loan: EmiLoanEntity): Int = CoroutinesRoom.execute(__db, true,
      object : Callable<Int> {
    public override fun call(): Int {
      var _total: Int = 0
      __db.beginTransaction()
      try {
        _total += __updateAdapterOfEmiLoanEntity.handle(loan)
        __db.setTransactionSuccessful()
        return _total
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun incrementPaidMonth(id: Long): Int = CoroutinesRoom.execute(__db, true,
      object : Callable<Int> {
    public override fun call(): Int {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfIncrementPaidMonth.acquire()
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
        __preparedStmtOfIncrementPaidMonth.release(_stmt)
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

  public override fun getLoansByBusiness(businessId: Long): Flow<List<EmiLoanEntity>> {
    val _sql: String = "SELECT * FROM emi_loans WHERE businessId = ? ORDER BY dueDayOfMonth ASC"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, businessId)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("emi_loans"), object :
        Callable<List<EmiLoanEntity>> {
      public override fun call(): List<EmiLoanEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfBusinessId: Int = getColumnIndexOrThrow(_cursor, "businessId")
          val _cursorIndexOfLoanName: Int = getColumnIndexOrThrow(_cursor, "loanName")
          val _cursorIndexOfLenderBank: Int = getColumnIndexOrThrow(_cursor, "lenderBank")
          val _cursorIndexOfMonthlyEmi: Int = getColumnIndexOrThrow(_cursor, "monthlyEmi")
          val _cursorIndexOfDueDayOfMonth: Int = getColumnIndexOrThrow(_cursor, "dueDayOfMonth")
          val _cursorIndexOfTotalTenureMonths: Int = getColumnIndexOrThrow(_cursor,
              "totalTenureMonths")
          val _cursorIndexOfPaidMonths: Int = getColumnIndexOrThrow(_cursor, "paidMonths")
          val _cursorIndexOfStartDate: Int = getColumnIndexOrThrow(_cursor, "startDate")
          val _result: MutableList<EmiLoanEntity> = ArrayList<EmiLoanEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: EmiLoanEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpBusinessId: Long
            _tmpBusinessId = _cursor.getLong(_cursorIndexOfBusinessId)
            val _tmpLoanName: String
            _tmpLoanName = _cursor.getString(_cursorIndexOfLoanName)
            val _tmpLenderBank: String
            _tmpLenderBank = _cursor.getString(_cursorIndexOfLenderBank)
            val _tmpMonthlyEmi: Double
            _tmpMonthlyEmi = _cursor.getDouble(_cursorIndexOfMonthlyEmi)
            val _tmpDueDayOfMonth: Int
            _tmpDueDayOfMonth = _cursor.getInt(_cursorIndexOfDueDayOfMonth)
            val _tmpTotalTenureMonths: Int
            _tmpTotalTenureMonths = _cursor.getInt(_cursorIndexOfTotalTenureMonths)
            val _tmpPaidMonths: Int
            _tmpPaidMonths = _cursor.getInt(_cursorIndexOfPaidMonths)
            val _tmpStartDate: Long
            _tmpStartDate = _cursor.getLong(_cursorIndexOfStartDate)
            _item =
                EmiLoanEntity(_tmpId,_tmpBusinessId,_tmpLoanName,_tmpLenderBank,_tmpMonthlyEmi,_tmpDueDayOfMonth,_tmpTotalTenureMonths,_tmpPaidMonths,_tmpStartDate)
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

  public override fun getTotalMonthlyEmi(businessId: Long): Flow<Double?> {
    val _sql: String = "SELECT SUM(monthlyEmi) FROM emi_loans WHERE businessId = ?"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, businessId)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("emi_loans"), object : Callable<Double?> {
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

  public override fun getActiveMonthlyEmiTotal(): Flow<Double> {
    val _sql: String =
        "SELECT COALESCE(SUM(monthlyEmi), 0.0) FROM emi_loans WHERE (totalTenureMonths - paidMonths) > 0"
    val _statement: RoomSQLiteQuery = acquire(_sql, 0)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("emi_loans"), object : Callable<Double> {
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

  public override fun getActiveMonthlyEmiTotal(businessId: Long): Flow<Double> {
    val _sql: String =
        "SELECT COALESCE(SUM(monthlyEmi), 0.0) FROM emi_loans WHERE businessId = ? AND (totalTenureMonths - paidMonths) > 0"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, businessId)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("emi_loans"), object : Callable<Double> {
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

  public override fun getTotalOutstandingEmi(): Flow<Double> {
    val _sql: String =
        "SELECT COALESCE(SUM(monthlyEmi * (totalTenureMonths - paidMonths)), 0.0) FROM emi_loans WHERE (totalTenureMonths - paidMonths) > 0"
    val _statement: RoomSQLiteQuery = acquire(_sql, 0)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("emi_loans"), object : Callable<Double> {
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

  public override fun getTotalOutstandingEmi(businessId: Long): Flow<Double> {
    val _sql: String =
        "SELECT COALESCE(SUM(monthlyEmi * (totalTenureMonths - paidMonths)), 0.0) FROM emi_loans WHERE businessId = ? AND (totalTenureMonths - paidMonths) > 0"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, businessId)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("emi_loans"), object : Callable<Double> {
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
