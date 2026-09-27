package com.cashmath.app.ui.screens.dues.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.data.EmiLoanEntity
import com.cashmath.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLoanBottomSheet(
    loan: EmiLoanEntity,
    onDismiss: () -> Unit,
    onSave: (EmiLoanEntity) -> Unit
) {
    var loanName by remember(loan) { mutableStateOf(loan.loanName) }
    var bank by remember(loan) { mutableStateOf(loan.lenderBank) }
    var emiText by remember(loan) { mutableStateOf(loan.monthlyEmi.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }) }
    var dueDayText by remember(loan) { mutableStateOf(loan.dueDayOfMonth.toString()) }
    var tenureText by remember(loan) { mutableStateOf(loan.totalTenureMonths.toString()) }
    var paidMonthsText by remember(loan) { mutableStateOf(loan.paidMonths.toString()) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        cursorColor = RoyalIndigo,
        focusedBorderColor = RoyalIndigo,
        unfocusedBorderColor = BorderHairline
    )
    val fieldStyle = TextStyle(
        color = TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )

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
                Text("Edit Loan / EMI", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = loanName, onValueChange = { loanName = it }, label = { Text("Loan Name") }, colors = fieldColors, textStyle = fieldStyle, singleLine = true)
                OutlinedTextField(value = bank, onValueChange = { bank = it }, label = { Text("Bank / Lender") }, colors = fieldColors, textStyle = fieldStyle, singleLine = true)
                OutlinedTextField(value = emiText, onValueChange = { emiText = it }, label = { Text("Monthly EMI (₹)") }, colors = fieldColors, textStyle = fieldStyle, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = dueDayText, onValueChange = { dueDayText = it }, label = { Text("Due Day of Month") }, colors = fieldColors, textStyle = fieldStyle, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = tenureText, onValueChange = { tenureText = it }, label = { Text("Total Tenure (Months)") }, colors = fieldColors, textStyle = fieldStyle, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = paidMonthsText, onValueChange = { paidMonthsText = it }, label = { Text("Paid Months") }, colors = fieldColors, textStyle = fieldStyle, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    val emi = emiText.toDoubleOrNull() ?: loan.monthlyEmi
                    val day = dueDayText.toIntOrNull() ?: loan.dueDayOfMonth
                    val tenure = tenureText.toIntOrNull() ?: loan.totalTenureMonths
                    val paid = paidMonthsText.toIntOrNull() ?: loan.paidMonths

                    if (loanName.isNotBlank() && bank.isNotBlank() && emi > 0) {
                        val updated = loan.copy(
                            loanName = loanName,
                            lenderBank = bank,
                            monthlyEmi = emi,
                            dueDayOfMonth = day,
                            totalTenureMonths = tenure,
                            paidMonths = paid
                        )
                        onSave(updated)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalIndigo),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Changes", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
