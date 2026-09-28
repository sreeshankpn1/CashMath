package com.cashmath.app.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.data.BookEntity
import com.cashmath.app.data.EmiLoanEntity
import com.cashmath.app.service.BankSmsInboxManager
import com.cashmath.app.service.BankSmsItem
import com.cashmath.app.service.ClassifiedSmsTransaction
import com.cashmath.app.ui.screens.formatIndianCurrency
import com.cashmath.app.ui.theme.LocalThemeIsDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankSmsInboxBottomSheet(
    books: List<BookEntity>,
    @Suppress("UNUSED_PARAMETER") loans: List<EmiLoanEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSaveTransactionToBook: (bookId: Long, amount: Double, isCashIn: Boolean, partyName: String, remark: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val isDark = LocalThemeIsDark.current

    val sheetBg = if (isDark) Color(0xFF131927) else Color.White
    val cardBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val titleText = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val mutedText = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val emeraldAccent = if (isDark) Color(0xFF10B981) else Color(0xFF059669)

    var itemsList by remember {
        mutableStateOf(BankSmsInboxManager.loadPendingItems(context))
    }

    var reviewItem by remember { mutableStateOf<BankSmsItem?>(null) }

    fun refreshList() {
        itemsList = BankSmsInboxManager.loadPendingItems(context)
    }

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
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = null,
                        tint = emeraldAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pending Queue (${itemsList.size})",
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

            Spacer(modifier = Modifier.height(14.dp))

            if (itemsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = mutedText, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No pending transactions. Incoming UPI and bank alerts will appear here for 1-tap entry.",
                            fontSize = 13.sp,
                            color = mutedText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("UNPROCESSED BANK TRANSACTIONS:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = mutedText, letterSpacing = 1.sp)
                    TextButton(onClick = {
                        BankSmsInboxManager.clearAll(context)
                        refreshList()
                    }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Queue", fontSize = 11.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                ) {
                    items(itemsList, key = { it.id }) { item ->
                        val itemAccent = if (item.isCashIn) {
                            if (isDark) Color(0xFF10B981) else Color(0xFF059669)
                        } else {
                            if (isDark) Color(0xFFEF4444) else Color(0xFFDC2626)
                        }

                        val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH).format(Date(item.timestamp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(1.dp, cardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(item.bankName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = titleText)
                                        Text(dateStr, fontSize = 11.sp, color = mutedText)
                                    }

                                    Text(
                                        text = "${if (item.isCashIn) "+" else "-"} ${formatIndianCurrency(item.amount)}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = itemAccent
                                    )
                                }

                                if (!item.suggestedLoanTitle.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFEEF2FF),
                                        border = BorderStroke(0.5.dp, Color(0xFF818CF8))
                                    ) {
                                        Text(
                                            text = "💡 Matched EMI for: ${item.suggestedLoanTitle}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4338CA),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text("\"${item.snippet}\"", fontSize = 11.sp, color = mutedText, maxLines = 2)

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = {
                                        BankSmsInboxManager.removeItem(context, item.id)
                                        refreshList()
                                    }) {
                                        Text("Dismiss", fontSize = 12.sp, color = mutedText)
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Button(
                                        onClick = { reviewItem = item },
                                        colors = ButtonDefaults.buttonColors(containerColor = itemAccent),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Add to Book ✓", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Review Item Assignment
    reviewItem?.let { item ->
        SmsReviewBottomSheet(
            classifiedTx = ClassifiedSmsTransaction(
                amount = item.amount,
                isDebit = !item.isCashIn,
                partyOrSender = item.bankName,
                referenceToken = null,
                rawText = item.snippet
            ),
            books = books,
            onDismiss = { reviewItem = null },
            onSaveToBook = { bookId ->
                onSaveTransactionToBook(
                    bookId,
                    item.amount,
                    item.isCashIn,
                    item.bankName,
                    "Auto-tracked via Bank SMS (${item.bankName})"
                )
                BankSmsInboxManager.removeItem(context, item.id)
                reviewItem = null
                refreshList()
                Toast.makeText(context, "Saved entry to Cashbook! ✓", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
