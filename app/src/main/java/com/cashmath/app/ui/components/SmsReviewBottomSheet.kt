package com.cashmath.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.data.BookEntity
import com.cashmath.app.service.ClassifiedSmsTransaction
import com.cashmath.app.ui.screens.formatIndianCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsReviewBottomSheet(
    classifiedTx: ClassifiedSmsTransaction,
    books: List<BookEntity>,
    onDismiss: () -> Unit,
    onSaveToBook: (bookId: Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDark = isSystemInDarkTheme()

    val sheetBg = if (isDark) Color(0xFF131927) else Color.White
    val cardBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val titleText = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val mutedText = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val isDebit = classifiedTx.isDebit
    val primaryAccent = if (!isDebit) {
        if (isDark) Color(0xFF10B981) else Color(0xFF059669)
    } else {
        if (isDark) Color(0xFFEF4444) else Color(0xFFDC2626)
    }

    var selectedBookId by remember(books) { mutableStateOf(books.firstOrNull()?.id ?: 1L) }

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
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = null,
                        tint = primaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SMS Transaction Detected 📱",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleText
                    )
                }

                IconButton(
                    onClick = onDismiss,
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

            Spacer(modifier = Modifier.height(16.dp))

            // Detected Transaction Hero Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, primaryAccent.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = primaryAccent.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, primaryAccent.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = if (isDebit) "• CASH OUT (DEBIT)" else "• CASH IN (CREDIT)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryAccent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = formatIndianCurrency(classifiedTx.amount),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryAccent
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = classifiedTx.partyOrSender,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = titleText
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "\"${classifiedTx.rawText}\"",
                        fontSize = 11.sp,
                        color = mutedText,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Cashbook Selector Section
            Text(
                text = "SELECT CASHBOOK TO SAVE ENTRY:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = mutedText,
                letterSpacing = 1.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (books.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(books) { book ->
                        val isSelected = book.id == selectedBookId
                        Surface(
                            onClick = { selectedBookId = book.id },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) primaryAccent.copy(alpha = 0.15f) else cardBg,
                            border = BorderStroke(1.dp, if (isSelected) primaryAccent else cardBorder),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = if (isSelected) primaryAccent else mutedText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = book.name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) primaryAccent else titleText
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = primaryAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Primary Cashbook (Default)",
                    fontSize = 13.sp,
                    color = titleText,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Ignore", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = mutedText)
                }

                Button(
                    onClick = {
                        onSaveToBook(selectedBookId)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.5f).height(48.dp)
                ) {
                    Text("Save to Selected Book", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
