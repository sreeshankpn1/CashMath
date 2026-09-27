package com.cashmath.app.ui.screens.dues.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.data.EmiLoanEntity
import com.cashmath.app.ui.screens.formatIndianCurrency
import com.cashmath.app.ui.theme.LocalThemeIsDark
import java.util.Locale

@Composable
fun LoanCard(
    loan: EmiLoanEntity,
    onMarkPaidClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCalculateSavingsClick: (() -> Unit)? = null,
    onUndoLastPaymentClick: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    var showPayConfirmDialog by remember { mutableStateOf(false) }

    val isDark = LocalThemeIsDark.current

    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val titleText = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val mutedText = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val emeraldFill = if (isDark) Color(0xFF34D399) else Color(0xFF059669)
    val trackBg = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)

    val totalMonths = if (loan.totalTenureMonths > 0) loan.totalTenureMonths else 1
    val paidMonths = loan.paidMonths.coerceAtLeast(0)
    val remainingMonths = (totalMonths - paidMonths).coerceAtLeast(0)
    val progressPercentage = ((paidMonths.toFloat() / totalMonths.toFloat()) * 100).toInt().coerceIn(0, 100)
    val progressFraction = (paidMonths.toFloat() / totalMonths.toFloat()).coerceIn(0f, 1f)
    val isFullyPaid = paidMonths >= totalMonths

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.dp),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            // --- 1. HEADER ROW: Title, Bank Subtitle & Monthly EMI ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = loan.loanName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = titleText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = loan.lenderBank,
                        fontSize = 12.sp,
                        color = mutedText
                    )
                }

                Text(
                    text = "₹ ${String.format(Locale.getDefault(), "%,.0f", loan.monthlyEmi)}/mo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = titleText
                )

                Spacer(modifier = Modifier.width(4.dp))

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = mutedText,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(cardBg)
                    ) {
                        if (onCalculateSavingsClick != null) {
                            DropdownMenuItem(
                                text = { Text("💡 Interest Savings", color = titleText) },
                                leadingIcon = { Icon(Icons.Default.Calculate, contentDescription = null, tint = emeraldFill, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showMenu = false
                                    onCalculateSavingsClick()
                                }
                            )
                        }

                        if (paidMonths > 0 && onUndoLastPaymentClick != null) {
                            DropdownMenuItem(
                                text = { Text("Undo Last Payment", color = titleText) },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showMenu = false
                                    onUndoLastPaymentClick()
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("Edit Loan", color = titleText) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = titleText, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Delete Loan", color = Color(0xFFDC2626)) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- 2. TENURE PROGRESS ROW ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$paidMonths of $totalMonths paid ($progressPercentage%)",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = mutedText
                )
                Text(
                    text = "$remainingMonths months left",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = emeraldFill
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(2.5.dp)),
                color = emeraldFill,
                trackColor = trackBg
            )

            Spacer(modifier = Modifier.height(10.dp))

            // --- 3. FINANCIAL BALANCE & ACTION ROW (2-Column Stats + Compact Button) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 2-Column Stats: Paid vs Remaining
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Paid", fontSize = 10.sp, color = mutedText)
                        Text(
                            text = formatIndianCurrency(paidMonths * loan.monthlyEmi),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleText
                        )
                    }

                    Column {
                        Text("Remaining", fontSize = 10.sp, color = mutedText)
                        Text(
                            text = formatIndianCurrency(remainingMonths * loan.monthlyEmi),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleText
                        )
                    }
                }

                // Compact "Mark Paid" Button
                if (!isFullyPaid) {
                    val btnBg = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                    val btnText = if (isDark) Color(0xFF0F172A) else Color.White

                    Button(
                        onClick = { showPayConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = btnBg),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Mark Paid ✓", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = btnText)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFECFDF5),
                        border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                    ) {
                        Text(
                            text = "Paid ✓",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF059669),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }

    // Accidental Tap Protection Confirmation Dialog
    if (showPayConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showPayConfirmDialog = false },
            containerColor = cardBg,
            shape = RoundedCornerShape(20.dp),
            title = { Text("Confirm EMI Payment", fontWeight = FontWeight.Bold, color = titleText, fontSize = 17.sp) },
            text = {
                Text(
                    text = "Mark Installment #${paidMonths + 1} of $totalMonths (${formatIndianCurrency(loan.monthlyEmi)}) as paid for ${loan.loanName}?",
                    fontSize = 13.sp,
                    color = mutedText
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPayConfirmDialog = false
                        onMarkPaidClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = emeraldFill),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("✓ Confirm Paid", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPayConfirmDialog = false }) {
                    Text("Cancel", color = mutedText)
                }
            }
        )
    }
}
