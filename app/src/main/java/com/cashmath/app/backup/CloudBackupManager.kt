package com.cashmath.app.backup

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.cashmath.app.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

object CloudBackupManager {

    private val firestore get() = FirebaseFirestore.getInstance()
    private val auth get() = FirebaseAuth.getInstance()

    suspend fun backupData(context: Context): Result<String> {
        val user = auth.currentUser ?: return Result.failure(Exception("Please sign in to backup data"))
        val userId = user.uid

        return try {
            val db = AppDatabase.getDatabase(context)
            val businesses = db.businessDao().getAllBusinesses().first()
            val books = mutableListOf<BookEntity>()
            val transactions = mutableListOf<TransactionEntity>()
            val dueParties = mutableListOf<DuePartyEntity>()
            val emiLoans = mutableListOf<EmiLoanEntity>()

            for (biz in businesses) {
                val bizBooks = db.bookDao().getBooksByBusiness(biz.id).first()
                books.addAll(bizBooks)
                for (b in bizBooks) {
                    val txs = db.transactionDao().getTransactionsForBook(b.id).first()
                    transactions.addAll(txs)
                }
                val parties = db.duePartyDao().getPartiesByBusiness(biz.id).first()
                dueParties.addAll(parties)
                val loans = db.emiLoanDao().getLoansByBusiness(biz.id).first()
                emiLoans.addAll(loans)
            }

            val backupMap = hashMapOf(
                "lastBackupTime" to System.currentTimeMillis(),
                "userEmail" to (user.email ?: "Anonymous"),
                "businesses" to businesses.map { mapOf("id" to it.id, "name" to it.name, "role" to it.role, "gstNumber" to (it.gstNumber ?: ""), "category" to (it.category ?: ""), "phone" to (it.phone ?: ""), "email" to (it.email ?: ""), "address" to (it.address ?: "")) },
                "books" to books.map { mapOf("id" to it.id, "businessId" to it.businessId, "name" to it.name, "balance" to it.balance) },
                "transactions" to transactions.map { mapOf("id" to it.id, "bookId" to it.bookId, "amount" to it.amount, "type" to it.type.name, "partyName" to (it.partyName ?: ""), "paymentMode" to it.paymentMode, "remark" to (it.remark ?: ""), "timestamp" to it.transactionTimestamp) },
                "due_parties" to dueParties.map { mapOf("id" to it.id, "businessId" to it.businessId, "name" to it.name, "phone" to (it.phone ?: ""), "toReceive" to it.toReceive, "toPay" to it.toPay) },
                "emi_loans" to emiLoans.map { mapOf("id" to it.id, "businessId" to it.businessId, "loanName" to it.loanName, "lenderBank" to it.lenderBank, "monthlyEmi" to it.monthlyEmi, "dueDayOfMonth" to it.dueDayOfMonth, "totalTenureMonths" to it.totalTenureMonths, "paidMonths" to it.paidMonths) }
            )

            firestore.collection("users")
                .document(userId)
                .collection("backups")
                .document("latest")
                .set(backupMap, SetOptions.merge())
                .await()

            Result.success("Cloud Backup Successful!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreData(context: Context): Result<String> {
        val user = auth.currentUser ?: return Result.failure(Exception("Please sign in to restore data"))
        val userId = user.uid

        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .collection("backups")
                .document("latest")
                .get()
                .await()

            if (!doc.exists()) {
                return Result.failure(Exception("No cloud backup found for this account"))
            }

            val db = AppDatabase.getDatabase(context)

            @Suppress("UNCHECKED_CAST")
            val bizList = doc.get("businesses") as? List<Map<String, Any>> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val booksList = doc.get("books") as? List<Map<String, Any>> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val txList = doc.get("transactions") as? List<Map<String, Any>> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val partyList = doc.get("due_parties") as? List<Map<String, Any>> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val loanList = doc.get("emi_loans") as? List<Map<String, Any>> ?: emptyList()

            for (biz in bizList) {
                val bEntity = BusinessEntity(
                    id = (biz["id"] as? Long) ?: (biz["id"] as? Number)?.toLong() ?: 0L,
                    name = biz["name"] as? String ?: "Business",
                    role = biz["role"] as? String ?: "Primary Admin",
                    gstNumber = (biz["gstNumber"] as? String)?.ifBlank { null },
                    category = (biz["category"] as? String)?.ifBlank { null },
                    phone = (biz["phone"] as? String)?.ifBlank { null },
                    email = (biz["email"] as? String)?.ifBlank { null },
                    address = (biz["address"] as? String)?.ifBlank { null }
                )
                db.businessDao().insert(bEntity)
            }

            for (b in booksList) {
                val bookEntity = BookEntity(
                    id = (b["id"] as? Long) ?: (b["id"] as? Number)?.toLong() ?: 0L,
                    businessId = (b["businessId"] as? Long) ?: (b["businessId"] as? Number)?.toLong() ?: 1L,
                    name = b["name"] as? String ?: "Ledger",
                    balance = (b["balance"] as? Double) ?: (b["balance"] as? Number)?.toDouble() ?: 0.0
                )
                db.bookDao().insertBook(bookEntity)
            }

            for (tx in txList) {
                val typeStr = tx["type"] as? String ?: "EXPENSE"
                val txType = try { TransactionType.valueOf(typeStr) } catch (_: Exception) { TransactionType.EXPENSE }
                val txEntity = TransactionEntity(
                    id = (tx["id"] as? Long) ?: (tx["id"] as? Number)?.toLong() ?: 0L,
                    bookId = (tx["bookId"] as? Long) ?: (tx["bookId"] as? Number)?.toLong() ?: 1L,
                    amount = (tx["amount"] as? Double) ?: (tx["amount"] as? Number)?.toDouble() ?: 0.0,
                    type = txType,
                    partyName = (tx["partyName"] as? String)?.ifBlank { null },
                    paymentMode = tx["paymentMode"] as? String ?: "Cash",
                    remark = (tx["remark"] as? String)?.ifBlank { null },
                    transactionTimestamp = (tx["timestamp"] as? Long) ?: (tx["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                )
                db.transactionDao().insert(txEntity)
                db.bookDao().recalculateBookBalance(txEntity.bookId)
            }

            for (p in partyList) {
                val partyEntity = DuePartyEntity(
                    id = (p["id"] as? Long) ?: (p["id"] as? Number)?.toLong() ?: 0L,
                    businessId = (p["businessId"] as? Long) ?: (p["businessId"] as? Number)?.toLong() ?: 1L,
                    name = p["name"] as? String ?: "Party",
                    phone = (p["phone"] as? String)?.ifBlank { null },
                    toReceive = (p["toReceive"] as? Double) ?: (p["toReceive"] as? Number)?.toDouble() ?: 0.0,
                    toPay = (p["toPay"] as? Double) ?: (p["toPay"] as? Number)?.toDouble() ?: 0.0
                )
                db.duePartyDao().insert(partyEntity)
            }

            for (l in loanList) {
                val loanEntity = EmiLoanEntity(
                    id = (l["id"] as? Long) ?: (l["id"] as? Number)?.toLong() ?: 0L,
                    businessId = (l["businessId"] as? Long) ?: (l["businessId"] as? Number)?.toLong() ?: 1L,
                    loanName = l["loanName"] as? String ?: "Loan",
                    lenderBank = l["lenderBank"] as? String ?: "Bank",
                    monthlyEmi = (l["monthlyEmi"] as? Double) ?: (l["monthlyEmi"] as? Number)?.toDouble() ?: 0.0,
                    dueDayOfMonth = ((l["dueDayOfMonth"] as? Long) ?: (l["dueDayOfMonth"] as? Number)?.toLong() ?: 5L).toInt(),
                    totalTenureMonths = ((l["totalTenureMonths"] as? Long) ?: (l["totalTenureMonths"] as? Number)?.toLong() ?: 12L).toInt(),
                    paidMonths = ((l["paidMonths"] as? Long) ?: (l["paidMonths"] as? Number)?.toLong() ?: 0L).toInt()
                )
                db.emiLoanDao().insert(loanEntity)
            }

            Result.success("Restored ${bizList.size} businesses, ${booksList.size} books, and ${txList.size} entries!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
