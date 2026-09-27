package com.cashmath.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.data.Converters
import com.cashmath.app.data.TransactionEntity
import com.cashmath.app.data.TransactionType
import com.cashmath.app.parser.DateTimeUtils
import com.cashmath.app.ui.components.AttachmentGalleryView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailSheet(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onEditTransaction: () -> Unit = {},
    onDeleteTransaction: (Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.PartiallyExpanded }
    )

    val isDark = isSystemInDarkTheme()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val isIncome = transaction.type == TransactionType.INCOME
    val accentColor = if (isIncome) {
        if (isDark) Color(0xFF10B981) else Color(0xFF059669)
    } else {
        if (isDark) Color(0xFFEF4444) else Color(0xFFDC2626)
    }

    val sheetBg = if (isDark) Color(0xFF131B2E) else Color.White
    val cardBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val titleText = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val mutedText = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        contentColor = titleText,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFCBD5E1)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = titleText
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = titleText)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isIncome) "CASH IN (+)" else "CASH OUT (-)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "₹ ${"%,.2f".format(transaction.amount)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Detail rows card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    DarkDetailRow(label = "Party / Contact", value = transaction.partyName ?: "N/A", icon = Icons.Default.Person, cardBg = cardBg, titleText = titleText, mutedText = mutedText)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = cardBorder)

                    DarkDetailRow(label = "Remark / Note", value = transaction.remark ?: "No remark", icon = Icons.Default.EditNote, cardBg = cardBg, titleText = titleText, mutedText = mutedText)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = cardBorder)

                    DarkDetailRow(label = "Payment Mode", value = transaction.paymentMode, icon = Icons.Default.Payments, cardBg = cardBg, titleText = titleText, mutedText = mutedText)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = cardBorder)

                    DarkDetailRow(
                        label = "Date & Time",
                        value = DateTimeUtils.formatTransactionTime(transaction.transactionTimestamp),
                        icon = Icons.Default.Schedule,
                        cardBg = cardBg,
                        titleText = titleText,
                        mutedText = mutedText
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = cardBorder)

                    DarkDetailRow(label = "Created By", value = transaction.createdByName, icon = Icons.Default.AccountCircle, cardBg = cardBg, titleText = titleText, mutedText = mutedText)
                }
            }

            if (!transaction.attachmentUri.isNullOrBlank()) {
                val attUri = transaction.attachmentUri!!
                Spacer(modifier = Modifier.height(16.dp))
                val paths = if (attUri.startsWith("[")) {
                    Converters().toStringList(attUri)
                } else {
                    listOf(attUri)
                }
                AttachmentGalleryView(
                    attachments = paths,
                    isEditable = false,
                    onAttachmentsChanged = {}
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Edit & Delete Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onEditTransaction()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, cardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = cardBg, contentColor = titleText)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = titleText)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Entry", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = titleText)
                }

                Button(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Entry", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = cardBg,
            shape = RoundedCornerShape(20.dp),
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
            title = { Text("Delete Entry?", color = titleText, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this entry of ₹ ${"%,.2f".format(transaction.amount)}? This action cannot be undone.", color = mutedText) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteTransaction(transaction.id)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = mutedText)
                }
            }
        )
    }
}

@Composable
private fun DarkDetailRow(
    label: String,
    value: String,
    icon: ImageVector,
    cardBg: Color,
    titleText: Color,
    mutedText: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(cardBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = mutedText, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 14.sp, color = titleText, fontWeight = FontWeight.SemiBold)
        }
    }
}
