package com.cashmath.app.ui.screens.khata

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.cashmath.app.data.DuePartyEntity
import com.cashmath.app.data.model.PartyTransactionEntity
import com.cashmath.app.parser.DateTimeUtils
import com.cashmath.app.ui.screens.dues.ReminderPreviewBottomSheet
import com.cashmath.app.ui.screens.formatIndianCurrency
import com.cashmath.app.utils.WhatsAppReminderHelper
import kotlin.math.abs

private val DeepObsidian = Color(0xFF0A0E17)
private val SurfaceCard = Color(0xFF141A28)
private val CardBorder = Color(0xFF222C42)
private val NeonEmerald = Color(0xFF00E676)
private val NeonCoral = Color(0xFFFF5252)
private val MutedLabel = Color(0xFF8E9BB0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyDetailScreen(
    party: DuePartyEntity,
    transactions: List<PartyTransactionEntity> = emptyList(),
    businessName: String = "CashMath Ledger",
    onNavigateBack: () -> Unit,
    onAddTransaction: (amount: Double, isCredit: Boolean, remarks: String, mode: String) -> Unit = { _, _, _, _ -> },
    onSettleBalance: (amount: Double, paymentMode: String, isFullSettlement: Boolean, recordInCashbook: Boolean) -> Unit
) {
    val context = LocalContext.current

    var showSettleSheet by remember { mutableStateOf(false) }
    var showWhatsAppPreviewSheet by remember { mutableStateOf(false) }
    var showAddTxSheet by remember { mutableStateOf(false) }
    var addTxIsCredit by remember { mutableStateOf(false) }

    val netBalance = party.toReceive - party.toPay
    val isReceivable = netBalance > 0
    val isPayable = netBalance < 0
    val absBalance = abs(netBalance)

    val currentMsg = remember(party, netBalance) {
        WhatsAppReminderHelper.getMessage(
            context = context,
            partyName = party.name,
            amountDue = absBalance,
            businessName = businessName,
            isReceivable = isReceivable
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = party.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        if (!party.phone.isNullOrBlank()) {
                            Text(
                                text = party.phone,
                                fontSize = 12.sp,
                                color = MutedLabel
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (!party.phone.isNullOrBlank()) {
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, "tel:${party.phone}".toUri())
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.Phone, contentDescription = "Call", tint = NeonEmerald)
                        }

                        IconButton(onClick = { showWhatsAppPreviewSheet = true }) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "WhatsApp", tint = NeonEmerald)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepObsidian)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceCard,
                tonalElevation = 8.dp,
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // RED BUTTON: You Gave (Udhaar Given / Debit)
                    Button(
                        onClick = {
                            addTxIsCredit = false
                            showAddTxSheet = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCoral),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("You Gave ₹", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }

                    // GREEN BUTTON: You Got (Payment Received / Credit)
                    Button(
                        onClick = {
                            addTxIsCredit = true
                            showAddTxSheet = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("You Got ₹", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                    }
                }
            }
        },
        containerColor = DeepObsidian
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Net Balance Summary Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(
                    1.dp,
                    when {
                        isReceivable -> NeonEmerald.copy(alpha = 0.4f)
                        isPayable -> NeonCoral.copy(alpha = 0.4f)
                        else -> CardBorder
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when {
                            isReceivable -> "YOU WILL GET (CUSTOMER OWES YOU)"
                            isPayable -> "YOU WILL GIVE (SUPPLIER DUE)"
                            else -> "SETTLED / NO BALANCE"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isReceivable -> NeonEmerald
                            isPayable -> NeonCoral
                            else -> MutedLabel
                        },
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = formatIndianCurrency(absBalance),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            isReceivable -> NeonEmerald
                            isPayable -> NeonCoral
                            else -> Color.White
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (absBalance > 0) {
                        Button(
                            onClick = { showSettleSheet = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isReceivable) NeonEmerald else NeonCoral
                            )
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "1-Tap Settle Balance (${formatIndianCurrency(absBalance)})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction History Timeline
            Text(
                text = "Khata Timeline History",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MutedLabel
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (transactions.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No credit/debit history recorded yet.", color = MutedLabel, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions) { tx ->
                        val isCredit = tx.isCredit
                        val itemColor = if (isCredit) NeonEmerald else NeonCoral

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(itemColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            tint = itemColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = tx.remarks.ifBlank { if (isCredit) "Payment Received (Got)" else "Udhaar Given (Gave)" },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${DateTimeUtils.formatTransactionTime(tx.date)} • ${tx.paymentMode}",
                                            fontSize = 11.sp,
                                            color = MutedLabel
                                        )
                                    }
                                }

                                Text(
                                    text = "${if (isCredit) "+" else "-"}${formatIndianCurrency(tx.amount)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = itemColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddTxSheet) {
        AddPartyTxBottomSheet(
            partyName = party.name,
            isCredit = addTxIsCredit,
            onDismiss = { showAddTxSheet = false },
            onConfirm = { amount, remarks, mode ->
                onAddTransaction(amount, addTxIsCredit, remarks, mode)
                Toast.makeText(context, "Entry recorded successfully! ✓", Toast.LENGTH_SHORT).show()
                showAddTxSheet = false
            }
        )
    }

    if (showSettleSheet) {
        SettleBalanceBottomSheet(
            partyName = party.name,
            netBalance = netBalance,
            onDismiss = { showSettleSheet = false },
            onConfirmSettlement = { amount, paymentMode, isFullSettlement, recordInCashbook ->
                onSettleBalance(amount, paymentMode, isFullSettlement, recordInCashbook)
                Toast.makeText(context, "Settlement of ${formatIndianCurrency(amount)} recorded! ✓", Toast.LENGTH_SHORT).show()
                showSettleSheet = false
            }
        )
    }

    if (showWhatsAppPreviewSheet) {
        ReminderPreviewBottomSheet(
            partyName = party.name,
            partyPhone = party.phone ?: "",
            initialMessage = currentMsg,
            defaultMessage = currentMsg,
            onDismiss = { showWhatsAppPreviewSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPartyTxBottomSheet(
    partyName: String,
    isCredit: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, remarks: String, mode: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var remarksText by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("Cash") }

    val primaryColor = if (isCredit) NeonEmerald else NeonCoral
    val titleText = if (isCredit) "You Got (Payment Received)" else "You Gave (Udhaar Given)"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "$titleText - $partyName",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount (₹)", color = MutedLabel) },
                placeholder = { Text("e.g. 17000", color = MutedLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = remarksText,
                onValueChange = { remarksText = it },
                label = { Text("Note / Remarks", color = MutedLabel) },
                placeholder = { Text("e.g. Bill #102, Cash payment, Goods, etc.", color = MutedLabel) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Cash", "Online", "UPI").forEach { mode ->
                    val isSelected = paymentMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { paymentMode = mode },
                        label = { Text(mode) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor.copy(alpha = 0.2f),
                            selectedLabelColor = primaryColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0.0) {
                        onConfirm(amount, remarksText.trim(), paymentMode)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Save Entry", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (isCredit) Color.Black else Color.White)
            }
        }
    }
}
