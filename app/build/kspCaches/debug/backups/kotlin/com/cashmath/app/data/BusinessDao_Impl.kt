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
import java.lang.Class
import java.util.ArrayList
import java.util.concurrent.Callable
import javax.`annotation`.processing.Generated
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
public class BusinessDao_Impl(
  __db: RoomDatabase,
) : BusinessDao {
  private val __db: RoomDatabase

  private val __insertionAdapterOfBusinessEntity: EntityInsertionAdapter<BusinessEntity>

  private val __updateAdapterOfBusinessEntity: EntityDeletionOrUpdateAdapter<BusinessEntity>

  private val __preparedStmtOfUpdateName: SharedSQLiteStatement

  private val __preparedStmtOfDelete: SharedSQLiteStatement

  private val __preparedStmtOfDeleteBusinessCascade: SharedSQLiteStatement
  init {
    this.__db = __db
    this.__insertionAdapterOfBusinessEntity = object : EntityInsertionAdapter<BusinessEntity>(__db)
        {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `businesses` (`id`,`name`,`role`,`logoUri`,`gstNumber`,`category`,`phone`,`email`,`address`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SupportSQLiteStatement, entity: BusinessEntity) {
        statement.bindLong(1, entity.id)
        statement.bindString(2, entity.name)
        statement.bindString(3, entity.role)
        val _tmpLogoUri: String? = entity.logoUri
        if (_tmpLogoUri == null) {
          statement.bindNull(4)
        } else {
          statement.bindString(4, _tmpLogoUri)
        }
        val _tmpGstNumber: String? = entity.gstNumber
        if (_tmpGstNumber == null) {
          statement.bindNull(5)
        } else {
          statement.bindString(5, _tmpGstNumber)
        }
        val _tmpCategory: String? = entity.category
        if (_tmpCategory == null) {
          statement.bindNull(6)
        } else {
          statement.bindString(6, _tmpCategory)
        }
        val _tmpPhone: String? = entity.phone
        if (_tmpPhone == null) {
          statement.bindNull(7)
        } else {
          statement.bindString(7, _tmpPhone)
        }
        val _tmpEmail: String? = entity.email
        if (_tmpEmail == null) {
          statement.bindNull(8)
        } else {
          statement.bindString(8, _tmpEmail)
        }
        val _tmpAddress: String? = entity.address
        if (_tmpAddress == null) {
          statement.bindNull(9)
        } else {
          statement.bindString(9, _tmpAddress)
        }
        statement.bindLong(10, entity.createdAt)
      }
    }
    this.__updateAdapterOfBusinessEntity = object :
        EntityDeletionOrUpdateAdapter<BusinessEntity>(__db) {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `businesses` SET `id` = ?,`name` = ?,`role` = ?,`logoUri` = ?,`gstNumber` = ?,`category` = ?,`phone` = ?,`email` = ?,`address` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SupportSQLiteStatement, entity: BusinessEntity) {
        statement.bindLong(1, entity.id)
        statement.bindString(2, entity.name)
        statement.bindString(3, entity.role)
        val _tmpLogoUri: String? = entity.logoUri
        if (_tmpLogoUri == null) {
          statement.bindNull(4)
        } else {
          statement.bindString(4, _tmpLogoUri)
        }
        val _tmpGstNumber: String? = entity.gstNumber
        if (_tmpGstNumber == null) {
          statement.bindNull(5)
        } else {
          statement.bindString(5, _tmpGstNumber)
        }
        val _tmpCategory: String? = entity.category
        if (_tmpCategory == null) {
          statement.bindNull(6)
        } else {
          statement.bindString(6, _tmpCategory)
        }
        val _tmpPhone: String? = entity.phone
        if (_tmpPhone == null) {
          statement.bindNull(7)
        } else {
          statement.bindString(7, _tmpPhone)
        }
        val _tmpEmail: String? = entity.email
        if (_tmpEmail == null) {
          statement.bindNull(8)
        } else {
          statement.bindString(8, _tmpEmail)
        }
        val _tmpAddress: String? = entity.address
        if (_tmpAddress == null) {
          statement.bindNull(9)
        } else {
          statement.bindString(9, _tmpAddress)
        }
        statement.bindLong(10, entity.createdAt)
        statement.bindLong(11, entity.id)
      }
    }
    this.__preparedStmtOfUpdateName = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String = "UPDATE businesses SET name = ? WHERE id = ?"
        return _query
      }
    }
    this.__preparedStmtOfDelete = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String = "DELETE FROM businesses WHERE id = ?"
        return _query
      }
    }
    this.__preparedStmtOfDeleteBusinessCascade = object : SharedSQLiteStatement(__db) {
      public override fun createQuery(): String {
        val _query: String = "DELETE FROM businesses WHERE id = ?"
        return _query
      }
    }
  }

  public override suspend fun insert(business: BusinessEntity): Long = CoroutinesRoom.execute(__db,
      true, object : Callable<Long> {
    public override fun call(): Long {
      __db.beginTransaction()
      try {
        val _result: Long = __insertionAdapterOfBusinessEntity.insertAndReturnId(business)
        __db.setTransactionSuccessful()
        return _result
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun update(business: BusinessEntity): Int = CoroutinesRoom.execute(__db,
      true, object : Callable<Int> {
    public override fun call(): Int {
      var _total: Int = 0
      __db.beginTransaction()
      try {
        _total += __updateAdapterOfBusinessEntity.handle(business)
        __db.setTransactionSuccessful()
        return _total
      } finally {
        __db.endTransaction()
      }
    }
  })

  public override suspend fun updateName(businessId: Long, newName: String): Int =
      CoroutinesRoom.execute(__db, true, object : Callable<Int> {
    public override fun call(): Int {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfUpdateName.acquire()
      var _argIndex: Int = 1
      _stmt.bindString(_argIndex, newName)
      _argIndex = 2
      _stmt.bindLong(_argIndex, businessId)
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
        __preparedStmtOfUpdateName.release(_stmt)
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

  public override suspend fun deleteBusinessCascade(businessId: Long): Int =
      CoroutinesRoom.execute(__db, true, object : Callable<Int> {
    public override fun call(): Int {
      val _stmt: SupportSQLiteStatement = __preparedStmtOfDeleteBusinessCascade.acquire()
      var _argIndex: Int = 1
      _stmt.bindLong(_argIndex, businessId)
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
        __preparedStmtOfDeleteBusinessCascade.release(_stmt)
      }
    }
  })

  public override fun getAllBusinesses(): Flow<List<BusinessEntity>> {
    val _sql: String = "SELECT * FROM businesses ORDER BY createdAt DESC"
    val _statement: RoomSQLiteQuery = acquire(_sql, 0)
    return CoroutinesRoom.createFlow(__db, false, arrayOf("businesses"), object :
        Callable<List<BusinessEntity>> {
      public override fun call(): List<BusinessEntity> {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfName: Int = getColumnIndexOrThrow(_cursor, "name")
          val _cursorIndexOfRole: Int = getColumnIndexOrThrow(_cursor, "role")
          val _cursorIndexOfLogoUri: Int = getColumnIndexOrThrow(_cursor, "logoUri")
          val _cursorIndexOfGstNumber: Int = getColumnIndexOrThrow(_cursor, "gstNumber")
          val _cursorIndexOfCategory: Int = getColumnIndexOrThrow(_cursor, "category")
          val _cursorIndexOfPhone: Int = getColumnIndexOrThrow(_cursor, "phone")
          val _cursorIndexOfEmail: Int = getColumnIndexOrThrow(_cursor, "email")
          val _cursorIndexOfAddress: Int = getColumnIndexOrThrow(_cursor, "address")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _result: MutableList<BusinessEntity> = ArrayList<BusinessEntity>(_cursor.getCount())
          while (_cursor.moveToNext()) {
            val _item: BusinessEntity
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpName: String
            _tmpName = _cursor.getString(_cursorIndexOfName)
            val _tmpRole: String
            _tmpRole = _cursor.getString(_cursorIndexOfRole)
            val _tmpLogoUri: String?
            if (_cursor.isNull(_cursorIndexOfLogoUri)) {
              _tmpLogoUri = null
            } else {
              _tmpLogoUri = _cursor.getString(_cursorIndexOfLogoUri)
            }
            val _tmpGstNumber: String?
            if (_cursor.isNull(_cursorIndexOfGstNumber)) {
              _tmpGstNumber = null
            } else {
              _tmpGstNumber = _cursor.getString(_cursorIndexOfGstNumber)
            }
            val _tmpCategory: String?
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory)
            }
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
            val _tmpAddress: String?
            if (_cursor.isNull(_cursorIndexOfAddress)) {
              _tmpAddress = null
            } else {
              _tmpAddress = _cursor.getString(_cursorIndexOfAddress)
            }
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            _item =
                BusinessEntity(_tmpId,_tmpName,_tmpRole,_tmpLogoUri,_tmpGstNumber,_tmpCategory,_tmpPhone,_tmpEmail,_tmpAddress,_tmpCreatedAt)
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

  public override suspend fun getBusinessById(id: Long): BusinessEntity? {
    val _sql: String = "SELECT * FROM businesses WHERE id = ?"
    val _statement: RoomSQLiteQuery = acquire(_sql, 1)
    var _argIndex: Int = 1
    _statement.bindLong(_argIndex, id)
    val _cancellationSignal: CancellationSignal? = createCancellationSignal()
    return execute(__db, false, _cancellationSignal, object : Callable<BusinessEntity?> {
      public override fun call(): BusinessEntity? {
        val _cursor: Cursor = query(__db, _statement, false, null)
        try {
          val _cursorIndexOfId: Int = getColumnIndexOrThrow(_cursor, "id")
          val _cursorIndexOfName: Int = getColumnIndexOrThrow(_cursor, "name")
          val _cursorIndexOfRole: Int = getColumnIndexOrThrow(_cursor, "role")
          val _cursorIndexOfLogoUri: Int = getColumnIndexOrThrow(_cursor, "logoUri")
          val _cursorIndexOfGstNumber: Int = getColumnIndexOrThrow(_cursor, "gstNumber")
          val _cursorIndexOfCategory: Int = getColumnIndexOrThrow(_cursor, "category")
          val _cursorIndexOfPhone: Int = getColumnIndexOrThrow(_cursor, "phone")
          val _cursorIndexOfEmail: Int = getColumnIndexOrThrow(_cursor, "email")
          val _cursorIndexOfAddress: Int = getColumnIndexOrThrow(_cursor, "address")
          val _cursorIndexOfCreatedAt: Int = getColumnIndexOrThrow(_cursor, "createdAt")
          val _result: BusinessEntity?
          if (_cursor.moveToFirst()) {
            val _tmpId: Long
            _tmpId = _cursor.getLong(_cursorIndexOfId)
            val _tmpName: String
            _tmpName = _cursor.getString(_cursorIndexOfName)
            val _tmpRole: String
            _tmpRole = _cursor.getString(_cursorIndexOfRole)
            val _tmpLogoUri: String?
            if (_cursor.isNull(_cursorIndexOfLogoUri)) {
              _tmpLogoUri = null
            } else {
              _tmpLogoUri = _cursor.getString(_cursorIndexOfLogoUri)
            }
            val _tmpGstNumber: String?
            if (_cursor.isNull(_cursorIndexOfGstNumber)) {
              _tmpGstNumber = null
            } else {
              _tmpGstNumber = _cursor.getString(_cursorIndexOfGstNumber)
            }
            val _tmpCategory: String?
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory)
            }
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
            val _tmpAddress: String?
            if (_cursor.isNull(_cursorIndexOfAddress)) {
              _tmpAddress = null
            } else {
              _tmpAddress = _cursor.getString(_cursorIndexOfAddress)
            }
            val _tmpCreatedAt: Long
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt)
            _result =
                BusinessEntity(_tmpId,_tmpName,_tmpRole,_tmpLogoUri,_tmpGstNumber,_tmpCategory,_tmpPhone,_tmpEmail,_tmpAddress,_tmpCreatedAt)
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
