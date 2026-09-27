package com.cashmath.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.data.BookEntity
import com.cashmath.app.data.BusinessEntity
import com.cashmath.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookHubScreen(
    businesses: List<BusinessEntity>,
    selectedBusiness: BusinessEntity?,
    books: List<BookEntity>,
    onBusinessSelected: (Long) -> Unit,
    onAddBusiness: (String) -> Unit,
    onRenameBusiness: (Long, String) -> Unit,
    onDeleteBusiness: (BusinessEntity) -> Unit,
    onBookSelected: (Long) -> Unit,
    onAddBook: (String) -> Unit,
    onRenameBook: (BookEntity, String) -> Unit,
    onDeleteBook: (BookEntity) -> Unit
) {
    var showBusinessSheet by remember { mutableStateOf(false) }
    var showAddBookDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.clickable { showBusinessSheet = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = RoyalIndigo)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                selectedBusiness?.name ?: "Select Business",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Tap to switch business ▾", style = MaterialTheme.typography.labelSmall, color = SlateGray)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* Invite Member */ }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Invite")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddBookDialog = true },
                containerColor = RoyalIndigo,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add new book")
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Surface) {
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(Icons.Default.Payments, contentDescription = null) },
                    label = { Text("Payments") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Book, contentDescription = null) },
                    label = { Text("Books") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
        ) {
            item {
                BannerSection()
            }
            item {
                EducationalCard()
            }
            item {
                SectionHeader("Your Books")
            }
            if (books.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("No books yet in this business", color = SlateGray)
                    }
                }
            } else {
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

    if (showBusinessSheet) {
        BusinessSelectorSheet(
            businesses = businesses,
            selectedId = selectedBusiness?.id,
            onDismiss = { showBusinessSheet = false },
            onSelect = { onBusinessSelected(it); showBusinessSheet = false },
            onAdd = { onAddBusiness(it); showBusinessSheet = false },
            onRenameBusiness = onRenameBusiness,
            onDeleteBusiness = onDeleteBusiness
        )
    }

    if (showAddBookDialog) {
        AddBookDialog(
            onDismiss = { showAddBookDialog = false },
            onConfirm = { onAddBook(it); showAddBookDialog = false }
        )
    }
}

@Composable
fun BannerSection() {
    Card(
        modifier = Modifier.padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CloudDone, contentDescription = null, tint = RoyalIndigo, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                "CashMath • 100% Free Offline & Cloud Backup",
                style = MaterialTheme.typography.labelMedium,
                color = RoyalIndigo,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EducationalCard() {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Outline),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MenuBook, contentDescription = null, tint = RoyalIndigo, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text("CashMath Basics", fontWeight = FontWeight.Bold)
                Text("Learn how to manage your ledgers", style = MaterialTheme.typography.bodySmall, color = SlateGray)
            }
            Spacer(Modifier.weight(1f))
            Text("Know more >", color = RoyalIndigo, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SlateGray)
        Row {
            IconButton(onClick = {}) { Icon(Icons.Default.SortByAlpha, contentDescription = null, tint = SlateGray) }
            IconButton(onClick = {}) { Icon(Icons.Default.Search, contentDescription = null, tint = SlateGray) }
        }
    }
}

@Composable
fun BookListItem(book: BookEntity, onClick: () -> Unit, onRename: (String) -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isDark = LocalThemeIsDark.current
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val titleColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val positiveGreen = if (isDark) Color(0xFF34D399) else Color(0xFF047857)
    val negativeRed = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
    val iconBg = if (isDark) Color(0xFF334155) else Color(0xFFEEF2FF)
    val iconTint = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4338CA)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, cardBorder),
        elevation = CardDefaults.cardElevation(if (isDark) 0.dp else 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Book, contentDescription = null, tint = iconTint)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(book.name, fontWeight = FontWeight.Bold, color = titleColor, fontSize = 15.sp, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                Text("Updated just now", style = MaterialTheme.typography.labelSmall, color = subtitleColor)
            }
            Text(
                "₹ ${"%.2f".format(book.balance)}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = if (book.balance >= 0) positiveGreen else negativeRed
            )
            Box {
                IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, contentDescription = null, tint = subtitleColor) }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(cardBg)) {
                    DropdownMenuItem(
                        text = { Text("Rename", color = titleColor) },
                        onClick = { showRenameDialog = true; showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = titleColor) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = negativeRed) },
                        onClick = { showDeleteDialog = true; showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = negativeRed) }
                    )
                }
            }
        }
    }

    if (showRenameDialog) {
        RenameBookDialog(currentName = book.name, onDismiss = { showRenameDialog = false }, onConfirm = { onRename(it); showRenameDialog = false })
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = cardBg,
            title = { Text("Delete Book?", color = titleColor, fontWeight = FontWeight.Bold) },
            text = { Text("All entries in '${book.name}' will be permanently removed.", color = subtitleColor) },
            confirmButton = { Button(onClick = { onDelete(); showDeleteDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = negativeRed)) { Text("Delete", color = Color.White) } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel", color = subtitleColor) } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessSelectorSheet(
    businesses: List<BusinessEntity>,
    selectedId: Long?,
    onDismiss: () -> Unit,
    onSelect: (Long) -> Unit,
    onAdd: (String) -> Unit,
    onRenameBusiness: (Long, String) -> Unit,
    onDeleteBusiness: (BusinessEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.PartiallyExpanded }
    )
    val haptic = LocalHapticFeedback.current

    var showAddDialog by remember { mutableStateOf(false) }
    var businessToRename by remember { mutableStateOf<BusinessEntity?>(null) }
    var businessToDelete by remember { mutableStateOf<BusinessEntity?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0A0E17),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Select Business",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Your businesses • Under your subscription",
                        fontSize = 13.sp,
                        color = Color(0xFF8E9BB0)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF141A28), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                businesses.forEach { biz ->
                    val isSelected = selectedId == biz.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelect(biz.id)
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141A28)),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF00E676) else Color(0xFF222C42))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF192132)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFF00E676) else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = biz.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = biz.role.ifBlank { "Primary Admin" },
                                    fontSize = 12.sp,
                                    color = Color(0xFF8E9BB0)
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            IconButton(
                                onClick = { businessToRename = biz },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFF192132), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Rename",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = { businessToDelete = biz },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFF192132), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom "+ Add new Business" Button
            OutlinedButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF222C42)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF141A28))
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("+ Add new Business", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showAddDialog) {
        AddBusinessDialog(onDismiss = { showAddDialog = false }, onConfirm = { onAdd(it); showAddDialog = false })
    }

    businessToRename?.let { biz: BusinessEntity ->
        RenameBusinessDialog(
            currentName = biz.name,
            onDismiss = { businessToRename = null },
            onConfirm = { newName: String ->
                onRenameBusiness(biz.id, newName)
                businessToRename = null
            }
        )
    }

    businessToDelete?.let { biz ->
        AlertDialog(
            onDismissRequest = { businessToDelete = null },
            containerColor = Color(0xFF141A28),
            shape = RoundedCornerShape(20.dp),
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF5252)) },
            title = { Text("Delete Business '${biz.name}'?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Deleting this business will permanently erase all associated books and transactions. This action cannot be undone.",
                    color = Color(0xFF8E9BB0)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = biz
                        businessToDelete = null
                        onDismiss()
                        onDeleteBusiness(target)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete Business", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { businessToDelete = null }) {
                    Text("Cancel", color = Color(0xFF8E9BB0))
                }
            }
        )
    }
}

@Composable
fun AddBookDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var bookName by remember { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = if (isDark) Color(0xFF131927) else Color.White,
        title = {
            Text(
                text = "New Book",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
            )
        },
        text = {
            Column {
                Text(
                    text = "Create a separate cashbook for your shop, project, or personal expenses.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = bookName,
                    onValueChange = { bookName = it },
                    placeholder = { Text("e.g., Daily Shop Sales, Office Expenses, Project Ledger") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                        unfocusedContainerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                        focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                        unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                        focusedPlaceholderColor = Color(0xFF94A3B8),
                        unfocusedPlaceholderColor = Color(0xFF94A3B8)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (bookName.isNotBlank()) {
                        onConfirm(bookName.trim())
                    }
                },
                enabled = bookName.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White,
                    disabledContainerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                    disabledContentColor = Color(0xFF94A3B8)
                )
            ) {
                Text("Create Book", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Cancel",
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

@Composable
fun AddBusinessDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var businessName by remember { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = if (isDark) Color(0xFF131927) else Color.White,
        title = {
            Text(
                text = "New Business",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
            )
        },
        text = {
            Column {
                Text(
                    text = "Create a new business profile to organize your ledgers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    placeholder = { Text("e.g., CashMath Enterprises, Hardware Store") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                        unfocusedContainerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                        focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                        unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                        focusedPlaceholderColor = Color(0xFF94A3B8),
                        unfocusedPlaceholderColor = Color(0xFF94A3B8)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (businessName.isNotBlank()) {
                        onConfirm(businessName.trim())
                    }
                },
                enabled = businessName.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White,
                    disabledContainerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                    disabledContentColor = Color(0xFF94A3B8)
                )
            ) {
                Text("Create Business", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Cancel",
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

@Composable
fun RenameBookDialog(currentName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141A28),
        shape = RoundedCornerShape(20.dp),
        title = { Text("Rename Book", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("New Name", color = Color(0xFF8E9BB0)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF00E676),
                    unfocusedBorderColor = Color(0xFF222C42),
                    cursorColor = Color(0xFF00E676)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFF8E9BB0)) }
        }
    )
}

@Composable
fun RenameBusinessDialog(currentName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141A28),
        shape = RoundedCornerShape(20.dp),
        title = { Text("Rename Business", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Business Name", color = Color(0xFF8E9BB0)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF00E676),
                    unfocusedBorderColor = Color(0xFF222C42),
                    cursorColor = Color(0xFF00E676)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFF8E9BB0)) }
        }
    )
}
