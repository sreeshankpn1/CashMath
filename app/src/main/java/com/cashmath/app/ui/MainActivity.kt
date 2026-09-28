package com.cashmath.app.ui

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.cashmath.app.service.DailyPulseScheduler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.cashmath.app.data.*
import com.cashmath.app.service.PartyTransactionManager
import com.cashmath.app.ui.screens.khata.PartyDetailScreen
import com.cashmath.app.parser.DateFilterType
import com.cashmath.app.parser.DateTimeUtils
import com.cashmath.app.security.BiometricAuthHelper
import com.cashmath.app.security.BiometricSecurityManager
import com.cashmath.app.service.CsvReportGenerator
import com.cashmath.app.service.DuesEmiReportGenerator
import com.cashmath.app.service.EmiReminderWorker
import com.cashmath.app.service.InviteManager
import com.cashmath.app.service.PdfReportGenerator
import com.cashmath.app.utils.WhatsAppReminderHelper
import com.cashmath.app.ui.components.AppLockedScreen
import com.cashmath.app.ui.screens.*
import com.cashmath.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : FragmentActivity() {

    private var isAppAuthenticatedState = mutableStateOf(false)
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (savedInstanceState != null) {
            isAppAuthenticatedState.value = true
        }
        
        setContent {
            val settingsVm: SettingsViewModel = viewModel()
            settingsViewModel = settingsVm

            val isBiometricEnabled by settingsVm.isBiometricEnabled.collectAsState()
            val themeMode by settingsVm.themeMode.collectAsState()

            val isDarkTheme = when (themeMode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            CashMathTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val viewModel: BookViewModel = viewModel()
                val isAppAuthenticated by isAppAuthenticatedState
                val scope = rememberCoroutineScope()
                val context = LocalContext.current
                var showDpdpDisclosure by remember { mutableStateOf(false) }
                var showUpiOnboardingDialog by remember { mutableStateOf(false) }

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { _ -> }

                // Schedule Automated EMI Due Reminders & First Launch Permission Onboarding
                LaunchedEffect(Unit) {
                    EmiReminderWorker.scheduleDailyEmiCheck(context)

                    val prefs = context.getSharedPreferences("rupeemath_settings_prefs",
                        MODE_PRIVATE
                    )
                    val isFirstLaunch = prefs.getBoolean("is_first_launch", true)
                    val isPulseEnabled = prefs.getBoolean("pulse_enabled", true)
                    val pulseHour = prefs.getInt("pulse_hour", 21)
                    val pulseMinute = prefs.getInt("pulse_minute", 0)

                    if (isFirstLaunch) {
                        prefs.edit().putBoolean("is_first_launch", false).apply()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    if (isPulseEnabled) {
                        DailyPulseScheduler.schedule(context, pulseHour, pulseMinute, true)
                    }

                    val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
                    if (!enabledPackages.contains(context.packageName)) {
                        showUpiOnboardingDialog = true
                    }
                }

                // Handle Deep Link
                LaunchedEffect(intent) {
                    intent?.data?.let { uri ->
                        val (bookIdStr, _) = InviteManager.parseInviteUri(uri)
                        val bookId = bookIdStr?.toLongOrNull()
                        if (bookId != null) {
                            scope.launch {
                                AppDatabase.getDatabase(context).bookDao().insertMember(
                                    BookMemberEntity(
                                        bookId = bookId,
                                        userIdentifier = "Guest",
                                        displayName = "Guest User",
                                        role = "VIEWER"
                                    )
                                )
                                navController.navigate("ledger/$bookId")
                                Toast.makeText(context, "Joined Shared Book!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = "splash",
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(280, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(220))
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(280, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(220))
                    },
                    popEnterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(280, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(220))
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(280, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(220))
                    }
                ) {
                    composable("splash") {
                        SplashScreen(onSplashFinished = {
                            navController.navigate("hub") {
                                popUpTo("splash") { inclusive = true }
                            }
                        })
                    }
                    composable("hub") {
                        if (!isAppAuthenticated && isBiometricEnabled) {
                            AppLockedScreen(onUnlock = { authenticateApp() })
                        } else {
                            var selectedTab by remember { mutableIntStateOf(1) } // Default Tab 1 = Books

                            val businesses by viewModel.businesses.collectAsState()
                            val bizId by viewModel.selectedBusinessId.collectAsState()
                            val selectedBusiness = businesses.find { it.id == bizId } ?: businesses.firstOrNull()
                            
                            LaunchedEffect(businesses) {
                                if (bizId == null && businesses.isNotEmpty()) {
                                    viewModel.selectBusiness(businesses.first().id)
                                }
                            }

                            val books by viewModel.books.collectAsState()
                            val teamMembers by viewModel.teamMembers.collectAsState()
                            val searchQuery by viewModel.bookSearchQuery.collectAsState()
                            val sortOption by viewModel.bookSortOption.collectAsState()

                            val dueParties by viewModel.dueParties.collectAsState()
                            val totalToReceive by viewModel.totalToReceive.collectAsState()
                            val totalToPay by viewModel.totalToPay.collectAsState()
                            val emiLoans by viewModel.emiLoans.collectAsState()
                            val totalMonthlyEmi by viewModel.totalMonthlyEmi.collectAsState()
                            val duesEmiUiState by viewModel.duesEmiUiState.collectAsState()

                            Scaffold(
                                bottomBar = {
                                    NavigationBar(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.onSurface,
                                        tonalElevation = 8.dp,
                                        modifier = Modifier.border(width = 1.dp, color = MaterialTheme.colorScheme.outline)
                                    ) {
                                        NavigationBarItem(
                                            selected = selectedTab == 0,
                                            onClick = { selectedTab = 0 },
                                            icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Dues & EMI") },
                                            label = { Text("Dues & EMI", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                        NavigationBarItem(
                                            selected = selectedTab == 1,
                                            onClick = { selectedTab = 1 },
                                            icon = { Icon(Icons.Default.Book, contentDescription = "Books") },
                                            label = { Text("Books", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                        NavigationBarItem(
                                            selected = selectedTab == 2,
                                            onClick = { selectedTab = 2 },
                                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                            label = { Text("Settings", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                            ) { innerPadding ->
                                Box(modifier = Modifier.padding(innerPadding)) {
                                    when (selectedTab) {
                                        0 -> DuesEmiScreen(
                                            businessName = selectedBusiness?.name ?: "CashMath Business",
                                            businesses = businesses,
                                            selectedBusinessId = selectedBusiness?.id,
                                            onBusinessSelected = { viewModel.selectBusiness(it) },
                                            onAddBusiness = { viewModel.addBusiness(it) },
                                            dueParties = dueParties,
                                            totalToReceive = totalToReceive,
                                            totalToPay = totalToPay,
                                            emiLoans = emiLoans,
                                            totalMonthlyEmi = totalMonthlyEmi,
                                            uiState = duesEmiUiState,
                                            onPartyClick = { partyId ->
                                                navController.navigate("party_detail/$partyId")
                                            },
                                            onAddParty = { name, phone, toRec, toPay ->
                                                viewModel.addDueParty(name, phone, toRec, toPay)
                                            },
                                            onEditParty = { party ->
                                                BiometricAuthHelper.authenticate(
                                                    activity = this@MainActivity,
                                                    title = "Edit Party '${party.name}'",
                                                    subtitle = "Verify identity to edit party details.",
                                                    onSuccess = { viewModel.updateDueParty(party) }
                                                )
                                            },
                                            onDeleteParty = { id ->
                                                BiometricSecurityManager.authenticate(
                                                    activity = this@MainActivity,
                                                    title = "Delete Khata Party",
                                                    subtitle = "Verify identity to delete party.",
                                                    onSuccess = { viewModel.deleteDueParty(id) }
                                                )
                                            },
                                            onAddLoan = { name, bank, emi, day, tenure ->
                                                viewModel.addEmiLoan(name, bank, emi, day, tenure)
                                            },
                                            onEditLoan = { loan ->
                                                BiometricAuthHelper.authenticate(
                                                    activity = this@MainActivity,
                                                    title = "Edit Loan '${loan.loanName}'",
                                                    subtitle = "Verify identity to edit loan details.",
                                                    onSuccess = { viewModel.updateEmiLoan(loan) }
                                                )
                                            },
                                            onIncrementPaidMonth = { id -> viewModel.incrementPaidEmiMonth(id) },
                                            onDeleteLoan = { id ->
                                                BiometricSecurityManager.authenticate(
                                                    activity = this@MainActivity,
                                                    title = "Delete EMI Loan",
                                                    subtitle = "Verify identity to delete loan.",
                                                    onSuccess = { viewModel.deleteEmiLoan(id) }
                                                )
                                            },
                                            onShareDuesPdf = {
                                                scope.launch {
                                                    val fileName = WhatsAppReminderHelper.generateStandardFilename(
                                                        selectedBusiness?.name ?: "Business",
                                                        "Dues_Report",
                                                        "pdf"
                                                    )
                                                    val reportFile = File(context.cacheDir, "reports/$fileName")
                                                    reportFile.parentFile?.mkdirs()
                                                    DuesEmiReportGenerator.generatePdf(
                                                        selectedBusiness?.name ?: "Business",
                                                        dueParties,
                                                        emiLoans,
                                                        duesEmiUiState,
                                                        reportFile
                                                    )
                                                    shareReportFile(reportFile, "application/pdf")
                                                }
                                            },
                                            onShareDuesCsv = {
                                                scope.launch {
                                                    val fileName = WhatsAppReminderHelper.generateStandardFilename(
                                                        selectedBusiness?.name ?: "Business",
                                                        "Dues_Ledger",
                                                        "csv"
                                                    )
                                                    val reportFile = File(context.cacheDir, "reports/$fileName")
                                                    reportFile.parentFile?.mkdirs()
                                                    DuesEmiReportGenerator.generateCsv(dueParties, emiLoans, reportFile)
                                                    shareReportFile(reportFile, "text/csv")
                                                }
                                            }
                                        )
                                        1 -> BooksDashboardScreen(
                                            businesses = businesses,
                                            selectedBusiness = selectedBusiness,
                                            books = books,
                                            members = teamMembers,
                                            searchQuery = searchQuery,
                                            sortOption = sortOption,
                                            onSearchQueryChange = { viewModel.bookSearchQuery.value = it },
                                            onSortOptionChange = { viewModel.bookSortOption.value = it },
                                            onBusinessSelected = { viewModel.selectBusiness(it) },
                                            onAddBusiness = { viewModel.addBusiness(it) },
                                            onRenameBusiness = { businessId, newName ->
                                                viewModel.renameBusiness(businessId, newName)
                                            },
                                            onDeleteBusiness = { biz ->
                                                BiometricSecurityManager.authenticate(
                                                    activity = this@MainActivity,
                                                    title = "Delete Business '${biz.name}'",
                                                    subtitle = "Deleting this business will permanently erase all associated books.",
                                                    onSuccess = {
                                                        scope.launch {
                                                            viewModel.deleteBusinessCascade(biz.id)
                                                            val remaining = AppDatabase.getDatabase(context).businessDao().getAllBusinesses().first()
                                                            if (remaining.isEmpty()) {
                                                                val newBizId = AppDatabase.getDatabase(context).businessDao().insert(
                                                                    BusinessEntity(name = "My Business", role = "Primary Admin")
                                                                )
                                                                val newBookId = AppDatabase.getDatabase(context).bookDao().insertBook(
                                                                    BookEntity(businessId = newBizId, name = "Primary Ledger")
                                                                )
                                                                AppDatabase.getDatabase(context).bookDao().insertMember(
                                                                    BookMemberEntity(bookId = newBookId, userIdentifier = "Self", displayName = "You", role = "ADMIN")
                                                                )
                                                                viewModel.selectBusiness(newBizId)
                                                            } else {
                                                                viewModel.selectBusiness(remaining.first().id)
                                                            }
                                                            Toast.makeText(context, "Business deleted successfully", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                )
                                            },
                                            onBookSelected = { 
                                                viewModel.selectBook(it)
                                                navController.navigate("ledger/$it")
                                            },
                                            onAddBook = { viewModel.addBook(it) },
                                            onRenameBook = { book, newName ->
                                                viewModel.renameBook(book, newName)
                                            },
                                            onDuplicateBook = { book ->
                                                viewModel.duplicateBook(book)
                                            },
                                            onMoveBook = { bookId, targetBizId ->
                                                viewModel.moveBook(bookId, targetBizId)
                                            },
                                            onDeleteBook = { book ->
                                                BiometricSecurityManager.authenticate(
                                                    activity = this@MainActivity,
                                                    title = "Delete Book '${book.name}'",
                                                    subtitle = "Verify identity to delete book.",
                                                    onSuccess = { viewModel.deleteBook(book.id) }
                                                )
                                            },
                                            onSaveMember = { viewModel.saveTeamMember(it) },
                                            onDeleteMember = { viewModel.deleteTeamMember(it) },
                                            onOpenTeamManagement = {
                                                selectedTab = 2
                                            }
                                        )
                                        2 -> SettingsScreen(
                                            currentBusiness = selectedBusiness,
                                            isBiometricEnabled = isBiometricEnabled,
                                            themeMode = themeMode,
                                            books = books,
                                            members = teamMembers,
                                            onUpdateBusinessProfile = { updatedBiz ->
                                                viewModel.updateBusinessProfile(updatedBiz)
                                            },
                                            onToggleBiometricLock = { enabled ->
                                                settingsVm.setBiometricEnabled(enabled)
                                                Toast.makeText(context, "Biometric App Lock ${if (enabled) "Enabled" else "Disabled"}", Toast.LENGTH_SHORT).show()
                                            },
                                            onSelectThemeMode = { mode ->
                                                settingsVm.setThemeMode(mode)
                                            },
                                            onSaveMember = { viewModel.saveTeamMember(it) },
                                            onDeleteMember = { viewModel.deleteTeamMember(it) },
                                            onOpenAuthScreen = {
                                                navController.navigate("auth")
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    composable(
                        "ledger/{bookId}",
                        arguments = listOf(navArgument("bookId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
                        val books by viewModel.books.collectAsState()
                        val book = books.find { it.id == bookId }
                        val transactions by viewModel.transactions.collectAsState()
                        val balance by viewModel.bookBalance.collectAsState()
                        val totalIn by viewModel.totalIn.collectAsState()
                        val totalOut by viewModel.totalOut.collectAsState()
                        val members by AppDatabase.getDatabase(context).bookDao().getMembersForBook(bookId).collectAsState(initial = emptyList())
                        
                        val myMember = members.find { it.userIdentifier == "Self" } ?: members.firstOrNull()
                        val myRole = myMember?.role ?: "ADMIN"
                        val hasPermission = remember { isNotificationServiceEnabled() }

                        BookLedgerScreen(
                            book = book,
                            transactions = transactions,
                            netBalance = balance ?: 0.0,
                            totalIn = totalIn ?: 0.0,
                            totalOut = totalOut ?: 0.0,
                            myRole = myRole,
                            members = members,
                            hasPermission = hasPermission,
                            onBack = { navController.popBackStack() },
                            onRequestPermission = { showDpdpDisclosure = true },
                            onSaveTransaction = { tx ->
                                viewModel.addTransaction(tx)
                            },
                            onUpdateTransaction = { tx ->
                                BiometricAuthHelper.authenticate(
                                    activity = this@MainActivity,
                                    title = "Edit Entry",
                                    subtitle = "Verify identity to update transaction.",
                                    onSuccess = { viewModel.updateTransaction(tx) }
                                )
                            },
                            onDeleteTransaction = { txId ->
                                BiometricSecurityManager.authenticate(
                                    activity = this@MainActivity,
                                    title = "Confirm Deletion",
                                    subtitle = "Verify identity to delete transaction.",
                                    onSuccess = { viewModel.deleteTransaction(txId) }
                                )
                            },
                            onShowReports = {
                                navController.navigate("reports/$bookId")
                            },
                            onAddMember = { identifier, displayName, role ->
                                scope.launch {
                                    AppDatabase.getDatabase(context).bookDao().insertMember(
                                        BookMemberEntity(
                                            bookId = bookId,
                                            userIdentifier = identifier,
                                            displayName = displayName,
                                            role = role
                                        )
                                    )
                                }
                            },
                            onOpenTransactionEntry = { bId, type, entryId ->
                                val route = "transaction_entry/$bId/$type" + if (entryId != null && entryId > 0) "?entryId=$entryId" else ""
                                navController.navigate(route)
                            }
                        )
                    }
                    composable(
                        route = "transaction_entry/{bookId}/{type}?entryId={entryId}",
                        arguments = listOf(
                            navArgument("bookId") { type = NavType.LongType },
                            navArgument("type") { type = NavType.StringType },
                            navArgument("entryId") {
                                type = NavType.LongType
                                defaultValue = -1L
                            }
                        )
                    ) { backStackEntry ->
                        val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
                        val typeStr = backStackEntry.arguments?.getString("type") ?: "INCOME"
                        val entryId = backStackEntry.arguments?.getLong("entryId") ?: -1L
                        val entryType = if (typeStr == "EXPENSE") TransactionType.EXPENSE else TransactionType.INCOME

                        val transactions by viewModel.transactions.collectAsState()
                        val initialTx = if (entryId > 0) transactions.find { it.id == entryId } else null

                        TransactionEntryScreen(
                            bookId = bookId,
                            entryType = entryType,
                            isEditMode = initialTx != null,
                            initialTransaction = initialTx,
                            onBack = { navController.popBackStack() },
                            onSave = { entity ->
                                if (initialTx != null) {
                                    viewModel.updateTransaction(entity)
                                } else {
                                    viewModel.addTransaction(entity)
                                }
                            }
                        )
                    }
                    composable(
                        "reports/{bookId}",
                        arguments = listOf(navArgument("bookId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
                        val books by viewModel.books.collectAsState()
                        val book = books.find { it.id == bookId }
                        val selectedBusiness by viewModel.selectedBusiness.collectAsState(initial = null)
                        
                        var filter by remember { mutableStateOf(DateFilterType.THIS_MONTH) }
                        val bounds = DateTimeUtils.getDateBounds(filter)
                        
                        val dao = AppDatabase.getDatabase(context).transactionDao()
                        val catSummaries by dao.getCategorySummaries(bookId, bounds.first, bounds.second).collectAsState(initial = emptyList())
                        val partySummaries by dao.getPartySummaries(bookId, bounds.first, bounds.second).collectAsState(initial = emptyList())
                        val modeSummaries by dao.getPaymentModeSummaries(bookId, bounds.first, bounds.second).collectAsState(initial = emptyList())
                        val rangeIn by dao.getIncomeInRange(bookId, bounds.first, bounds.second).collectAsState(initial = 0.0)
                        val rangeOut by dao.getExpensesInRange(bookId, bounds.first, bounds.second).collectAsState(initial = 0.0)
                        
                        ReportsScreen(
                            bookName = book?.name ?: "Book",
                            categorySummaries = catSummaries,
                            partySummaries = partySummaries,
                            paymentModeSummaries = modeSummaries,
                            totalIn = rangeIn ?: 0.0,
                            totalOut = rangeOut ?: 0.0,
                            selectedFilter = filter,
                            onFilterSelected = { filter = it },
                            onBack = { navController.popBackStack() },
                            onSharePdf = {
                                scope.launch {
                                    val allTxs = viewModel.transactions.value.filter { it.transactionTimestamp in bounds.first..bounds.second }
                                    val fileName = WhatsAppReminderHelper.generateStandardFilename(
                                        selectedBusiness?.name ?: book?.name ?: "Ledger",
                                        "Statement",
                                        "pdf"
                                    )
                                    val reportFile = File(context.cacheDir, "reports/$fileName")
                                    reportFile.parentFile?.mkdirs()
                                    PdfReportGenerator.generatePdf(
                                        bookName = book?.name ?: "Book",
                                        dateRange = filter.name,
                                        transactions = allTxs,
                                        totalIn = rangeIn ?: 0.0,
                                        totalOut = rangeOut ?: 0.0,
                                        file = reportFile,
                                        businessName = selectedBusiness?.name ?: "Business",
                                        gstin = selectedBusiness?.gstNumber,
                                        phone = selectedBusiness?.phone,
                                        address = selectedBusiness?.address
                                    )
                                    shareReportFile(reportFile, "application/pdf")
                                }
                            },
                            onShareCsv = {
                                scope.launch {
                                    val allTxs = viewModel.transactions.value.filter { it.transactionTimestamp in bounds.first..bounds.second }
                                    val fileName = WhatsAppReminderHelper.generateStandardFilename(
                                        selectedBusiness?.name ?: book?.name ?: "Ledger",
                                        "Statement",
                                        "csv"
                                    )
                                    val reportFile = File(context.cacheDir, "reports/$fileName")
                                    reportFile.parentFile?.mkdirs()
                                    CsvReportGenerator.generateCsv(
                                        transactions = allTxs,
                                        file = reportFile,
                                        businessName = selectedBusiness?.name ?: "Business",
                                        phone = selectedBusiness?.phone,
                                        gstin = selectedBusiness?.gstNumber
                                    )
                                    shareReportFile(reportFile, "text/csv")
                                }
                            }
                        )
                    }
                    composable(
                        "party_detail/{partyId}",
                        arguments = listOf(navArgument("partyId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val partyId = backStackEntry.arguments?.getLong("partyId") ?: 0L
                        val dueParties by viewModel.dueParties.collectAsState()
                        val party = dueParties.find { it.id == partyId }

                        if (party != null) {
                            val selectedBusiness by viewModel.selectedBusiness.collectAsState(initial = null)
                            val bizName = selectedBusiness?.name ?: "CashMath Ledger"

                            val transactions = remember(party.id, party.updatedAt) {
                                PartyTransactionManager.getTransactions(context, party)
                            }

                            PartyDetailScreen(
                                party = party,
                                transactions = transactions,
                                businessName = bizName,
                                onNavigateBack = { navController.popBackStack() },
                                onAddTransaction = { amount, isCredit, remarks, mode ->
                                    viewModel.addPartyTransaction(context, party, amount, isCredit, remarks, mode)
                                },
                                onSettleBalance = { amount, paymentMode, isFullSettlement, recordInCashbook ->
                                    viewModel.settlePartyBalance(context, party, amount, paymentMode, isFullSettlement, recordInCashbook)
                                }
                            )
                        }
                    }
                    composable("auth") {
                        AuthScreen(
                            onAuthSuccess = { _ ->
                                navController.popBackStack()
                            },
                            onContinueOffline = {
                                navController.popBackStack()
                            }
                        )
                    }
                }

                if (showDpdpDisclosure) {
                    AlertDialog(
                        onDismissRequest = { showDpdpDisclosure = false },
                        icon = { Icon(Icons.Default.Security, contentDescription = null, tint = RoyalIndigo) },
                        title = { Text("DPDP Act 2023 Privacy Disclosure") },
                        text = {
                            Text(
                                "CashMath requires notification access to automatically record your UPI transactions. " +
                                "All financial data is processed and stored locally on your device in compliance with the DPDP Act 2023. " +
                                "Data is never uploaded or sold to external servers."
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showDpdpDisclosure = false
                                    openNotificationAccessSettings()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalIndigo)
                            ) {
                                Text("Proceed to Settings")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDpdpDisclosure = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                if (showUpiOnboardingDialog) {
                    AlertDialog(
                        onDismissRequest = { showUpiOnboardingDialog = false },
                        containerColor = Color(0xFF141A28),
                        shape = RoundedCornerShape(20.dp),
                        icon = {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(32.dp))
                        },
                        title = {
                            Text("Enable 1-Tap UPI Auto-Tracking", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        },
                        text = {
                            Text(
                                "CashMath automatically detects incoming payments from GPay, PhonePe, Paytm, and BHIM so you never type entries manually. 100% private, on-device only (DPDP Act 2023 compliant).",
                                color = Color(0xFF8E9BB0),
                                fontSize = 13.sp
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showUpiOnboardingDialog = false
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Open Settings to enable notification access", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Enable in Settings", fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showUpiOnboardingDialog = false }) {
                                Text("Maybe Later", color = Color(0xFF8E9BB0))
                            }
                        }
                    )
                }

                // Initial authentication trigger
                LaunchedEffect(Unit) {
                    if (!isAppAuthenticated && ::settingsViewModel.isInitialized && settingsViewModel.isBiometricEnabled.value) {
                        authenticateApp()
                    } else if (!isAppAuthenticated && ::settingsViewModel.isInitialized && !settingsViewModel.isBiometricEnabled.value) {
                        isAppAuthenticatedState.value = true
                    }
                }
            }
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":".toRegex())
            for (name in names) {
                val cn = ComponentName.unflattenFromString(name)
                if (cn != null && TextUtils.equals(pkgName, cn.packageName)) return true
            }
        }
        return false
    }

    private fun openNotificationAccessSettings() { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }

    private fun shareReportFile(file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share Report"))
    }

    private fun authenticateApp() {
        if (isAppAuthenticatedState.value) return
        if (::settingsViewModel.isInitialized && !settingsViewModel.isBiometricEnabled.value) {
            isAppAuthenticatedState.value = true
            return
        }
        BiometricSecurityManager.authenticate(
            activity = this,
            title = "Unlock CashMath",
            subtitle = "Verify identity to access your books",
            onSuccess = {
                runOnUiThread {
                    try {
                        isAppAuthenticatedState.value = true
                    } catch (e: Exception) {
                        Log.e("Biometric", "Unlock error", e)
                    }
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()
        if (!isAppAuthenticatedState.value) {
            authenticateApp()
        }
    }
}

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1000),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1800)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer(alpha = alphaAnim.value)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(BrandIndigoLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = RoyalIndigo,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("CashMath", color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "CashMath • Smart Financial Ledger",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
