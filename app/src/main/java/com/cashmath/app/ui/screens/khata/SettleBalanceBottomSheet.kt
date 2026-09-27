package com.cashmath.app.ui.screens.khata

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
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
import com.cashmath.app.ui.screens.formatIndianCurrency
import kotlin.math.abs

private val DeepObsidian = Color(0xFF0A0E17)
private val SurfaceCard = Color(0xFF141A28)
private val CardBorder = Color(0xFF222C42)
private val NeonEmerald = Color(0xFF00E676)
private val NeonCoral = Color(0xFFFF5252)
private val MutedLabel = Color(0xFF8E9BB0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettleBalanceBottomSheet(
    partyName: String,
    netBalance: Double,
    onDismiss: () -> Unit,
    onConfirmSettlement: (amount: Double, paymentMode: String, isFullSettlement: Boolean, recordInCashbook: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val absBalance = abs(netBalance)
    val isReceivable = netBalance > 0
    val accentColor = if (isReceivable) NeonEmerald else NeonCoral

    var amountText by remember { mutableStateOf(if (absBalance % 1.0 == 0.0) "%.0f".format(absBalance) else absBalance.toString()) }
    var selectedMode by remember { mutableStateOf("Cash") }
    var recordInCashbook by remember { mutableStateOf(true) }

    val paymentModes = listOf("Cash", "GPay", "PhonePe", "Paytm", "Bank")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepObsidian,
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF334155)) }
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "1-Tap Settle Balance",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Settling khata account for $partyName",
                        fontSize = 13.sp,
                        color = MutedLabel
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .background(SurfaceCard, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Outstanding Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isReceivable) "TOTAL OUTSTANDING (YOU'LL GET)" else "TOTAL PAYABLE (YOU'LL GIVE)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatIndianCurrency(absBalance),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settlement Amount Input
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Settlement Amount (₹)", color = MutedLabel) },
                leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = accentColor) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = CardBorder,
                    cursorColor = accentColor
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Payment Mode Chips
            Text("Payment Mode", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MutedLabel)
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(paymentModes) { mode ->
                    val isSelected = selectedMode == mode
                    Surface(
                        onClick = { selectedMode = mode },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) accentColor else SurfaceCard,
                        border = BorderStroke(1.dp, if (isSelected) Color.Transparent else CardBorder),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Record in Cashbook Checkbox
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { recordInCashbook = !recordInCashbook },
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, CardBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = recordInCashbook,
                        onCheckedChange = { recordInCashbook = it },
                        colors = CheckboxDefaults.colors(checkedColor = NeonEmerald)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Record as Cash In/Out in Cashbook",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Auto-creates a linked transaction entry in active ledger",
                            fontSize = 11.sp,
                            color = MutedLabel
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Confirm Settlement CTA
            val settleAmt = amountText.toDoubleOrNull() ?: 0.0
            Button(
                onClick = {
                    if (settleAmt > 0.0) {
                        val isFull = settleAmt >= absBalance
                        onConfirmSettlement(settleAmt, selectedMode, isFull, recordInCashbook)
                        onDismiss()
                    }
                },
                enabled = settleAmt > 0.0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Confirm Settlement (${formatIndianCurrency(settleAmt)})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
