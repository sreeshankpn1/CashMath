package com.cashmath.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cashmath.app.data.model.BusinessMemberEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class, 
        BusinessEntity::class, 
        BookEntity::class, 
        BookMemberEntity::class,
        BusinessMemberEntity::class,
        PaymentModeEntity::class,
        DuePartyEntity::class,
        EmiLoanEntity::class
    ], 
    version = 15, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun businessDao(): BusinessDao
    abstract fun bookDao(): BookDao
    abstract fun teamMemberDao(): TeamMemberDao
    abstract fun paymentModeDao(): PaymentModeDao
    abstract fun duePartyDao(): DuePartyDao
    abstract fun emiLoanDao(): EmiLoanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cashmath_database_v3.db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getDatabase(context)
                            if (database.businessDao().getAllBusinesses().first().isEmpty()) {
                                val bizId = database.businessDao().insert(
                                    BusinessEntity(name = "My Business", role = "Primary Admin")
                                )
                                val bookId = database.bookDao().insertBook(
                                    BookEntity(businessId = bizId, name = "Daily Cash")
                                )
                                database.bookDao().insertMember(
                                    BookMemberEntity(
                                        bookId = bookId,
                                        userIdentifier = "Self",
                                        displayName = "You",
                                        role = "ADMIN"
                                    )
                                )
                                database.paymentModeDao().insert(PaymentModeEntity(bookId = bookId, name = "Cash", isDefault = true, displayOrder = 0))
                                database.paymentModeDao().insert(PaymentModeEntity(bookId = bookId, name = "Online", displayOrder = 1))
                                database.paymentModeDao().insert(PaymentModeEntity(bookId = bookId, name = "UPI", displayOrder = 2))
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
