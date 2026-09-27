package com.cashmath.app.ui.screens.dues.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.data.EmiLoanEntity
import com.cashmath.app.ui.screens.formatIndianCurrency
import kotlin.math.pow

data class SavingsResult(
    val interestSaved: Double,
    val monthsSaved: Int,
    val isZeroInterest: Boolean
)

fun calculateLoanSavings(
    principal: Double,
    monthlyEmi: Double,
    annualRate: Double,
    extraMonthly: Double
): SavingsResult {
    if (principal <= 0.0 || monthlyEmi <= 0.0) return SavingsResult(0.0, 0, false)

    val r = annualRate / 1200.0 // Monthly interest rate
    val isZeroInterest = (annualRate <= 0.0 || (principal / monthlyEmi).toInt().toDouble() * monthlyEmi == principal)

    // 1. Normal Schedule
    var bal1 = principal
    var totalInterest1 = 0.0
    var months1 = 0
    while (bal1 > 1.0 && months1 < 600) {
        val interest = if (isZeroInterest) 0.0 else bal1 * r
        val principalPaid = if (isZeroInterest) monthlyEmi else (monthlyEmi - interest)
        if (principalPaid <= 0) break
        totalInterest1 += interest
        bal1 -= principalPaid
        months1++
    }

    // 2. Accelerated Schedule with extra payment
    var bal2 = principal
    var totalInterest2 = 0.0
    var months2 = 0
    val acceleratedEmi = monthlyEmi + extraMonthly
    while (bal2 > 1.0 && months2 < 600) {
        val interest = if (isZeroInterest) 0.0 else bal2 * r
        val principalPaid = if (isZeroInterest) acceleratedEmi else ((acceleratedEmi - interest).coerceAtMost(bal2))
        totalInterest2 += interest
        bal2 -= principalPaid
        months2++
    }

    val interestSaved = (totalInterest1 - totalInterest2).coerceAtLeast(0.0)
    val monthsSaved = (months1 - months2).coerceAtLeast(0)
    return SavingsResult(interestSaved, monthsSaved, isZeroInterest)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanCalculatorsBottomSheet(
    loans: List<EmiLoanEntity> = emptyList(),
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDark = isSystemInDarkTheme()

    val sheetBg = if (isDark) Color(0xFF131B2E) else Color.White
    val cardBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val titleText = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val mutedText = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val emeraldAccent = if (isDark) Color(0xFF10B981) else Color(0xFF059669)

    // Re-ordered Tabs: Tab 0 (Default Active Tab) = "Interest Saver", Tab 1 = "EMI Calculator"
    var selectedTab by remember { mutableIntStateOf(0) }

    // Interest Saver Real Amortization State
    var selectedLoanId by remember(loans) { mutableStateOf(loans.firstOrNull()?.id) }
    val selectedLoan = remember(loans, selectedLoanId) {
        loans.find { it.id == selectedLoanId } ?: loans.firstOrNull()
    }

    var extraPrepaymentInput by remember { mutableStateOf("2000") }
    var annualInterestRateInput by remember { mutableStateOf("10.5") }

    val extraPrepay = extraPrepaymentInput.toDoubleOrNull() ?: 0.0
    val annualInterestRate = annualInterestRateInput.toDoubleOrNull() ?: 0.0

    val calcPrincipal = selectedLoan?.let { (it.totalTenureMonths - it.paidMonths) * it.monthlyEmi } ?: 500000.0
    val calcEmi = selectedLoan?.monthlyEmi ?: 12500.0

    val savings = remember(calcPrincipal, calcEmi, annualInterestRate, extraPrepay) {
        calculateLoanSavings(calcPrincipal, calcEmi, annualInterestRate, extraPrepay)
    }

    // EMI Calc State
    var principalInput by remember { mutableStateOf("500000") }
    var rateInput by remember { mutableStateOf("9.5") }
    var tenureYearsInput by remember { mutableStateOf("5") }

    val principal = principalInput.toDoubleOrNull() ?: 0.0
    val rateMonthly = (rateInput.toDoubleOrNull() ?: 0.0) / (12 * 100)
    val tenureMonths = ((tenureYearsInput.toDoubleOrNull() ?: 0.0) * 12).toInt()

    val calculatedEmi = remember(principal, rateMonthly, tenureMonths) {
        if (principal > 0 && rateMonthly > 0 && tenureMonths > 0) {
            val emi = (principal * rateMonthly * (1 + rateMonthly).pow(tenureMonths)) /
                    ((1 + rateMonthly).pow(tenureMonths) - 1)
            if (emi.isNaN() || emi.isInfinite()) 0.0 else emi
        } else 0.0
    }

    val totalPayment = calculatedEmi * tenureMonths
    val totalInterest = (totalPayment - principal).coerceAtLeast(0.0)

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
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Financial Loan Tools 💡", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = titleText)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = titleText)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Re-ordered Segmented Pill Tabs (Interest Saver First)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == 0) emeraldAccent else Color.Transparent)
                            .clickable { selectedTab = 0 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Interest Saver 💡", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == 0) Color.White else titleText)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == 1) emeraldAccent else Color.Transparent)
                            .clickable { selectedTab = 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("EMI Calculator 🧮", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == 1) Color.White else titleText)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // TAB 0: INTEREST SAVER (Default Active Tab)
                Text(
                    text = if (loans.isNotEmpty()) "Select Active Loan to Optimize:" else "Select an Active Loan (or Custom Calculation)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = mutedText
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (loans.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        items(loans) { loan ->
                            val isSelected = selectedLoan?.id == loan.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) emeraldAccent else cardBg)
                                    .border(1.dp, if (isSelected) emeraldAccent else cardBorder, RoundedCornerShape(10.dp))
                                    .clickable { selectedLoanId = loan.id }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(loan.loanName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else titleText)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Dynamic Results Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, emeraldAccent.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Savings, contentDescription = null, tint = emeraldAccent, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(6.dp))

                        if (savings.isZeroInterest || annualInterestRate <= 0.0) {
                            Text("0% NO-COST EMI LOAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = emeraldAccent, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Clear Liability ${savings.monthsSaved} Months Earlier!", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = emeraldAccent)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("This loan has no interest charges. Your extra payments reduce your debt tenure without bank interest penalties, freeing up ${formatIndianCurrency(calcEmi)}/mo in cashflow sooner.", fontSize = 11.sp, color = mutedText, textAlign = TextAlign.Center)
                        } else {
                            Text("ESTIMATED INTEREST SAVINGS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = mutedText, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("SAVE ${formatIndianCurrency(savings.interestSaved)}", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = emeraldAccent)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("🏆 Pay off loan ${savings.monthsSaved} months earlier!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = emeraldAccent)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val tfColors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg,
                    focusedTextColor = titleText,
                    unfocusedTextColor = titleText,
                    focusedBorderColor = emeraldAccent,
                    unfocusedBorderColor = cardBorder,
                    focusedLabelColor = emeraldAccent,
                    unfocusedLabelColor = mutedText
                )

                OutlinedTextField(
                    value = extraPrepaymentInput,
                    onValueChange = { extraPrepaymentInput = it },
                    label = { Text("Extra Monthly Prepayment (₹)") },
                    colors = tfColors,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Only display Annual Interest Rate for interest-bearing loans
                if (!savings.isZeroInterest) {
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = annualInterestRateInput,
                        onValueChange = { annualInterestRateInput = it },
                        label = { Text("Loan Annual Interest Rate (% p.a.)") },
                        colors = tfColors,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // TAB 1: EMI CALCULATOR
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, emeraldAccent.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ESTIMATED MONTHLY EMI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = mutedText)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatIndianCurrency(calculatedEmi), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = emeraldAccent)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Interest: ${formatIndianCurrency(totalInterest)}", fontSize = 11.sp, color = mutedText)
                            Text("Total Paid: ${formatIndianCurrency(totalPayment)}", fontSize = 11.sp, color = mutedText)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val tfColors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg,
                    focusedTextColor = titleText,
                    unfocusedTextColor = titleText,
                    focusedBorderColor = emeraldAccent,
                    unfocusedBorderColor = cardBorder,
                    focusedLabelColor = emeraldAccent,
                    unfocusedLabelColor = mutedText
                )

                OutlinedTextField(
                    value = principalInput,
                    onValueChange = { principalInput = it },
                    label = { Text("Loan Amount / Principal (₹)") },
                    colors = tfColors,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rateInput,
                        onValueChange = { rateInput = it },
                        label = { Text("Interest Rate (% p.a.)") },
                        colors = tfColors,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = tenureYearsInput,
                        onValueChange = { tenureYearsInput = it },
                        label = { Text("Tenure (Years)") },
                        colors = tfColors,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
