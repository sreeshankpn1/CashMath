package com.cashmath.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.data.BookEntity
import com.cashmath.app.data.BusinessEntity
import com.cashmath.app.data.model.BusinessMemberEntity
import com.cashmath.app.subscription.SubscriptionManager
import com.cashmath.app.subscription.SubscriptionState
import com.cashmath.app.subscription.UserPlan
import com.cashmath.app.ui.BookSortOption
import com.cashmath.app.ui.components.BankSmsInboxBottomSheet
import com.cashmath.app.ui.components.BusinessTopBar
import com.cashmath.app.ui.screens.members.ManageMembersBottomSheet
import com.cashmath.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksDashboardScreen(
    businesses: List<BusinessEntity>,
    selectedBusiness: BusinessEntity?,
    books: List<BookEntity>,
    members: List<BusinessMemberEntity> = emptyList(),
    searchQuery: String,
    sortOption: BookSortOption,
    onSearchQueryChange: (String) -> Unit,
    onSortOptionChange: (BookSortOption) -> Unit,
    onBusinessSelected: (Long) -> Unit,
    onAddBusiness: (String) -> Unit,
    onRenameBusiness: (Long, String) -> Unit,
    onDeleteBusiness: (BusinessEntity) -> Unit,
    onBookSelected: (Long) -> Unit,
    onAddBook: (String) -> Unit,
    onRenameBook: (BookEntity, String) -> Unit,
    onDuplicateBook: (BookEntity) -> Unit,
    onMoveBook: (Long, Long) -> Unit,
    onDeleteBook: (BookEntity) -> Unit,
    onSaveMember: (BusinessMemberEntity) -> Unit = {},
    onDeleteMember: (BusinessMemberEntity) -> Unit = {},
    onOpenTeamManagement: () -> Unit = {}
) {
    val isDark = LocalThemeIsDark.current
    val topIconTint = if (isDark) Color(0xFF818CF8) else RoyalIndigo
    val fabContainerColor = if (isDark) Color(0xFF4F46E5) else Color(0xFF0F172A)

    var showBusinessSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    var showAddBookDialog by remember { mutableStateOf(false) }
    var showTeamSheet by remember { mutableStateOf(false) }
    var showPaywallSheet by remember { mutableStateOf(false) }
    var showSmsInboxSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val subManager = remember { SubscriptionManager(context) }
    val subState by subManager.subscriptionState.collectAsState(
        initial = SubscriptionState(
            isPro = false,
            plan = UserPlan.FREE,
            expiryTimestamp = 0L,
            maxBusinessesAllowed = 1,
            maxMembersPerBusiness = 2
        )
    )

    Scaffold(
        topBar = {
            BusinessTopBar(
                activeBusinessName = selectedBusiness?.name ?: "Select Business",
                onSwitchBusinessClick = { showBusinessSheet = true },
                actions = {
                    IconButton(onClick = { showSmsInboxSheet = true }) {
                        Icon(Icons.Default.Sms, contentDescription = "Bank SMS Queue", tint = EmeraldGreen)
                    }
                    IconButton(onClick = {
                        showTeamSheet = true
                        onOpenTeamManagement()
                    }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Team Members", tint = topIconTint)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddBookDialog = true },
                containerColor = fabContainerColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Add New Book", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
        ) {
            // Search & Sort Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search by book name", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SlateGray) },
                    modifier = Modifier.weight(1f),
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

                Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick = { showSortSheet = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Surface)
                ) {
                    Icon(Icons.Default.SortByAlpha, contentDescription = "Sort Books", tint = topIconTint)
                }
            }

            Text(
                "Your Books",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Books List
            if (books.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No books found. Tap 'Add New Book' to get started.", color = SlateGray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(books) { book ->
                        BookListItem(
                            book = book,
                            onClick = { onBookSelected(book.id) },
                            onRename = { onRenameBook(book, it) },
                            onDelete = { onDeleteBook(book) }
                        )
                    }
                }
            }
        }
    }

    // Business Switcher Sheet
    if (showBusinessSheet) {
        BusinessSelectorSheet(
            businesses = businesses,
            selectedId = selectedBusiness?.id,
            onDismiss = { showBusinessSheet = false },
            onSelect = { onBusinessSelected(it); showBusinessSheet = false },
            onAdd = { newBizName ->
                if (businesses.size >= subState.maxBusinessesAllowed) {
                    showPaywallSheet = true
                } else {
                    onAddBusiness(newBizName)
                    showBusinessSheet = false
                }
            },
            onRenameBusiness = onRenameBusiness,
            onDeleteBusiness = onDeleteBusiness
        )
    }

    // Unified CRED/Apple Dark Team & Permissions Sheet
    if (showTeamSheet) {
        ManageMembersBottomSheet(
            businessName = selectedBusiness?.name ?: "Business",
            businessId = selectedBusiness?.id?.toString() ?: "1",
            books = books,
            members = members,
            onDismiss = { showTeamSheet = false },
            onSaveMember = onSaveMember,
            onDeleteMember = onDeleteMember
        )
    }

    if (showPaywallSheet) {
        PaywallBottomSheet(
            onDismiss = { showPaywallSheet = false },
            onSubscriptionSuccess = { showPaywallSheet = false }
        )
    }

    if (showSmsInboxSheet) {
        BankSmsInboxBottomSheet(
            books = books,
            onDismiss = { showSmsInboxSheet = false },
            onSaveTransactionToBook = { bookId, amount, isCashIn, partyName, remark ->
                showSmsInboxSheet = false
            }
        )
    }

    // Sort Options Sheet
    if (showSortSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(Modifier.padding(24.dp).padding(bottom = 32.dp)) {
                Text(
                    text = "Sort Books By",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
                listOf(
                    BookSortOption.LAST_UPDATED to "Last Updated",
                    BookSortOption.NAME_A_Z to "Name (A to Z)",
                    BookSortOption.BALANCE_HIGH_TO_LOW to "Net Balance (High to Low)",
                    BookSortOption.BALANCE_LOW_TO_HIGH to "Net Balance (Low to High)",
                    BookSortOption.LAST_CREATED to "Last Created"
                ).forEach { (option, label) ->
                    val isSelected = sortOption == option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSortOptionChange(option)
                                showSortSheet = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                onSortOptionChange(option)
                                showSortSheet = false
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    // Add Book Dialog
    if (showAddBookDialog) {
        AddBookDialog(
            onDismiss = { showAddBookDialog = false },
            onConfirm = { bookName ->
                onAddBook(bookName)
                showAddBookDialog = false
            }
        )
    }
}