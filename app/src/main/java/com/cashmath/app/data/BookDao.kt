package com.cashmath.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity): Int

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBook(id: Long): Int

    @Query("SELECT * FROM books WHERE businessId = :businessId ORDER BY updatedAt DESC")
    fun getBooksByBusiness(businessId: Long): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE businessId = :businessId AND id IN (:allowedBookIds) ORDER BY updatedAt DESC")
    fun getBooksByBusinessAndAllowedIds(businessId: Long, allowedBookIds: List<Long>): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE businessId = :bizId AND id IN (:bookIds) ORDER BY id DESC")
    fun getBooksByIds(bizId: Long, bookIds: List<Long>): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Long): BookEntity?

    @Query("UPDATE books SET balance = :balance, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateBalance(id: Long, balance: Double, updatedAt: Long): Int

    @Query("UPDATE books SET businessId = :targetBusinessId, updatedAt = :updatedAt WHERE id = :bookId")
    suspend fun moveBook(bookId: Long, targetBusinessId: Long, updatedAt: Long = System.currentTimeMillis()): Int

    @Query("UPDATE books SET balance = (SELECT COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END), 0.0) FROM transactions WHERE bookId = :bookId AND isDeleted = 0), updatedAt = (strftime('%s','now') * 1000) WHERE id = :bookId")
    suspend fun recalculateBookBalance(bookId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: BookMemberEntity): Long

    @Query("SELECT * FROM book_members WHERE bookId = :bookId")
    fun getMembersForBook(bookId: Long): Flow<List<BookMemberEntity>>

    @Query("SELECT role FROM book_members WHERE bookId = :bookId AND userIdentifier = :userIdentifier")
    suspend fun getUserRole(bookId: Long, userIdentifier: String): String?

    @Query("DELETE FROM book_members WHERE bookId = :bookId AND userIdentifier = :userIdentifier")
    suspend fun removeMember(bookId: Long, userIdentifier: String): Int
}
