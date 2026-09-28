package com.cashmath.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.data.BookEntity
import com.cashmath.app.data.BookMemberEntity
import com.cashmath.app.data.TransactionEntity
import com.cashmath.app.data.TransactionType
import com.cashmath.app.data.model.DateRangeFilter
import com.cashmath.app.data.model.EntryTypeFilter
import com.cashmath.app.data.model.SortDirection
import com.cashmath.app.parser.DateTimeUtils
import com.cashmath.app.ui.components.PremiumBalanceCard
import com.cashmath.app.ui.components.PremiumPermissionCard
import com.cashmath.app.ui.screens.components.VoiceEntryBottomSheet
import com.cashmath.app.ui.theme.LocalThemeIsDark
import com.cashmath.app.ui.theme.RoyalIndigo
import com.cashmath.app.ui.theme.SlateGray
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BookLedgerScreen(
    book: BookEntity?,
    transactions: List<TransactionEntity>,
    netBalance: Double,
    totalIn: Double,
    totalOut: Double,
    myRole: String,
    members: List<BookMemberEntity>,
    hasPermission: Boolean,
    onBack: () -> Unit,
    onRequestPermission: () -> Unit,
    onSaveTransaction: (TransactionEntity) -> Unit,
    @Suppress("UNUSED_PARAMETER") onUpdateTransaction: (TransactionEntity) -> Unit = {},
    onDeleteTransaction: (Long) -> Unit,
    onShowReports: () -> Unit,
    onAddMember: (userIdentifier: String, displayName: String, role: String) -> Unit,
    onOpenTransactionEntry: (bookId: Long, type: String, entryId: Long?) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    // Filters state
    var selectedDateFilter by remember { mutableStateOf(DateRangeFilter.ALL) }
    var selectedEntryTypeFilter by remember { mutableStateOf(EntryTypeFilter.ALL) }
    var sortDirection by remember { mutableStateOf(SortDirection.NEWEST_FIRST) }

    var showVoiceSheet by remember { mutableStateOf(false) }
    var showDateFilterSheet by remember { mutableStateOf(false) }
    var showEntryTypeSheet by remember { mutableStateOf(false) }
    var showManageMembersSheet by remember { mutableStateOf(false) }

    var selectedTransactionForDetail by remember { mutableStateOf<TransactionEntity?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark = LocalThemeIsDark.current

    val filteredTransactions = remember(
        transactions, searchQuery, selectedDateFilter, selectedEntryTypeFilter, sortDirection
    ) {
        transactions
            .filter { item ->
                if (searchQuery.isBlank()) true
                else (item.partyName ?: "").contains(searchQuery, ignoreCase = true) ||
                        (item.remark ?: "").contains(searchQuery, ignoreCase = true) ||
                        item.amount.toString().contains(searchQuery)
            }
            .filter { item ->
                when (selectedEntryTypeFilter) {
                    EntryTypeFilter.ALL -> true
                    EntryTypeFilter.CASH_IN_ONLY -> item.type == TransactionType.INCOME
                    EntryTypeFilter.CASH_OUT_ONLY -> item.type == TransactionType.EXPENSE
                }
            }
            .filter { item ->
                val itemCal = Calendar.getInstance().apply { timeInMillis = item.transactionTimestamp }
                val nowCal = Calendar.getInstance()
                when (selectedDateFilter) {
                    DateRangeFilter.ALL -> true
                    DateRangeFilter.TODAY -> {
                        itemCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR) &&
                                itemCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
                    }
                    DateRangeFilter.THIS_WEEK -> {
                        itemCal.get(Calendar.WEEK_OF_YEAR) == nowCal.get(Calendar.WEEK_OF_YEAR) &&
                                itemCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
                    }
                    DateRangeFilter.THIS_MONTH -> {
                        itemCal.get(Calendar.MONTH) == nowCal.get(Calendar.MONTH) &&
                                itemCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
                    }
                    DateRangeFilter.CUSTOM -> true
                }
            }
            .let { list ->
                if (sortDirection == SortDirection.NEWEST_FIRST) {
                    list.sortedByDescending { it.transactionTimestamp }
                } else {
                    list.sortedBy { it.transactionTimestamp }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = book?.name ?: "Cashbook",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${filteredTransactions.size} transactions • $myRole",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onShowReports) {
                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Reports", tint = RoyalIndigo)
                    }
                    IconButton(onClick = { showManageMembersSheet = true }) {
                        Icon(Icons.Default.GroupAdd, contentDescription = "Team", tint = RoyalIndigo)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (!hasPermission) {
                    PremiumPermissionCard(
                        onRequestPermission = onRequestPermission
                    )
                }

                PremiumBalanceCard(
                    netBalance = netBalance,
                    income = totalIn,
                    expense = totalOut
                )

                SearchBar(
                    searchQuery = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                FilterChipsRow(
                    selectedDate = selectedDateFilter,
                    selectedType = selectedEntryTypeFilter,
                    onOpenDateFilter = { showDateFilterSheet = true },
                    onOpenTypeFilter = { showEntryTypeSheet = true },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Transactions List
                if (filteredTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = SlateGray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isBlank()) "No entries in this cashbook yet." else "No matching transactions found.",
                                color = SlateGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredTransactions, key = { it.id }) { tx ->
                            TransactionTile(
                                transaction = tx,
                                runningBalance = 0.0,
                                onClick = { selectedTransactionForDetail = tx }
                            )
                        }
                    }
                }
            }

            // Bottom Floating / Pinned Cash In & Cash Out Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                val isViewer = myRole.equals("VIEWER", ignoreCase = true)

                if (!isViewer) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // --- 1. Cash In Button (Left) ---
                        Button(
                            onClick = {
                                onOpenTransactionEntry(book?.id ?: 1L, "INCOME", null)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cash In", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }

                        // --- 2. Center CRED-Style Voice Microphone Button ---
                        Surface(
                            onClick = { showVoiceSheet = true },
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = if (isDark) Color(0xFF1E293B) else Color(0xFF0F2646),
                            border = BorderStroke(1.5.dp, Color(0xFF10B981)),
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Entry",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // --- 3. Cash Out Button (Right) ---
                        Button(
                            onClick = {
                                onOpenTransactionEntry(book?.id ?: 1L, "EXPENSE", null)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cash Out", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .height(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Read-Only Viewer Access", fontWeight = FontWeight.Bold, color = Color(0xFFD97706), fontSize = 14.sp)
                        }
                    }
                }
            }

            // Snackbar Host overlay
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
            )
        }
    }

    if (showVoiceSheet) {
        VoiceEntryBottomSheet(
            onDismiss = { showVoiceSheet = false },
            onConfirmEntry = { amount, isCashIn, partyName ->
                val txType = if (isCashIn) TransactionType.INCOME else TransactionType.EXPENSE
                onSaveTransaction(
                    TransactionEntity(
                        bookId = book?.id ?: 1L,
                        amount = amount,
                        type = txType,
                        partyName = partyName,
                        remark = "Voice Entry",
                        transactionTimestamp = System.currentTimeMillis()
                    )
                )
                Toast.makeText(context, "Voice entry added successfully! 🎙️", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showDateFilterSheet) {
        DateFilterBottomSheet(
            selectedFilter = selectedDateFilter,
            sortDirection = sortDirection,
            onDismiss = { showDateFilterSheet = false },
            onSelectFilter = { selectedDateFilter = it },
            onSelectSort = { sortDirection = it }
        )
    }

    if (showEntryTypeSheet) {
        EntryTypeFilterBottomSheet(
            selectedType = selectedEntryTypeFilter,
            onTypeSelected = { selectedEntryTypeFilter = it },
            onDismiss = { showEntryTypeSheet = false }
        )
    }

    // Transaction Details & Delete Bottom Sheet
    selectedTransactionForDetail?.let { tx: TransactionEntity ->
        TransactionDetailSheet(
            transaction = tx,
            onDismiss = { selectedTransactionForDetail = null },
            onEditTransaction = {
                val currentTx = tx
                selectedTransactionForDetail = null
                onOpenTransactionEntry(book?.id ?: 1L, currentTx.type.name, currentTx.id)
            },
            onDeleteTransaction = { txId: Long ->
                onDeleteTransaction(txId)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Entry deleted")
                }
            }
        )
    }

    // Manage Members & WhatsApp Invite Bottom Sheet
    if (showManageMembersSheet) {
        ManageMembersBottomSheet(
            currentBook = book,
            members = members,
            onDismiss = { showManageMembersSheet = false },
            onAddMember = { identifier, displayName, role ->
                onAddMember(identifier, displayName, role)
            }
        )
    }
}

@Composable
fun TransactionTile(
    transaction: TransactionEntity,
    @Suppress("UNUSED_PARAMETER") runningBalance: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCashIn = transaction.type == TransactionType.INCOME
    val amountColor = if (isCashIn) Color(0xFF10B981) else Color(0xFFEF4444)
    val formattedTime = DateTimeUtils.formatTransactionTime(transaction.transactionTimestamp)
    val mode = transaction.paymentMode.lowercase()
    val isOnline = mode.contains("online") || mode.contains("upi") || mode.contains("paytm") || mode.contains("phonepe") || mode.contains("google")

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Icon Badge (Ice-Blue for Online, Soft Mint for Cash) ---
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isOnline) Color(0xFFEEF2FF) else Color(0xFFECFDF5),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isOnline) Icons.Default.AccountBalance else Icons.Default.Payments,
                    contentDescription = null,
                    tint = if (isOnline) Color(0xFF4338CA) else Color(0xFF059669),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // --- Center: Party Name & Metadata ---
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = (transaction.partyName ?: transaction.remark ?: "").ifBlank { if (isCashIn) "Cash In" else "Cash Out" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${transaction.paymentMode} • $formattedTime",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // --- Right: Amount ---
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isCashIn) "+" else "-"} ${formatIndianCurrency(transaction.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = amountColor
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onQueryChange,
        placeholder = { Text("Search transactions...", fontSize = 13.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SlateGray, modifier = Modifier.size(18.dp)) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        singleLine = true
    )
}

@Composable
fun FilterChipsRow(
    selectedDate: DateRangeFilter,
    selectedType: EntryTypeFilter,
    onOpenDateFilter: () -> Unit,
    onOpenTypeFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(
            onClick = onOpenDateFilter,
            label = { Text("Date: ${selectedDate.name.replace("_", " ")}", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp)) }
        )
        AssistChip(
            onClick = onOpenTypeFilter,
            label = { Text("Type: ${selectedType.name.replace("_", " ")}", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(14.dp)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterBottomSheet(
    selectedFilter: DateRangeFilter,
    sortDirection: SortDirection,
    onDismiss: () -> Unit,
    onSelectFilter: (DateRangeFilter) -> Unit,
    onSelectSort: (SortDirection) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Date Range Filter", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            DateRangeFilter.entries.forEach { filter ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectFilter(filter)
                            onDismiss()
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selectedFilter == filter, onClick = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(filter.name.replace("_", " "))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Sort Order", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            listOf(
                SortDirection.NEWEST_FIRST to "Newest First",
                SortDirection.OLDEST_FIRST to "Oldest First"
            ).forEach { (dir, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectSort(dir)
                            onDismiss()
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = sortDirection == dir, onClick = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(label)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryTypeFilterBottomSheet(
    selectedType: EntryTypeFilter,
    onTypeSelected: (EntryTypeFilter) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text("Entry Type Filter", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            EntryTypeFilter.entries.forEach { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onTypeSelected(type)
                            onDismiss()
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (selectedType == type),
                        onClick = {
                            onTypeSelected(type)
                            onDismiss()
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = type.name.replace("_", " "),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}