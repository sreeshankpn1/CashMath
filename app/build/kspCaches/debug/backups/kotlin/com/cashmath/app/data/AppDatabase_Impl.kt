package com.cashmath.app.`data`

import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.room.RoomDatabase
import androidx.room.RoomOpenHelper
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import java.lang.Class
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import javax.`annotation`.processing.Generated
import kotlin.Any
import kotlin.Boolean
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.Set

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION"])
public class AppDatabase_Impl : AppDatabase() {
  private val _transactionDao: Lazy<TransactionDao> = lazy {
    TransactionDao_Impl(this)
  }


  private val _businessDao: Lazy<BusinessDao> = lazy {
    BusinessDao_Impl(this)
  }


  private val _bookDao: Lazy<BookDao> = lazy {
    BookDao_Impl(this)
  }


  private val _teamMemberDao: Lazy<TeamMemberDao> = lazy {
    TeamMemberDao_Impl(this)
  }


  private val _paymentModeDao: Lazy<PaymentModeDao> = lazy {
    PaymentModeDao_Impl(this)
  }


  private val _duePartyDao: Lazy<DuePartyDao> = lazy {
    DuePartyDao_Impl(this)
  }


  private val _emiLoanDao: Lazy<EmiLoanDao> = lazy {
    EmiLoanDao_Impl(this)
  }


  protected override fun createOpenHelper(config: DatabaseConfiguration): SupportSQLiteOpenHelper {
    val _openCallback: SupportSQLiteOpenHelper.Callback = RoomOpenHelper(config, object :
        RoomOpenHelper.Delegate(15) {
      public override fun createAllTables(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `bookId` INTEGER NOT NULL, `amount` REAL NOT NULL, `type` TEXT NOT NULL, `partyName` TEXT, `remark` TEXT, `category` TEXT, `paymentMode` TEXT NOT NULL, `attachmentUri` TEXT, `transactionTimestamp` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `createdByName` TEXT NOT NULL, `createdByRole` TEXT NOT NULL, `isDeleted` INTEGER NOT NULL, `deletedAt` INTEGER, `deletionReason` TEXT, FOREIGN KEY(`bookId`) REFERENCES `books`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_bookId` ON `transactions` (`bookId`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `businesses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `role` TEXT NOT NULL, `logoUri` TEXT, `gstNumber` TEXT, `category` TEXT, `phone` TEXT, `email` TEXT, `address` TEXT, `createdAt` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `books` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `businessId` INTEGER NOT NULL, `name` TEXT NOT NULL, `balance` REAL NOT NULL, `updatedAt` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, FOREIGN KEY(`businessId`) REFERENCES `businesses`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_books_businessId` ON `books` (`businessId`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `book_members` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `bookId` INTEGER NOT NULL, `userIdentifier` TEXT NOT NULL, `displayName` TEXT NOT NULL, `role` TEXT NOT NULL, `joinedAt` INTEGER NOT NULL, FOREIGN KEY(`bookId`) REFERENCES `books`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_book_members_bookId` ON `book_members` (`bookId`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `business_members` (`id` TEXT NOT NULL, `businessId` TEXT NOT NULL, `name` TEXT NOT NULL, `phone` TEXT, `email` TEXT, `role` TEXT NOT NULL, `scope` TEXT NOT NULL, `assignedBookIds` TEXT NOT NULL, `canAccessDuesEmi` INTEGER NOT NULL, `inviteCode` TEXT NOT NULL, `isInviteAccepted` INTEGER NOT NULL, `isConnected` INTEGER NOT NULL, `status` TEXT NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `payment_modes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `bookId` INTEGER NOT NULL, `name` TEXT NOT NULL, `isDefault` INTEGER NOT NULL, `displayOrder` INTEGER NOT NULL, FOREIGN KEY(`bookId`) REFERENCES `books`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_modes_bookId` ON `payment_modes` (`bookId`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `due_parties` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `businessId` INTEGER NOT NULL, `name` TEXT NOT NULL, `phone` TEXT, `toReceive` REAL NOT NULL, `toPay` REAL NOT NULL, `updatedAt` INTEGER NOT NULL, FOREIGN KEY(`businessId`) REFERENCES `businesses`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_due_parties_businessId` ON `due_parties` (`businessId`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `emi_loans` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `businessId` INTEGER NOT NULL, `loanName` TEXT NOT NULL, `lenderBank` TEXT NOT NULL, `monthlyEmi` REAL NOT NULL, `dueDayOfMonth` INTEGER NOT NULL, `totalTenureMonths` INTEGER NOT NULL, `paidMonths` INTEGER NOT NULL, `startDate` INTEGER NOT NULL, FOREIGN KEY(`businessId`) REFERENCES `businesses`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_emi_loans_businessId` ON `emi_loans` (`businessId`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '9d8626e9283542d29bedd057ef9e66c0')")
      }

      public override fun dropAllTables(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `transactions`")
        db.execSQL("DROP TABLE IF EXISTS `businesses`")
        db.execSQL("DROP TABLE IF EXISTS `books`")
        db.execSQL("DROP TABLE IF EXISTS `book_members`")
        db.execSQL("DROP TABLE IF EXISTS `business_members`")
        db.execSQL("DROP TABLE IF EXISTS `payment_modes`")
        db.execSQL("DROP TABLE IF EXISTS `due_parties`")
        db.execSQL("DROP TABLE IF EXISTS `emi_loans`")
        val _callbacks: List<RoomDatabase.Callback>? = mCallbacks
        if (_callbacks != null) {
          for (_callback: RoomDatabase.Callback in _callbacks) {
            _callback.onDestructiveMigration(db)
          }
        }
      }

      public override fun onCreate(db: SupportSQLiteDatabase) {
        val _callbacks: List<RoomDatabase.Callback>? = mCallbacks
        if (_callbacks != null) {
          for (_callback: RoomDatabase.Callback in _callbacks) {
            _callback.onCreate(db)
          }
        }
      }

      public override fun onOpen(db: SupportSQLiteDatabase) {
        mDatabase = db
        db.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(db)
        val _callbacks: List<RoomDatabase.Callback>? = mCallbacks
        if (_callbacks != null) {
          for (_callback: RoomDatabase.Callback in _callbacks) {
            _callback.onOpen(db)
          }
        }
      }

      public override fun onPreMigrate(db: SupportSQLiteDatabase) {
        dropFtsSyncTriggers(db)
      }

      public override fun onPostMigrate(db: SupportSQLiteDatabase) {
      }

      public override fun onValidateSchema(db: SupportSQLiteDatabase):
          RoomOpenHelper.ValidationResult {
        val _columnsTransactions: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(17)
        _columnsTransactions.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("bookId", TableInfo.Column("bookId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("amount", TableInfo.Column("amount", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("partyName", TableInfo.Column("partyName", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("remark", TableInfo.Column("remark", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("category", TableInfo.Column("category", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("paymentMode", TableInfo.Column("paymentMode", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("attachmentUri", TableInfo.Column("attachmentUri", "TEXT", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("transactionTimestamp", TableInfo.Column("transactionTimestamp",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("createdByName", TableInfo.Column("createdByName", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("createdByRole", TableInfo.Column("createdByRole", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("isDeleted", TableInfo.Column("isDeleted", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("deletedAt", TableInfo.Column("deletedAt", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("deletionReason", TableInfo.Column("deletionReason", "TEXT", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTransactions: HashSet<TableInfo.ForeignKey> =
            HashSet<TableInfo.ForeignKey>(1)
        _foreignKeysTransactions.add(TableInfo.ForeignKey("books", "CASCADE", "NO ACTION",
            listOf("bookId"), listOf("id")))
        val _indicesTransactions: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(1)
        _indicesTransactions.add(TableInfo.Index("index_transactions_bookId", false,
            listOf("bookId"), listOf("ASC")))
        val _infoTransactions: TableInfo = TableInfo("transactions", _columnsTransactions,
            _foreignKeysTransactions, _indicesTransactions)
        val _existingTransactions: TableInfo = read(db, "transactions")
        if (!_infoTransactions.equals(_existingTransactions)) {
          return RoomOpenHelper.ValidationResult(false, """
              |transactions(com.cashmath.app.data.TransactionEntity).
              | Expected:
              |""".trimMargin() + _infoTransactions + """
              |
              | Found:
              |""".trimMargin() + _existingTransactions)
        }
        val _columnsBusinesses: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(10)
        _columnsBusinesses.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBusinesses.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBusinesses.put("role", TableInfo.Column("role", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBusinesses.put("logoUri", TableInfo.Column("logoUri", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBusinesses.put("gstNumber", TableInfo.Column("gstNumber", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBusinesses.put("category", TableInfo.Column("category", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBusinesses.put("phone", TableInfo.Column("phone", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBusinesses.put("email", TableInfo.Column("email", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBusinesses.put("address", TableInfo.Column("address", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBusinesses.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysBusinesses: HashSet<TableInfo.ForeignKey> = HashSet<TableInfo.ForeignKey>(0)
        val _indicesBusinesses: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(0)
        val _infoBusinesses: TableInfo = TableInfo("businesses", _columnsBusinesses,
            _foreignKeysBusinesses, _indicesBusinesses)
        val _existingBusinesses: TableInfo = read(db, "businesses")
        if (!_infoBusinesses.equals(_existingBusinesses)) {
          return RoomOpenHelper.ValidationResult(false, """
              |businesses(com.cashmath.app.data.BusinessEntity).
              | Expected:
              |""".trimMargin() + _infoBusinesses + """
              |
              | Found:
              |""".trimMargin() + _existingBusinesses)
        }
        val _columnsBooks: HashMap<String, TableInfo.Column> = HashMap<String, TableInfo.Column>(6)
        _columnsBooks.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBooks.put("businessId", TableInfo.Column("businessId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBooks.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBooks.put("balance", TableInfo.Column("balance", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBooks.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBooks.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysBooks: HashSet<TableInfo.ForeignKey> = HashSet<TableInfo.ForeignKey>(1)
        _foreignKeysBooks.add(TableInfo.ForeignKey("businesses", "CASCADE", "NO ACTION",
            listOf("businessId"), listOf("id")))
        val _indicesBooks: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(1)
        _indicesBooks.add(TableInfo.Index("index_books_businessId", false, listOf("businessId"),
            listOf("ASC")))
        val _infoBooks: TableInfo = TableInfo("books", _columnsBooks, _foreignKeysBooks,
            _indicesBooks)
        val _existingBooks: TableInfo = read(db, "books")
        if (!_infoBooks.equals(_existingBooks)) {
          return RoomOpenHelper.ValidationResult(false, """
              |books(com.cashmath.app.data.BookEntity).
              | Expected:
              |""".trimMargin() + _infoBooks + """
              |
              | Found:
              |""".trimMargin() + _existingBooks)
        }
        val _columnsBookMembers: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(6)
        _columnsBookMembers.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBookMembers.put("bookId", TableInfo.Column("bookId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBookMembers.put("userIdentifier", TableInfo.Column("userIdentifier", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBookMembers.put("displayName", TableInfo.Column("displayName", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBookMembers.put("role", TableInfo.Column("role", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBookMembers.put("joinedAt", TableInfo.Column("joinedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysBookMembers: HashSet<TableInfo.ForeignKey> =
            HashSet<TableInfo.ForeignKey>(1)
        _foreignKeysBookMembers.add(TableInfo.ForeignKey("books", "CASCADE", "NO ACTION",
            listOf("bookId"), listOf("id")))
        val _indicesBookMembers: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(1)
        _indicesBookMembers.add(TableInfo.Index("index_book_members_bookId", false,
            listOf("bookId"), listOf("ASC")))
        val _infoBookMembers: TableInfo = TableInfo("book_members", _columnsBookMembers,
            _foreignKeysBookMembers, _indicesBookMembers)
        val _existingBookMembers: TableInfo = read(db, "book_members")
        if (!_infoBookMembers.equals(_existingBookMembers)) {
          return RoomOpenHelper.ValidationResult(false, """
              |book_members(com.cashmath.app.data.BookMemberEntity).
              | Expected:
              |""".trimMargin() + _infoBookMembers + """
              |
              | Found:
              |""".trimMargin() + _existingBookMembers)
        }
        val _columnsBusinessMembers: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(13)
        _columnsBusinessMembers.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBusinessMembers.put("businessId", TableInfo.Column("businessId", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBusinessMembers.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBusinessMembers.put("phone", TableInfo.Column("phone", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBusinessMembers.put("email", TableInfo.Column("email", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBusinessMembers.put("role", TableInfo.Column("role", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBusinessMembers.put("scope", TableInfo.Column("scope", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBusinessMembers.put("assignedBookIds", TableInfo.Column("assignedBookIds", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBusinessMembers.put("canAccessDuesEmi", TableInfo.Column("canAccessDuesEmi",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBusinessMembers.put("inviteCode", TableInfo.Column("inviteCode", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBusinessMembers.put("isInviteAccepted", TableInfo.Column("isInviteAccepted",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBusinessMembers.put("isConnected", TableInfo.Column("isConnected", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBusinessMembers.put("status", TableInfo.Column("status", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysBusinessMembers: HashSet<TableInfo.ForeignKey> =
            HashSet<TableInfo.ForeignKey>(0)
        val _indicesBusinessMembers: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(0)
        val _infoBusinessMembers: TableInfo = TableInfo("business_members", _columnsBusinessMembers,
            _foreignKeysBusinessMembers, _indicesBusinessMembers)
        val _existingBusinessMembers: TableInfo = read(db, "business_members")
        if (!_infoBusinessMembers.equals(_existingBusinessMembers)) {
          return RoomOpenHelper.ValidationResult(false, """
              |business_members(com.cashmath.app.data.model.BusinessMemberEntity).
              | Expected:
              |""".trimMargin() + _infoBusinessMembers + """
              |
              | Found:
              |""".trimMargin() + _existingBusinessMembers)
        }
        val _columnsPaymentModes: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(5)
        _columnsPaymentModes.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPaymentModes.put("bookId", TableInfo.Column("bookId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPaymentModes.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPaymentModes.put("isDefault", TableInfo.Column("isDefault", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPaymentModes.put("displayOrder", TableInfo.Column("displayOrder", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPaymentModes: HashSet<TableInfo.ForeignKey> =
            HashSet<TableInfo.ForeignKey>(1)
        _foreignKeysPaymentModes.add(TableInfo.ForeignKey("books", "CASCADE", "NO ACTION",
            listOf("bookId"), listOf("id")))
        val _indicesPaymentModes: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(1)
        _indicesPaymentModes.add(TableInfo.Index("index_payment_modes_bookId", false,
            listOf("bookId"), listOf("ASC")))
        val _infoPaymentModes: TableInfo = TableInfo("payment_modes", _columnsPaymentModes,
            _foreignKeysPaymentModes, _indicesPaymentModes)
        val _existingPaymentModes: TableInfo = read(db, "payment_modes")
        if (!_infoPaymentModes.equals(_existingPaymentModes)) {
          return RoomOpenHelper.ValidationResult(false, """
              |payment_modes(com.cashmath.app.data.PaymentModeEntity).
              | Expected:
              |""".trimMargin() + _infoPaymentModes + """
              |
              | Found:
              |""".trimMargin() + _existingPaymentModes)
        }
        val _columnsDueParties: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(7)
        _columnsDueParties.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDueParties.put("businessId", TableInfo.Column("businessId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDueParties.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDueParties.put("phone", TableInfo.Column("phone", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDueParties.put("toReceive", TableInfo.Column("toReceive", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDueParties.put("toPay", TableInfo.Column("toPay", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDueParties.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDueParties: HashSet<TableInfo.ForeignKey> = HashSet<TableInfo.ForeignKey>(1)
        _foreignKeysDueParties.add(TableInfo.ForeignKey("businesses", "CASCADE", "NO ACTION",
            listOf("businessId"), listOf("id")))
        val _indicesDueParties: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(1)
        _indicesDueParties.add(TableInfo.Index("index_due_parties_businessId", false,
            listOf("businessId"), listOf("ASC")))
        val _infoDueParties: TableInfo = TableInfo("due_parties", _columnsDueParties,
            _foreignKeysDueParties, _indicesDueParties)
        val _existingDueParties: TableInfo = read(db, "due_parties")
        if (!_infoDueParties.equals(_existingDueParties)) {
          return RoomOpenHelper.ValidationResult(false, """
              |due_parties(com.cashmath.app.data.DuePartyEntity).
              | Expected:
              |""".trimMargin() + _infoDueParties + """
              |
              | Found:
              |""".trimMargin() + _existingDueParties)
        }
        val _columnsEmiLoans: HashMap<String, TableInfo.Column> =
            HashMap<String, TableInfo.Column>(9)
        _columnsEmiLoans.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEmiLoans.put("businessId", TableInfo.Column("businessId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEmiLoans.put("loanName", TableInfo.Column("loanName", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEmiLoans.put("lenderBank", TableInfo.Column("lenderBank", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEmiLoans.put("monthlyEmi", TableInfo.Column("monthlyEmi", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEmiLoans.put("dueDayOfMonth", TableInfo.Column("dueDayOfMonth", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEmiLoans.put("totalTenureMonths", TableInfo.Column("totalTenureMonths", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEmiLoans.put("paidMonths", TableInfo.Column("paidMonths", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsEmiLoans.put("startDate", TableInfo.Column("startDate", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysEmiLoans: HashSet<TableInfo.ForeignKey> = HashSet<TableInfo.ForeignKey>(1)
        _foreignKeysEmiLoans.add(TableInfo.ForeignKey("businesses", "CASCADE", "NO ACTION",
            listOf("businessId"), listOf("id")))
        val _indicesEmiLoans: HashSet<TableInfo.Index> = HashSet<TableInfo.Index>(1)
        _indicesEmiLoans.add(TableInfo.Index("index_emi_loans_businessId", false,
            listOf("businessId"), listOf("ASC")))
        val _infoEmiLoans: TableInfo = TableInfo("emi_loans", _columnsEmiLoans,
            _foreignKeysEmiLoans, _indicesEmiLoans)
        val _existingEmiLoans: TableInfo = read(db, "emi_loans")
        if (!_infoEmiLoans.equals(_existingEmiLoans)) {
          return RoomOpenHelper.ValidationResult(false, """
              |emi_loans(com.cashmath.app.data.EmiLoanEntity).
              | Expected:
              |""".trimMargin() + _infoEmiLoans + """
              |
              | Found:
              |""".trimMargin() + _existingEmiLoans)
        }
        return RoomOpenHelper.ValidationResult(true, null)
      }
    }, "9d8626e9283542d29bedd057ef9e66c0", "476722cabbd99941c4c46e0daf5bcf2a")
    val _sqliteConfig: SupportSQLiteOpenHelper.Configuration =
        SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build()
    val _helper: SupportSQLiteOpenHelper = config.sqliteOpenHelperFactory.create(_sqliteConfig)
    return _helper
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: HashMap<String, String> = HashMap<String, String>(0)
    val _viewTables: HashMap<String, Set<String>> = HashMap<String, Set<String>>(0)
    return InvalidationTracker(this, _shadowTablesMap, _viewTables,
        "transactions","businesses","books","book_members","business_members","payment_modes","due_parties","emi_loans")
  }

  public override fun clearAllTables() {
    super.assertNotMainThread()
    val _db: SupportSQLiteDatabase = super.openHelper.writableDatabase
    val _supportsDeferForeignKeys: Boolean = android.os.Build.VERSION.SDK_INT >=
        android.os.Build.VERSION_CODES.LOLLIPOP
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE")
      }
      super.beginTransaction()
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE")
      }
      _db.execSQL("DELETE FROM `transactions`")
      _db.execSQL("DELETE FROM `businesses`")
      _db.execSQL("DELETE FROM `books`")
      _db.execSQL("DELETE FROM `book_members`")
      _db.execSQL("DELETE FROM `business_members`")
      _db.execSQL("DELETE FROM `payment_modes`")
      _db.execSQL("DELETE FROM `due_parties`")
      _db.execSQL("DELETE FROM `emi_loans`")
      super.setTransactionSuccessful()
    } finally {
      super.endTransaction()
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE")
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close()
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM")
      }
    }
  }

  protected override fun getRequiredTypeConverters(): Map<Class<out Any>, List<Class<out Any>>> {
    val _typeConvertersMap: HashMap<Class<out Any>, List<Class<out Any>>> =
        HashMap<Class<out Any>, List<Class<out Any>>>()
    _typeConvertersMap.put(TransactionDao::class.java, TransactionDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(BusinessDao::class.java, BusinessDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(BookDao::class.java, BookDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(TeamMemberDao::class.java, TeamMemberDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(PaymentModeDao::class.java, PaymentModeDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(DuePartyDao::class.java, DuePartyDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(EmiLoanDao::class.java, EmiLoanDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecs(): Set<Class<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: HashSet<Class<out AutoMigrationSpec>> =
        HashSet<Class<out AutoMigrationSpec>>()
    return _autoMigrationSpecsSet
  }

  public override
      fun getAutoMigrations(autoMigrationSpecs: Map<Class<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = ArrayList<Migration>()
    return _autoMigrations
  }

  public override fun transactionDao(): TransactionDao = _transactionDao.value

  public override fun businessDao(): BusinessDao = _businessDao.value

  public override fun bookDao(): BookDao = _bookDao.value

  public override fun teamMemberDao(): TeamMemberDao = _teamMemberDao.value

  public override fun paymentModeDao(): PaymentModeDao = _paymentModeDao.value

  public override fun duePartyDao(): DuePartyDao = _duePartyDao.value

  public override fun emiLoanDao(): EmiLoanDao = _emiLoanDao.value
}
