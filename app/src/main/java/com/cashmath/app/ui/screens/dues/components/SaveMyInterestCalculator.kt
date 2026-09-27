package com.cashmath.app.ui.screens.dues.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.data.EmiLoanEntity
import com.cashmath.app.ui.screens.formatIndianCurrency
import com.cashmath.app.ui.theme.*

fun calculateSavings(
    principal: Double,
    annualRate: Double,
    monthlyEmi: Double,
    extraPaymentMonthly: Double
): Pair<Double, Int> {
    val monthlyRate = annualRate / (12 * 100)
    if (monthlyRate <= 0.0 || monthlyEmi <= 0.0) return Pair(0.0, 0)

    // Standard amortization loop without extra payment
    var balanceStandard = principal
    var totalInterestStandard = 0.0
    while (balanceStandard > 0.0) {
        val interest = balanceStandard * monthlyRate
        val principalPaid = monthlyEmi - interest
        if (principalPaid <= 0.0) break
        totalInterestStandard += interest
        balanceStandard -= principalPaid
    }

    // Amortization loop with extra payment
    var balanceAccelerated = principal
    var totalInterestAccelerated = 0.0
    var monthsAccelerated = 0
    while (balanceAccelerated > 0.0) {
        val interest = balanceAccelerated * monthlyRate
        val principalPaid = (monthlyEmi + extraPaymentMonthly) - interest
        if (principalPaid <= 0.0) break
        totalInterestAccelerated += interest
        balanceAccelerated -= principalPaid
        monthsAccelerated++
    }

    val interestSaved = (totalInterestStandard - totalInterestAccelerated).coerceAtLeast(0.0)
    return Pair(interestSaved, monthsAccelerated)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanPrepaymentCalculatorSheet(
    loan: EmiLoanEntity,
    onDismiss: () -> Unit
) {
    var extraMonthlyText by remember { mutableStateOf("1000") }
    var interestRateText by remember { mutableStateOf("10.5") }

    val extraMonthly = extraMonthlyText.toDoubleOrNull() ?: 0.0
    val annualRate = interestRateText.toDoubleOrNull() ?: 10.5
    val totalMonths = loan.totalTenureMonths
    val remainingMonths = (totalMonths - loan.paidMonths).coerceAtLeast(1)
    val remainingPrincipal = loan.monthlyEmi * remainingMonths

    val (interestSaved, monthsAccelerated) = calculateSavings(
        principal = remainingPrincipal,
        annualRate = annualRate,
        monthlyEmi = loan.monthlyEmi,
        extraPaymentMonthly = extraMonthly
    )

    val tenureReduction = (remainingMonths - monthsAccelerated).coerceAtLeast(0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Savings, contentDescription = null, tint = EmeraldGreen)
                    Spacer(Modifier.width(8.dp))
                    Text("Save My Interest 💰", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Calculate interest saved by paying a small extra amount monthly towards '${loan.loanName}'", fontSize = 12.sp, color = SlateGray)

            Spacer(Modifier.height(16.dp))

            // Inputs
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = extraMonthlyText,
                    onValueChange = { extraMonthlyText = it },
                    label = { Text("Extra Monthly (₹)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = interestRateText,
                    onValueChange = { interestRateText = it },
                    label = { Text("Interest Rate (% p.a.)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Spacer(Modifier.height(16.dp))

            // Savings Banner Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EmeraldLight),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CashInGreenBorder)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Total Interest You Save 💰", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                    Text(formatIndianCurrency(interestSaved), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldGreen)
                    Spacer(Modifier.height(4.dp))
                    Text("Pay off loan $tenureReduction months earlier! ($monthsAccelerated months instead of $remainingMonths months)", fontSize = 12.sp, color = EmeraldGreen, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalIndigo),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Got It!", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
