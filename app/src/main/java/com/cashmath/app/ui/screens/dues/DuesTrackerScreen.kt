package com.cashmath.app.ui.screens.dues

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.cashmath.app.data.BusinessEntity
import com.cashmath.app.data.DuePartyEntity
import com.cashmath.app.data.EmiLoanEntity
import com.cashmath.app.service.DuesEmiReportGenerator
import com.cashmath.app.ui.DuesEmiUiState
import com.cashmath.app.ui.components.BusinessTopBar
import com.cashmath.app.ui.screens.BusinessSelectorSheet
import com.cashmath.app.ui.screens.dues.components.EditLoanBottomSheet
import com.cashmath.app.ui.screens.dues.components.EditPartyBottomSheet
import com.cashmath.app.ui.screens.dues.components.LoanCalculatorsBottomSheet
import com.cashmath.app.ui.screens.dues.components.LoanCard
import com.cashmath.app.ui.screens.dues.components.LoanPrepaymentCalculatorSheet
import com.cashmath.app.ui.screens.dues.components.PartyCard
import com.cashmath.app.ui.screens.formatIndianCurrency
import com.cashmath.app.ui.theme.LocalThemeIsDark
import com.cashmath.app.util.ContactPickerHelper
import com.cashmath.app.utils.WhatsAppReminderHelper
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun DuesTrackerScreen(
    businessName: String = "CashMath Business",
    businesses: List<BusinessEntity> = emptyList(),
    selectedBusinessId: Long? = null,
    onBusinessSelected: (Long) -> Unit = {},
    onAddBusiness: (String) -> Unit = {},
    onRenameBusiness: (Long, String) -> Unit = { _, _ -> },
    onDeleteBusiness: (BusinessEntity) -> Unit = {},
    dueParties: List<DuePartyEntity>,
    totalToReceive: Double = 0.0,
    totalToPay: Double = 0.0,
    emiLoans: List<EmiLoanEntity> = emptyList(),
    totalMonthlyEmi: Double = 0.0,
    uiState: DuesEmiUiState = DuesEmiUiState(
        totalToReceive = totalToReceive,
        totalToPay = totalToPay,
        activeMonthlyEmiTotal = totalMonthlyEmi,
    ),
    onAddParty: (name: String, phone: String?, toReceive: Double, toPay: Double) -> Unit,
    onEditParty: (DuePartyEntity) -> Unit = {},
    onDeleteParty: (Long) -> Unit,
    onAddLoan: (loanName: String, lenderBank: String, monthlyEmi: Double, dueDay: Int, tenureMonths: Int) -> Unit,
    onEditLoan: (EmiLoanEntity) -> Unit = {},
    onIncrementPaidMonth: (Long) -> Unit,
    onDecrementPaidMonth: (Long) -> Unit = {},
    onDeleteLoan: (Long) -> Unit,
    onPartyClick: (Long) -> Unit = {},
    onShareDuesPdf: () -> Unit = {},
    onShareDuesCsv: () -> Unit = {},
    onSwitchBusinessClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // Theme-Adaptive Color Engine
    val isDark = LocalThemeIsDark.current
    val financialGreen = if (isDark) Color(0xFF34D399) else Color(0xFF047857)
    val neutralBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val neutralCardBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val screenBg = if (isDark) Color(0xFF0A0E17) else Color(0xFFF8FAFC)
    val titleText = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val mutedText = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    var partyToEdit by remember { mutableStateOf<DuePartyEntity?>(null) }
    var loanToEdit by remember { mutableStateOf<EmiLoanEntity?>(null) }

    fun sendWhatsAppReminder(party: DuePartyEntity) {
        val balance = party.toReceive - party.toPay
        WhatsAppReminderHelper.sendPaymentReminder(
            context = context,
            partyName = party.name,
            partyPhone = party.phone ?: "",
            amountDue = balance,
            businessName = businessName,
            isReceivable = party.toReceive >= party.toPay
        )
    }

    val pagerState = rememberPagerState(pageCount = { 2 })

    var showBusinessSheet by remember { mutableStateOf(false) }
    var showAddPartyDialog by remember { mutableStateOf(false) }
    var showAddLoanDialog by remember { mutableStateOf(false) }
    var showMasterReportSheet by remember { mutableStateOf(false) }
    var showTabReportSheet by remember { mutableStateOf(false) }
    var showLoanCalculatorsSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BusinessTopBar(
                activeBusinessName = businessName,
                onSwitchBusinessClick = { showBusinessSheet = true },
                actions = {
                    val isLoansTab = pagerState.currentPage == 1
                    IconButton(onClick = { showLoanCalculatorsSheet = true }) {
                        Icon(Icons.Default.Calculate, contentDescription = "Loan Calculators", tint = financialGreen)
                    }
                    IconButton(onClick = { showTabReportSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = if (isLoansTab) "Export Loan Schedule" else "Export Dues Statement",
                            tint = titleText
                        )
                    }
                }
            )
        },
        containerColor = screenBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Net Dues Summary Hero Tile (Compact 115dp Apple Wallet Style)
            NetDuesSummaryCard(
                netDues = uiState.netDues,
                monthlyEmi = uiState.activeMonthlyEmiTotal,
                totalOutstanding = uiState.totalOutstandingEmi
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Sub-Header Action Row: Interactive Tab Pills & Dynamic Add Button
            val isLoansTab = pagerState.currentPage == 1

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab Switcher
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // TAB 0: PARTIES
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            scope.launch { pagerState.animateScrollToPage(0) }
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (pagerState.currentPage == 0) financialGreen else neutralCardBg,
                        contentColor = if (pagerState.currentPage == 0) Color.White else mutedText,
                        border = BorderStroke(1.dp, if (pagerState.currentPage == 0) financialGreen else neutralBorder)
                    ) {
                        Text(
                            text = "Parties (${dueParties.size})",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // TAB 1: LOANS
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            scope.launch { pagerState.animateScrollToPage(1) }
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (pagerState.currentPage == 1) financialGreen else neutralCardBg,
                        contentColor = if (pagerState.currentPage == 1) Color.White else mutedText,
                        border = BorderStroke(1.dp, if (pagerState.currentPage == 1) financialGreen else neutralBorder)
                    ) {
                        Text(
                            text = "Loans (${emiLoans.size})",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // DYNAMIC ADD BUTTON (Switches between Party and Loan)
                Button(
                    onClick = {
                        if (isLoansTab) showAddLoanDialog = true else showAddPartyDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = financialGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isLoansTab) "Loan" else "Party", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
            }

            var partyFilter by remember { mutableStateOf("ALL") } // "ALL", "PENDING", "SETTLED"
            val pendingParties = remember(dueParties) { dueParties.filter { abs(it.toReceive - it.toPay) > 0.01 } }
            val settledParties = remember(dueParties) { dueParties.filter { abs(it.toReceive - it.toPay) <= 0.01 } }
            val displayedParties = remember(dueParties, partyFilter) {
                when (partyFilter) {
                    "PENDING" -> pendingParties
                    "SETTLED" -> settledParties
                    else -> dueParties
                }
            }

            if (pagerState.currentPage == 0 && dueParties.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        "ALL" to "All (${dueParties.size})",
                        "PENDING" to "Pending (${pendingParties.size})",
                        "SETTLED" to "Settled (${settledParties.size})"
                    ).forEach { (key, label) ->
                        val isSelected = partyFilter == key
                        Surface(
                            onClick = { partyFilter = key },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) financialGreen.copy(alpha = 0.15f) else neutralCardBg,
                            border = BorderStroke(1.dp, if (isSelected) financialGreen else neutralBorder)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) financialGreen else mutedText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // HORIZONTAL PAGER
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                when (page) {
                    0 -> DuesPartiesTabContent(
                        parties = displayedParties,
                        mutedText = mutedText,
                        onPartyClick = onPartyClick,
                        onWhatsAppClick = { sendWhatsAppReminder(it) },
                        onEditParty = { partyToEdit = it },
                        onDeleteParty = onDeleteParty
                    )
                    1 -> EmiLoansTabContent(
                        loans = emiLoans,
                        mutedText = mutedText,
                        onPayEmi = onIncrementPaidMonth,
                        onUndoEmi = onDecrementPaidMonth,
                        onEditLoan = { loanToEdit = it },
                        onDeleteLoan = onDeleteLoan,
                        onCalculateSavings = { showLoanCalculatorsSheet = true }
                    )
                }
            }
        }
    }

    // Business Selector Sheet
    if (showBusinessSheet) {
        BusinessSelectorSheet(
            businesses = businesses,
            selectedId = selectedBusinessId,
            onDismiss = { showBusinessSheet = false },
            onSelect = { onBusinessSelected(it); showBusinessSheet = false },
            onAdd = { newBizName ->
                onAddBusiness(newBizName)
                showBusinessSheet = false
            },
            onRenameBusiness = onRenameBusiness,
            onDeleteBusiness = onDeleteBusiness
        )
    }

    // Dialogs & Sheets
    if (showAddPartyDialog) {
        AddPartyDialog(
            onDismiss = { showAddPartyDialog = false },
            onConfirm = { name, phone, toRec, toPay ->
                onAddParty(name, phone, toRec, toPay)
                showAddPartyDialog = false
            }
        )
    }

    if (showAddLoanDialog) {
        AddLoanDialog(
            onDismiss = { showAddLoanDialog = false },
            onConfirm = { name, bank, emi, day, tenure ->
                onAddLoan(name, bank, emi, day, tenure)
                showAddLoanDialog = false
            }
        )
    }

    partyToEdit?.let { party ->
        EditPartyBottomSheet(
            party = party,
            onDismiss = { partyToEdit = null },
            onSave = { updated ->
                onEditParty(updated)
                partyToEdit = null
            }
        )
    }

    loanToEdit?.let { loan ->
        EditLoanBottomSheet(
            loan = loan,
            onDismiss = { loanToEdit = null },
            onSave = { updated ->
                onEditLoan(updated)
                loanToEdit = null
            }
        )
    }

    if (showLoanCalculatorsSheet) {
        LoanCalculatorsBottomSheet(
            loans = emiLoans,
            onDismiss = { showLoanCalculatorsSheet = false }
        )
    }

    if (showMasterReportSheet) {
        val cleanBizName = businessName.trim().replace(Regex("[^a-zA-Z0-9]"), "_").ifBlank { "CashMath" }
        val dateStr = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH).format(Date())
        ReportExportBottomSheet(
            reportTitle = "Consolidated Master Report",
            pdfFileProvider = {
                val file = File(context.cacheDir, "reports/${cleanBizName}_Consolidated_Report_${dateStr}.pdf")
                file.parentFile?.mkdirs()
                DuesEmiReportGenerator.generatePdf(businessName, dueParties, emiLoans, uiState, file)
            },
            csvFileProvider = {
                val file = File(context.cacheDir, "reports/${cleanBizName}_Consolidated_Ledger_${dateStr}.csv")
                file.parentFile?.mkdirs()
                DuesEmiReportGenerator.generateCsv(dueParties, emiLoans, file)
            },
            onDismiss = { showMasterReportSheet = false }
        )
    }

    if (showTabReportSheet) {
        val cleanBizName = businessName.trim().replace(Regex("[^a-zA-Z0-9]"), "_").ifBlank { "CashMath" }
        val dateStr = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH).format(Date())
        val isDuesTab = pagerState.currentPage == 0
        ReportExportBottomSheet(
            reportTitle = if (isDuesTab) "Dues & Khata Statement" else "Loan & EMI Schedule Report",
            pdfFileProvider = {
                val reportType = if (isDuesTab) "Dues_Report" else "Loan_EMI_Report"
                val file = File(context.cacheDir, "reports/${cleanBizName}_${reportType}_${dateStr}.pdf")
                file.parentFile?.mkdirs()
                if (isDuesTab) {
                    DuesEmiReportGenerator.generateDuesOnlyPdf(businessName, dueParties, uiState, file)
                } else {
                    DuesEmiReportGenerator.generateEmiOnlyPdf(businessName, emiLoans, uiState, file)
                }
            },
            csvFileProvider = {
                val reportType = if (isDuesTab) "Dues_Ledger" else "Loan_EMI_Ledger"
                val file = File(context.cacheDir, "reports/${cleanBizName}_${reportType}_${dateStr}.csv")
                file.parentFile?.mkdirs()
                if (isDuesTab) {
                    DuesEmiReportGenerator.generateDuesOnlyCsv(dueParties, file)
                } else {
                    DuesEmiReportGenerator.generateEmiOnlyCsv(emiLoans, file)
                }
            },
            onDismiss = { showTabReportSheet = false },
            onOpenConsolidatedMasterReport = {
                showMasterReportSheet = true
            }
        )
    }
}

@Composable
private fun DuesPartiesTabContent(
    parties: List<DuePartyEntity>,
    mutedText: Color,
    onPartyClick: (Long) -> Unit = {},
    onWhatsAppClick: (DuePartyEntity) -> Unit,
    onEditParty: (DuePartyEntity) -> Unit,
    onDeleteParty: (Long) -> Unit
) {
    if (parties.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No customer or supplier khata parties recorded.", color = mutedText, fontSize = 13.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(parties, key = { it.id }) { party ->
                PartyCard(
                    party = party,
                    onPartyClick = { onPartyClick(party.id) },
                    onWhatsAppClick = { onWhatsAppClick(party) },
                    onEditClick = { onEditParty(party) },
                    onDeleteClick = { onDeleteParty(party.id) }
                )
            }
        }
    }
}

@Composable
private fun EmiLoansTabContent(
    loans: List<EmiLoanEntity>,
    mutedText: Color,
    onPayEmi: (Long) -> Unit,
    onUndoEmi: (Long) -> Unit = {},
    onEditLoan: (EmiLoanEntity) -> Unit,
    onDeleteLoan: (Long) -> Unit,
    onCalculateSavings: () -> Unit
) {
    if (loans.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active loans or EMIs recorded.", color = mutedText, fontSize = 13.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(loans, key = { it.id }) { loan ->
                LoanCard(
                    loan = loan,
                    onMarkPaidClick = { onPayEmi(loan.id) },
                    onUndoLastPaymentClick = { onUndoEmi(loan.id) },
                    onEditClick = { onEditLoan(loan) },
                    onDeleteClick = { onDeleteLoan(loan.id) },
                    onCalculateSavingsClick = onCalculateSavings
                )
            }
        }
    }
}

@Composable
fun AddPartyDialog(onDismiss: () -> Unit, onConfirm: (String, String?, Double, Double) -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var toReceiveText by remember { mutableStateOf("") }
    var toPayText by remember { mutableStateOf("") }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contactUri: Uri? = result.data?.data
            contactUri?.let { uri ->
                try {
                    context.contentResolver.query(
                        uri,
                        arrayOf(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                        ),
                        null, null, null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                            val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                            if (nameIdx != -1) {
                                val pickedName = cursor.getString(nameIdx) ?: ""
                                if (name.isBlank()) name = pickedName
                            }
                            if (numIdx != -1) {
                                val rawNumber = cursor.getString(numIdx) ?: ""
                                phone = rawNumber.replace(Regex("[^0-9+]"), "")
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val isDark = LocalThemeIsDark.current
    val dialogBg = if (isDark) Color(0xFF131B2E) else Color.White
    val dialogText = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val mutedLabel = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val emeraldAccent = if (isDark) Color(0xFF10B981) else Color(0xFF059669)

    val tileColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = if (isDark) Color(0xFF0A0E17) else Color(0xFFF8FAFC),
        unfocusedContainerColor = if (isDark) Color(0xFF0A0E17) else Color(0xFFF8FAFC),
        focusedTextColor = dialogText,
        unfocusedTextColor = dialogText,
        focusedBorderColor = emeraldAccent,
        unfocusedBorderColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
        focusedLabelColor = emeraldAccent,
        unfocusedLabelColor = mutedLabel
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Add Customer / Supplier",
                fontWeight = FontWeight.Bold,
                color = dialogText
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Party Name *", fontSize = 12.sp) },
                    placeholder = { Text("e.g., Sharma Enterprises, Rahul Verma, Vendor", fontSize = 12.sp) },
                    colors = tileColors,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", fontSize = 12.sp) },
                    placeholder = { Text("e.g., 98765 43210", fontSize = 12.sp) },
                    trailingIcon = {
                        IconButton(onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                                contactPickerLauncher.launch(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }) {
                            Icon(Icons.Default.Contacts, contentDescription = "Pick Contact", tint = emeraldAccent)
                        }
                    },
                    colors = tileColors,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = toReceiveText,
                    onValueChange = { toReceiveText = it },
                    label = { Text("Amount You'll Receive (₹)", fontSize = 12.sp) },
                    colors = tileColors,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = toPayText,
                    onValueChange = { toPayText = it },
                    label = { Text("Amount You'll Pay (₹)", fontSize = 12.sp) },
                    colors = tileColors,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val toRec = toReceiveText.toDoubleOrNull() ?: 0.0
                        val toPay = toPayText.toDoubleOrNull() ?: 0.0
                        onConfirm(name.trim(), phone.ifBlank { null }, toRec, toPay)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = emeraldAccent),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("Save Party", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = mutedLabel, fontWeight = FontWeight.Medium)
            }
        }
    )
}

@Composable
fun AddLoanDialog(onDismiss: () -> Unit, onConfirm: (String, String, Double, Int, Int) -> Unit) {
    var loanName by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }
    var emiText by remember { mutableStateOf("") }
    var dueDayText by remember { mutableStateOf("") }
    var tenureText by remember { mutableStateOf("") }

    val isDark = LocalThemeIsDark.current
    val dialogBg = if (isDark) Color(0xFF131B2E) else Color.White
    val dialogText = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val mutedLabel = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val emeraldAccent = if (isDark) Color(0xFF10B981) else Color(0xFF059669)

    val tileColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = if (isDark) Color(0xFF0A0E17) else Color(0xFFF8FAFC),
        unfocusedContainerColor = if (isDark) Color(0xFF0A0E17) else Color(0xFFF8FAFC),
        focusedTextColor = dialogText,
        unfocusedTextColor = dialogText,
        focusedBorderColor = emeraldAccent,
        unfocusedBorderColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
        focusedLabelColor = emeraldAccent,
        unfocusedLabelColor = mutedLabel
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Add Loan / EMI Tracker",
                fontWeight = FontWeight.Bold,
                color = dialogText
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = loanName,
                    onValueChange = { loanName = it },
                    label = { Text("Loan Title *", fontSize = 12.sp) },
                    placeholder = { Text("e.g., Business Machinery Loan, Vehicle EMI, Commercial Loan", fontSize = 12.sp) },
                    colors = tileColors,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bank,
                    onValueChange = { bank = it },
                    label = { Text("Lender Bank / Financier *", fontSize = 12.sp) },
                    placeholder = { Text("e.g., HDFC Bank, SBI, ICICI, Bajaj Finance", fontSize = 12.sp) },
                    colors = tileColors,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = emiText,
                    onValueChange = { emiText = it },
                    label = { Text("Monthly EMI Amount (₹)", fontSize = 12.sp) },
                    placeholder = { Text("e.g., 15000", fontSize = 12.sp) },
                    colors = tileColors,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dueDayText,
                        onValueChange = { dueDayText = it },
                        label = { Text("Due Day (1-31)", fontSize = 12.sp) },
                        colors = tileColors,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = tenureText,
                        onValueChange = { tenureText = it },
                        label = { Text("Tenure (Months)", fontSize = 12.sp) },
                        colors = tileColors,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (loanName.isNotBlank() && bank.isNotBlank()) {
                        val emi = emiText.toDoubleOrNull() ?: 0.0
                        val day = dueDayText.toIntOrNull() ?: 5
                        val tenure = tenureText.toIntOrNull() ?: 24
                        onConfirm(loanName.trim(), bank.trim(), emi, day, tenure)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = emeraldAccent),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("Save Loan", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = mutedLabel, fontWeight = FontWeight.Medium)
            }
        }
    )
}

@Composable
fun NetDuesSummaryCard(
    netDues: Double,
    monthlyEmi: Double,
    totalOutstanding: Double,
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeIsDark.current
    val financialGreen = if (isDark) Color(0xFF34D399) else Color(0xFF047857)
    val greenContainer = if (isDark) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFFECFDF5)
    val financialRed = if (isDark) Color(0xFFF87171) else Color(0xFFB91C1C)
    val redContainer = if (isDark) Color(0xFF7F1D1D).copy(alpha = 0.5f) else Color(0xFFFEF2F2)
    val neutralBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val neutralCardBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = neutralCardBg
        ),
        border = BorderStroke(
            width = 1.dp,
            color = neutralBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Section: Label and Large Net Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NET DUES BALANCE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    letterSpacing = 1.sp
                )
                // Small indicator pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (netDues >= 0) greenContainer else redContainer
                ) {
                    Text(
                        text = if (netDues >= 0) "Receivable" else "Payable",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (netDues >= 0) financialGreen else financialRed,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(2.dp))

            // Prominent Amount
            Text(
                text = String.format(Locale.getDefault(), "₹ %,.2f", abs(netDues)),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (netDues >= 0) financialGreen else financialRed
            )

            Spacer(Modifier.height(8.dp))

            HorizontalDivider(
                thickness = 1.dp,
                color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
            )

            Spacer(Modifier.height(8.dp))

            // Bottom Tier: Compact 2-column stats (no text wrapping)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Monthly EMI
                Column {
                    Text(
                        text = "Monthly EMI",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "₹ %,.2f", monthlyEmi),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                    )
                }

                // Total Debt
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Debt",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "₹ %,.2f", totalOutstanding),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                    )
                }
            }
        }
    }
}
