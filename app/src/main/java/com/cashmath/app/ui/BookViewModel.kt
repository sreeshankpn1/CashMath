package com.cashmath.app.ui

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cashmath.app.data.AppDatabase
import com.cashmath.app.data.BookEntity
import com.cashmath.app.data.BookMemberEntity
import com.cashmath.app.data.BusinessEntity
import com.cashmath.app.data.DuePartyEntity
import com.cashmath.app.data.EmiLoanEntity
import com.cashmath.app.data.TransactionEntity
import com.cashmath.app.data.TransactionType
import com.cashmath.app.data.model.AccessScope
import com.cashmath.app.data.model.BusinessMemberEntity
import com.cashmath.app.data.model.DateRangeFilter
import com.cashmath.app.data.model.EntryTypeFilter
import com.cashmath.app.data.model.MemberRole
import com.cashmath.app.data.model.PartyTransactionEntity
import com.cashmath.app.service.PartyTransactionManager
import com.cashmath.app.data.model.SortDirection
import com.cashmath.app.parser.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

enum class BookSortOption(val displayName: String) {
    LAST_UPDATED("Last Updated"),
    NAME_A_Z("Name (A-Z)"),
    BALANCE_HIGH_TO_LOW("Balance: High to Low"),
    BALANCE_LOW_TO_HIGH("Balance: Low to High"),
    LAST_CREATED("Last Created")
}

data class DuesEmiUiState(
    val totalToReceive: Double = 0.0,
    val totalToPay: Double = 0.0,
    val activeMonthlyEmiTotal: Double = 0.0,
    val totalOutstandingEmi: Double = 0.0,
    val netDues: Double = 0.0
)

@OptIn(ExperimentalCoroutinesApi::class)
class BookViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()
    private val bookDao = db.bookDao()
    private val businessDao = db.businessDao()
    private val duePartyDao = db.duePartyDao()
    private val emiLoanDao = db.emiLoanDao()
    private val teamMemberDao = db.teamMemberDao()

    private val _selectedBusinessId = MutableStateFlow<Long?>(null)
    val selectedBusinessId = _selectedBusinessId.asStateFlow()

    val teamMembers = selectedBusinessId.flatMapLatest { id ->
        if (id != null) teamMemberDao.getMembersByBusiness(id.toString()) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun saveTeamMember(member: BusinessMemberEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            teamMemberDao.insertMember(member)
        }
    }

    fun updateMemberAccess(
        memberId: String,
        newRole: MemberRole,
        newScope: AccessScope,
        selectedBookIds: List<String>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val jsonList = JSONArray(selectedBookIds).toString()
            teamMemberDao.updateMemberScope(
                memberId = memberId,
                newRole = newRole.name,
                newScope = newScope.name,
                newBookIds = jsonList
            )
        }
    }

    fun deleteTeamMember(member: BusinessMemberEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            teamMemberDao.deleteMemberById(member.id)
        }
    }

    suspend fun getMemberByInviteCode(code: String): BusinessMemberEntity? {
        return withContext(Dispatchers.IO) {
            teamMemberDao.getMemberByInviteCode(code.trim().uppercase())
        }
    }

    private val prefs = application.getSharedPreferences("rupeemath_prefs", Context.MODE_PRIVATE)

    private val savedBizId: Long?
        get() {
            val id = prefs.getLong("saved_selected_biz_id", -1L)
            return if (id != -1L) id else null
        }

    private fun saveSelectedBizId(id: Long) {
        prefs.edit { putLong("saved_selected_biz_id", id) }
    }

    val businesses = businessDao.getAllBusinesses().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val selectedBusiness = combine(businesses, selectedBusinessId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _selectedBookId = MutableStateFlow<Long?>(null)
    val selectedBookId = _selectedBookId.asStateFlow()

    val bookSearchQuery = MutableStateFlow("")
    val bookSortOption = MutableStateFlow(BookSortOption.LAST_UPDATED)

    val rawBooks = selectedBusinessId.flatMapLatest { id ->
        if (id != null) bookDao.getBooksByBusiness(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val books = combine(rawBooks, bookSearchQuery, bookSortOption) { list, query, sort ->
        var result = if (query.isBlank()) list else list.filter { it.name.contains(query, ignoreCase = true) }
        result = when (sort) {
            BookSortOption.LAST_UPDATED -> result.sortedByDescending { it.updatedAt }
            BookSortOption.NAME_A_Z -> result.sortedBy { it.name.lowercase() }
            BookSortOption.BALANCE_HIGH_TO_LOW -> result.sortedByDescending { it.balance }
            BookSortOption.BALANCE_LOW_TO_HIGH -> result.sortedBy { it.balance }
            BookSortOption.LAST_CREATED -> result.sortedByDescending { it.createdAt }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val transactions = selectedBookId.flatMapLatest { id ->
        if (id != null) transactionDao.getTransactionsForBook(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val searchQuery = MutableStateFlow("")
    val selectedDateFilter = MutableStateFlow(DateRangeFilter.ALL)
    val customDateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    val selectedEntryType = MutableStateFlow(EntryTypeFilter.ALL)
    val sortDirection = MutableStateFlow(SortDirection.NEWEST_FIRST)

    val filteredTransactions = combine(
        transactions,
        searchQuery,
        selectedDateFilter,
        selectedEntryType,
        sortDirection
    ) { rawList: List<TransactionEntity>, query: String, dateFilter: DateRangeFilter, typeFilter: EntryTypeFilter, sort: SortDirection ->
        val customRange = customDateRange.value
        rawList
            .filter { item ->
                if (query.isBlank()) true
                else (item.partyName ?: "").contains(query, ignoreCase = true) ||
                        (item.remark ?: "").contains(query, ignoreCase = true) ||
                        item.amount.toString().contains(query)
            }
            .filter { item ->
                when (typeFilter) {
                    EntryTypeFilter.ALL -> true
                    EntryTypeFilter.CASH_IN_ONLY -> item.type == TransactionType.INCOME
                    EntryTypeFilter.CASH_OUT_ONLY -> item.type == TransactionType.EXPENSE
                }
            }
            .filter { item ->
                val itemCal = Calendar.getInstance().apply { timeInMillis = item.transactionTimestamp }
                val nowCal = Calendar.getInstance()
                when (dateFilter) {
                    DateRangeFilter.ALL -> true
                    DateRangeFilter.TODAY -> DateTimeUtils.isSameDay(itemCal, nowCal)
                    DateRangeFilter.THIS_WEEK -> DateTimeUtils.isSameWeek(itemCal, nowCal)
                    DateRangeFilter.THIS_MONTH -> itemCal.get(Calendar.MONTH) == nowCal.get(Calendar.MONTH) &&
                            itemCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
                    DateRangeFilter.CUSTOM -> {
                        customRange?.let { (start, end) ->
                            item.transactionTimestamp in start..end
                        } ?: true
                    }
                }
            }
            .sortedWith { a, b ->
                if (sort == SortDirection.NEWEST_FIRST) {
                    b.transactionTimestamp.compareTo(a.transactionTimestamp)
                } else {
                    a.transactionTimestamp.compareTo(b.transactionTimestamp)
                }
            }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val filteredTotalIn = filteredTransactions.map { list ->
        list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val filteredTotalOut = filteredTransactions.map { list ->
        list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val filteredNetBalance = combine(filteredTotalIn, filteredTotalOut) { inc: Double, exp: Double ->
        inc - exp
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val totalIn = selectedBookId.flatMapLatest { id ->
        if (id != null) transactionDao.getTotalIncome(id) else flowOf(0.0)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val totalOut = selectedBookId.flatMapLatest { id ->
        if (id != null) transactionDao.getTotalExpenses(id) else flowOf(0.0)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val bookBalance = combine(totalIn, totalOut) { inc: Double?, exp: Double? ->
        (inc ?: 0.0) - (exp ?: 0.0)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val dueParties = selectedBusinessId.flatMapLatest { id ->
        if (id != null) duePartyDao.getPartiesByBusiness(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val totalToReceive = selectedBusinessId.flatMapLatest { id ->
        if (id != null) duePartyDao.getTotalDuesReceivable(id) else flowOf(0.0)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val totalToPay = selectedBusinessId.flatMapLatest { id ->
        if (id != null) duePartyDao.getTotalDuesPayable(id) else flowOf(0.0)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val emiLoans = selectedBusinessId.flatMapLatest { id ->
        if (id != null) emiLoanDao.getLoansByBusiness(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activeMonthlyEmiTotal = selectedBusinessId.flatMapLatest { id ->
        if (id != null) emiLoanDao.getActiveMonthlyEmiTotal(id) else flowOf(0.0)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val totalOutstandingEmi = selectedBusinessId.flatMapLatest { id ->
        if (id != null) emiLoanDao.getTotalOutstandingEmi(id) else flowOf(0.0)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val totalMonthlyEmi = activeMonthlyEmiTotal

    val duesEmiUiState = combine(
        totalToReceive,
        totalToPay,
        activeMonthlyEmiTotal,
        totalOutstandingEmi
    ) { toReceive, toPay, monthlyEmi, outstandingEmi ->
        DuesEmiUiState(
            totalToReceive = toReceive,
            totalToPay = toPay,
            activeMonthlyEmiTotal = monthlyEmi,
            totalOutstandingEmi = outstandingEmi,
            netDues = toReceive - toPay
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DuesEmiUiState())

    init {
        viewModelScope.launch {
            businesses.collect { list ->
                if (list.isNotEmpty()) {
                    val current = _selectedBusinessId.value
                    val saved = savedBizId
                    val targetId = if (current != null && list.any { it.id == current }) {
                        current
                    } else if (saved != null && list.any { it.id == saved }) {
                        saved
                    } else {
                        list.first().id
                    }
                    if (_selectedBusinessId.value != targetId) {
                        selectBusiness(targetId)
                    }
                }
            }
        }
    }

    fun selectBusiness(id: Long) {
        _selectedBusinessId.value = id
        _selectedBookId.value = null
        saveSelectedBizId(id)
    }

    fun selectBook(id: Long) {
        _selectedBookId.value = id
    }

    fun addBusiness(name: String) {
        viewModelScope.launch {
            val newBizId = businessDao.insert(BusinessEntity(name = name, role = "Primary Admin"))
            selectBusiness(newBizId)
        }
    }

    fun renameBusiness(businessId: Long, newName: String) {
        viewModelScope.launch {
            val biz = businessDao.getBusinessById(businessId)
            if (biz != null) {
                businessDao.update(biz.copy(name = newName))
            }
        }
    }

    fun deleteBusinessCascade(business: BusinessEntity) {
        deleteBusinessCascade(business.id)
    }

    fun deleteBusinessCascade(businessId: Long) {
        viewModelScope.launch {
            businessDao.delete(businessId)
            if (_selectedBusinessId.value == businessId) {
                _selectedBusinessId.value = null
                _selectedBookId.value = null
            }
        }
    }

    fun updateBusinessProfile(business: BusinessEntity) {
        viewModelScope.launch {
            businessDao.update(business)
        }
    }

    fun addBook(name: String) {
        val bizId = selectedBusinessId.value ?: return
        viewModelScope.launch {
            bookDao.insertBook(BookEntity(businessId = bizId, name = name))
        }
    }

    fun renameBook(book: BookEntity, newName: String) {
        viewModelScope.launch {
            bookDao.updateBook(book.copy(name = newName, updatedAt = System.currentTimeMillis()))
        }
    }

    fun duplicateBook(book: BookEntity) {
        viewModelScope.launch {
            val newBookId = bookDao.insertBook(
                BookEntity(
                    businessId = book.businessId,
                    name = "${book.name} (Copy)",
                    balance = 0.0
                )
            )
            bookDao.insertMember(
                BookMemberEntity(
                    bookId = newBookId,
                    userIdentifier = "Self",
                    displayName = "You",
                    role = "ADMIN"
                )
            )
        }
    }

    fun moveBook(bookId: Long, targetBusinessId: Long) {
        viewModelScope.launch {
            bookDao.moveBook(bookId, targetBusinessId)
        }
    }

    fun addBookMember(bookId: Long, name: String, phoneOrEmail: String, role: String) {
        viewModelScope.launch(Dispatchers.IO) {
            bookDao.insertMember(
                BookMemberEntity(
                    bookId = bookId,
                    userIdentifier = phoneOrEmail.trim(),
                    displayName = name.trim(),
                    role = role
                )
            )
        }
    }

    fun getMembersForBook(bookId: Long): Flow<List<BookMemberEntity>> {
        return bookDao.getMembersForBook(bookId)
    }

    fun removeBookMember(bookId: Long, userIdentifier: String) {
        viewModelScope.launch(Dispatchers.IO) {
            bookDao.removeMember(bookId, userIdentifier)
        }
    }

    fun deleteBook(id: Long) {
        viewModelScope.launch {
            bookDao.deleteBook(id)
        }
    }

    fun deleteBook(book: BookEntity) {
        deleteBook(book.id)
    }

    fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionDao.insert(transaction)
            bookDao.recalculateBookBalance(transaction.bookId)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionDao.updateTransaction(transaction)
            bookDao.recalculateBookBalance(transaction.bookId)
        }
    }

    fun deleteTransaction(id: Long) {
        val bId = selectedBookId.value
        viewModelScope.launch {
            transactionDao.deleteTransaction(id)
            if (bId != null) {
                bookDao.recalculateBookBalance(bId)
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        deleteTransaction(transaction.id)
    }

    fun addDueParty(name: String, phone: String?, toReceive: Double, toPay: Double) {
        val bizId = selectedBusinessId.value ?: return
        viewModelScope.launch {
            duePartyDao.insert(
                DuePartyEntity(
                    businessId = bizId,
                    name = name,
                    phone = phone ?: "",
                    toReceive = toReceive,
                    toPay = toPay
                )
            )
        }
    }

    fun deleteDueParty(id: Long) {
        viewModelScope.launch {
            duePartyDao.delete(id)
        }
    }

    fun updateDueParty(party: DuePartyEntity) {
        viewModelScope.launch {
            duePartyDao.update(party)
        }
    }

    fun addPartyTransaction(
        context: Context,
        party: DuePartyEntity,
        amount: Double,
        isCredit: Boolean,
        remarks: String = "",
        paymentMode: String = "Cash"
    ) {
        viewModelScope.launch {
            val tx = PartyTransactionEntity(
                partyId = party.id.toString(),
                amount = amount,
                isCredit = isCredit,
                date = System.currentTimeMillis(),
                remarks = remarks.ifBlank { if (isCredit) "Payment Received (Got)" else "Udhaar Given (Gave)" },
                paymentMode = paymentMode
            )
            PartyTransactionManager.addTransaction(context, party.id, tx)

            var currentReceive = party.toReceive
            var currentPay = party.toPay

            if (isCredit) {
                if (currentReceive >= amount) {
                    currentReceive -= amount
                } else {
                    val diff = amount - currentReceive
                    currentReceive = 0.0
                    currentPay += diff
                }
            } else {
                if (currentPay >= amount) {
                    currentPay -= amount
                } else {
                    val diff = amount - currentPay
                    currentPay = 0.0
                    currentReceive += diff
                }
            }

            val updated = party.copy(
                toReceive = currentReceive,
                toPay = currentPay,
                updatedAt = System.currentTimeMillis()
            )
            duePartyDao.update(updated)
        }
    }

    fun settlePartyBalance(
        context: Context,
        party: DuePartyEntity,
        amount: Double,
        paymentMode: String = "Cash",
        isFullSettlement: Boolean = true,
        recordInCashbook: Boolean = false
    ) {
        viewModelScope.launch {
            val netBalance = party.toReceive - party.toPay
            val isCredit = netBalance > 0

            val tx = PartyTransactionEntity(
                partyId = party.id.toString(),
                amount = amount,
                isCredit = isCredit,
                date = System.currentTimeMillis(),
                remarks = "Balance Settlement",
                paymentMode = paymentMode
            )
            PartyTransactionManager.addTransaction(context, party.id, tx)

            val updated = if (isFullSettlement) {
                party.copy(toReceive = 0.0, toPay = 0.0, updatedAt = System.currentTimeMillis())
            } else {
                var rec = party.toReceive
                var pay = party.toPay
                if (rec > 0) rec = maxOf(0.0, rec - amount)
                else if (pay > 0) pay = maxOf(0.0, pay - amount)
                party.copy(toReceive = rec, toPay = pay, updatedAt = System.currentTimeMillis())
            }

            duePartyDao.update(updated)

            if (recordInCashbook) {
                val bookId = selectedBookId.value ?: books.value.firstOrNull()?.id ?: 1L
                val txType = if (isCredit) TransactionType.INCOME else TransactionType.EXPENSE
                val cashTx = TransactionEntity(
                    bookId = bookId,
                    amount = amount,
                    type = txType,
                    partyName = party.name,
                    paymentMode = paymentMode,
                    remark = "Khata Settlement - ${party.name}",
                    transactionTimestamp = System.currentTimeMillis(),
                    createdByName = "Self"
                )
                transactionDao.insert(cashTx)
                bookDao.recalculateBookBalance(bookId)
            }
        }
    }

    fun addEmiLoan(name: String, bank: String, emiAmount: Double, dueDay: Int, totalTenureMonths: Int) {
        val bizId = selectedBusinessId.value ?: return
        viewModelScope.launch {
            emiLoanDao.insert(
                EmiLoanEntity(
                    businessId = bizId,
                    loanName = name,
                    lenderBank = bank,
                    monthlyEmi = emiAmount,
                    dueDayOfMonth = dueDay,
                    totalTenureMonths = totalTenureMonths
                )
            )
        }
    }

    fun updateEmiLoan(loan: EmiLoanEntity) {
        viewModelScope.launch {
            emiLoanDao.update(loan)
        }
    }

    fun incrementPaidEmiMonth(id: Long) {
        viewModelScope.launch {
            emiLoanDao.incrementPaidMonth(id)
        }
    }

    fun deleteEmiLoan(id: Long) {
        viewModelScope.launch {
            emiLoanDao.delete(id)
        }
    }

    fun importAndCreateBook(
        bookName: String,
        transactions: List<TransactionEntity>,
        onComplete: (Long, String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val activeBizId = selectedBusinessId.value 
                ?: db.businessDao().getAllBusinesses().first().firstOrNull()?.id 
                ?: 1L

            val cleanBookName = bookName.ifBlank { "Excel Ledger" }
            val newBook = BookEntity(
                businessId = activeBizId,
                name = cleanBookName,
                createdAt = System.currentTimeMillis()
            )
            val newBookId = db.bookDao().insertBook(newBook)

            val linkedTransactions = transactions.map { tx ->
                tx.copy(bookId = newBookId)
            }
            db.transactionDao().insertTransactions(linkedTransactions)
            db.bookDao().recalculateBookBalance(newBookId)

            selectBook(newBookId)

            withContext(Dispatchers.Main) {
                onComplete(newBookId, cleanBookName)
            }
        }
    }
}
