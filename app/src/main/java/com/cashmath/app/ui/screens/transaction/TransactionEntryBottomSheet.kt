package com.cashmath.app.ui.screens.transaction

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.cashmath.app.data.Converters
import com.cashmath.app.data.TransactionEntity
import com.cashmath.app.data.TransactionType
import com.cashmath.app.ui.components.CashMathAllInOneKeypad
import com.cashmath.app.ui.theme.LocalThemeIsDark
import com.cashmath.app.util.ContactPickerHelper
import com.cashmath.app.util.FileStorageHelper
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class BrandPaymentPill(
    val id: String,
    val label: String,
    val badgeText: String,
    val brandColor: Color
)

enum class ActiveEntryField {
    AMOUNT,
    PARTY_NAME,
    REMARKS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEntryBottomSheet(
    bookId: Long,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    initialTransaction: TransactionEntity? = null,
    entryType: TransactionType = TransactionType.INCOME
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.PartiallyExpanded }
    )

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isDark = LocalThemeIsDark.current

    var activeInput by remember { mutableStateOf(ActiveEntryField.AMOUNT) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(activeInput) {
        if (activeInput == ActiveEntryField.AMOUNT) {
            keyboardController?.hide()
        }
    }

    val activeType = initialTransaction?.type ?: entryType
    val isCashIn = activeType == TransactionType.INCOME

    val primaryAccent = if (isCashIn) Color(0xFF1B5E20) else Color(0xFFD32F2F)

    val sheetBg = if (isDark) Color(0xFF131927) else Color.White
    val cardBg = if (isDark) Color(0xFF161F30) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val titleText = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val mutedText = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    var rawAmount by remember(initialTransaction) {
        mutableStateOf(
            initialTransaction?.amount?.let {
                if (it % 1.0 == 0.0) "%.0f".format(it) else it.toString()
            } ?: ""
        )
    }
    var partyName by remember(initialTransaction) { mutableStateOf(initialTransaction?.partyName ?: "") }
    var remarks by remember(initialTransaction) { mutableStateOf(initialTransaction?.remark ?: "") }
    var selectedPaymentMode by remember(initialTransaction) { mutableStateOf(initialTransaction?.paymentMode ?: "Cash") }
    var selectedTimestamp by remember(initialTransaction) { mutableLongStateOf(initialTransaction?.transactionTimestamp ?: System.currentTimeMillis()) }

    // Instant Reactive Attachment List State
    val attachmentPaths = remember(initialTransaction) {
        mutableStateListOf<String>().apply {
            initialTransaction?.attachmentUri?.let { uriStr ->
                if (uriStr.isNotBlank()) {
                    if (uriStr.startsWith("[")) {
                        addAll(Converters().toStringList(uriStr))
                    } else {
                        add(uriStr)
                    }
                }
            }
        }
    }
    var previewingUri by remember { mutableStateOf<String?>(null) }

    val brandPaymentPills = listOf(
        BrandPaymentPill("Cash", "Cash", "💵", Color(0xFF2E7D32)),
        BrandPaymentPill("GPay", "GPay", "G", Color(0xFF1A73E8)),
        BrandPaymentPill("PhonePe", "PhonePe", "पे", Color(0xFF5F259F)),
        BrandPaymentPill("Paytm", "Paytm", "paytm", Color(0xFF00B9F1)),
        BrandPaymentPill("UPI", "UPI", "UPI", Color(0xFFE65100))
    )

    val multiPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 3)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val available = 3 - attachmentPaths.size
            if (available > 0) {
                val saved = FileStorageHelper.saveMultipleFilesLocally(context, uris.take(available))
                attachmentPaths.addAll(saved)
            }
        }
    }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri: Uri? ->
        contactUri?.let { uri ->
            val (name, _) = ContactPickerHelper.extractContactDetails(context, uri)
            if (name.isNotBlank()) partyName = name
        }
    }

    fun handleClose() {
        if ((rawAmount.isNotBlank() && rawAmount != "0") || partyName.isNotBlank() || remarks.isNotBlank() || attachmentPaths.isNotEmpty()) {
            showDiscardDialog = true
        } else {
            onDismiss()
        }
    }

    fun openDateTimePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val dateCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        dateCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        dateCal.set(Calendar.MINUTE, minute)
                        dateCal.set(Calendar.SECOND, 0)
                        selectedTimestamp = dateCal.timeInMillis
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    false
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val dateTimeFormatted = remember(selectedTimestamp) {
        val timeStr = SimpleDateFormat("h:mm a", Locale.ENGLISH).format(Date(selectedTimestamp))
        val dateStr = SimpleDateFormat("dd MMM", Locale.ENGLISH).format(Date(selectedTimestamp))
        val todayStr = SimpleDateFormat("dd MMM", Locale.ENGLISH).format(Date())
        if (dateStr == todayStr) "Today, $timeStr" else "$dateStr, $timeStr"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        contentColor = titleText,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // TIER 1: PINNED TOP HEADER & BORDERLESS HERO AMOUNT
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = primaryAccent.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, primaryAccent.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(primaryAccent, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isCashIn) "• CASH IN" else "• CASH OUT",
                                color = primaryAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Interactive Date & Time Picker Chip
                        Surface(
                            onClick = { openDateTimePicker() },
                            shape = RoundedCornerShape(8.dp),
                            color = cardBg,
                            border = BorderStroke(1.dp, cardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "Select Date & Time",
                                    tint = primaryAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = dateTimeFormatted,
                                    color = titleText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { handleClose() },
                            modifier = Modifier
                                .size(32.dp)
                                .background(cardBg, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = titleText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Borderless Minimalist Hero Amount Display (Smart Focus Toggle to Keypad)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            activeInput = ActiveEntryField.AMOUNT
                            keyboardController?.hide()
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "₹ ${if (rawAmount.isEmpty()) "0" else formatDisplay(rawAmount)}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryAccent,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // TIER 2: SCROLLABLE MIDDLE BODY (Details, Branded Payment Mode Pills, Attachments)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
            ) {
                // Authentic Branded Payment Mode Micro-Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(brandPaymentPills) { item ->
                        val isSelected = selectedPaymentMode.equals(item.id, ignoreCase = true) ||
                                selectedPaymentMode.equals(item.label, ignoreCase = true)
                        val unselectedBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF5F5F5)
                        val unselectedBorder = if (isDark) Color(0xFF334155) else Color(0xFFE0E0E0)
                        val unselectedText = if (isDark) Color(0xFF94A3B8) else Color(0xFF424242)

                        Surface(
                            onClick = {
                                selectedPaymentMode = item.id
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) item.brandColor else unselectedBg,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) item.brandColor else unselectedBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White.copy(alpha = 0.25f) else Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.badgeText,
                                        fontSize = if (item.badgeText.length > 2) 8.sp else 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) Color.White else item.brandColor
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else unselectedText
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Single Inset-Grouped Details Card with 1.dp Dividers
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Party Name Row
                        OutlinedTextField(
                            value = partyName,
                            onValueChange = { partyName = it },
                            label = { Text("Party / Customer Name", color = mutedText) },
                            placeholder = { Text("e.g., Sharma Enterprises, Rahul Verma, Vendor", color = mutedText) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = primaryAccent) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    try {
                                        contactPickerLauncher.launch(null)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }) {
                                    Icon(Icons.Default.Contacts, contentDescription = "Pick Contact", tint = primaryAccent)
                                }
                            },
                            colors = themeTextFieldColors(primaryAccent, Color.Transparent, Color.Transparent, titleText, mutedText),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        activeInput = ActiveEntryField.PARTY_NAME
                                    }
                                }
                        )

                        HorizontalDivider(color = cardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))

                        // Remarks / Notes Row
                        OutlinedTextField(
                            value = remarks,
                            onValueChange = { remarks = it },
                            label = { Text("Remarks / Notes (Optional)", color = mutedText) },
                            placeholder = { Text("e.g., Stock purchase invoice, Client advance, Monthly rent", color = mutedText) },
                            leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null, tint = primaryAccent) },
                            colors = themeTextFieldColors(primaryAccent, Color.Transparent, Color.Transparent, titleText, mutedText),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        activeInput = ActiveEntryField.REMARKS
                                    }
                                }
                        )

                        HorizontalDivider(color = cardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))

                        // Attachments Strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Attachments (${attachmentPaths.size}/3)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = mutedText
                            )
                            if (attachmentPaths.size < 3) {
                                TextButton(
                                    onClick = {
                                        multiPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = primaryAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "+ Add Bill / Photo",
                                        color = primaryAccent,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (attachmentPaths.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(attachmentPaths) { path ->
                                    val file = File(path)
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
                                            .clickable { previewingUri = path }
                                    ) {
                                        if (file.exists()) {
                                            AsyncImage(
                                                model = file,
                                                contentDescription = "Receipt Attachment",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Description,
                                                contentDescription = null,
                                                tint = mutedText,
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }

                                        // Individual Delete Badge
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .align(Alignment.TopEnd)
                                                .padding(2.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.65f))
                                                .clickable { attachmentPaths.remove(path) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove photo",
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // TIER 3: PINNED BOTTOM INLINE KEYPAD & ACTION BUTTON
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = sheetBg
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    AnimatedVisibility(
                        visible = activeInput == ActiveEntryField.AMOUNT,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        CashMathAllInOneKeypad(
                            expression = rawAmount,
                            onExpressionChange = { rawAmount = it },
                            onEvaluated = { /* Real-time evaluation */ },
                            accentColor = primaryAccent,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    val evaluated = rawAmount.toDoubleOrNull() ?: 0.0
                    Button(
                        onClick = {
                            if (evaluated > 0.0) {
                                val finalUri = if (attachmentPaths.isNotEmpty()) Converters().fromStringList(attachmentPaths.toList()) else null
                                val entity = initialTransaction?.copy(
                                    amount = evaluated,
                                    partyName = partyName.trim().ifBlank { null },
                                    remark = remarks.trim().ifBlank { null },
                                    paymentMode = selectedPaymentMode,
                                    attachmentUri = finalUri,
                                    transactionTimestamp = selectedTimestamp,
                                    updatedAt = System.currentTimeMillis()
                                ) ?: TransactionEntity(
                                    bookId = bookId,
                                    amount = evaluated,
                                    type = activeType,
                                    partyName = partyName.trim().ifBlank { null },
                                    remark = remarks.trim().ifBlank { null },
                                    paymentMode = selectedPaymentMode,
                                    attachmentUri = finalUri,
                                    transactionTimestamp = selectedTimestamp,
                                    createdAt = System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )
                                onSave(entity)
                                onDismiss()
                            } else {
                                Toast.makeText(context, "Please enter an amount > 0", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = evaluated > 0.0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryAccent,
                            disabledContainerColor = cardBg
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = if (isEditMode) {
                                "Update Entry (${formatDisplay(rawAmount)})"
                            } else {
                                if (isCashIn) "Save Cash In (${formatDisplay(rawAmount)})" else "Save Cash Out (${formatDisplay(rawAmount)})"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // Full-Screen Image Preview Dialog
    previewingUri?.let { path ->
        val file = File(path)
        Dialog(onDismissRequest = { previewingUri = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (file.exists()) {
                        AsyncImage(
                            model = file,
                            contentDescription = "Full Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = mutedText,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    IconButton(
                        onClick = { previewingUri = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            containerColor = cardBg,
            shape = RoundedCornerShape(20.dp),
            title = { Text("Discard Entry?", fontWeight = FontWeight.Bold, color = titleText) },
            text = { Text("You have unsaved changes. Do you want to discard this entry?", color = mutedText) },
            confirmButton = {
                Button(
                    onClick = {
                        showDiscardDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Discard", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep Editing", color = mutedText, fontWeight = FontWeight.Medium)
                }
            }
        )
    }
}

private fun formatDisplay(raw: String): String {
    if (raw.isEmpty()) return "0"
    return try {
        val clean = raw.replace("₹", "").trim()
        if (clean.contains(".")) {
            val parts = clean.split(".")
            val whole = parts[0].toLongOrNull()?.let { DecimalFormat("#,##,###").format(it) } ?: "0"
            "$whole.${parts.getOrNull(1) ?: ""}"
        } else {
            clean.toLongOrNull()?.let { DecimalFormat("#,##,###").format(it) } ?: clean
        }
    } catch (_: Exception) {
        raw.replace("₹", "").trim()
    }
}

@Composable
private fun themeTextFieldColors(
    accentColor: Color,
    cardBg: Color,
    cardBorder: Color,
    titleText: Color,
    mutedText: Color
) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = cardBg,
    unfocusedContainerColor = cardBg,
    focusedTextColor = titleText,
    unfocusedTextColor = titleText,
    focusedBorderColor = accentColor,
    unfocusedBorderColor = cardBorder,
    focusedLabelColor = accentColor,
    unfocusedLabelColor = mutedText
)
