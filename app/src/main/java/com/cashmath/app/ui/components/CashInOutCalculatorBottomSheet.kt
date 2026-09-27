package com.cashmath.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat

@Suppress("unused")
enum class CalculatorTransactionType {
    CASH_IN,
    CASH_OUT
}

private val DarkNavyBg = Color(0xFF0F1523)
private val DisplayCardBg = Color(0xFF182032)
private val NumberKeyBg = Color(0xFF1E283E)
private val OperatorKeyBg = Color(0xFF24324F)
private val OperatorBlue = Color(0xFF5E9CFF)
private val RedKeyBg = Color(0xFF381F26)
private val RedKeyText = Color(0xFFFF6B6B)
private val EmeraldGreen = Color(0xFF00875A)
private val CardinalRed = Color(0xFFDE350B)

@Suppress("unused")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashInOutCalculatorBottomSheet(
    transactionType: CalculatorTransactionType,
    modifier: Modifier = Modifier,
    initialAmount: Double = 0.0,
    onDismiss: () -> Unit = {},
    onConfirmAmount: (Double) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.PartiallyExpanded }
    )

    val isCashIn = transactionType == CalculatorTransactionType.CASH_IN
    val primaryColor = if (isCashIn) EmeraldGreen else CardinalRed
    val tagText = if (isCashIn) "• CASH IN ENTRY" else "• CASH OUT ENTRY"

    var expression by remember { mutableStateOf(if (initialAmount > 0) formatNumber(initialAmount) else "") }
    var currentAmountStr by remember { mutableStateOf(if (initialAmount > 0) formatNumber(initialAmount) else "0") }

    fun compute(expr: String): Double {
        if (expr.isBlank()) return 0.0
        return try {
            val sanitized = expr.replace("×", "*").replace("÷", "/")
            
            // Handle Percentage (%) calculations
            val pctRegex = Regex("""(\d+(?:\.\d+)?)\s*([+\-*/])\s*(\d+(?:\.\d+)?)%""")
            val match = pctRegex.find(sanitized)
            if (match != null) {
                val (xStr, op, yStr) = match.destructured
                val x = xStr.toDoubleOrNull() ?: 0.0
                val y = yStr.toDoubleOrNull() ?: 0.0
                return when (op) {
                    "+" -> x + (x * y / 100.0)
                    "-" -> x - (x * y / 100.0)
                    "*" -> x * (y / 100.0)
                    "/" -> if (y != 0.0) x / (y / 100.0) else 0.0
                    else -> x
                }
            }

            if (sanitized.endsWith("%")) {
                val num = sanitized.dropLast(1).toDoubleOrNull() ?: 0.0
                return num / 100.0
            }

            simpleEvaluate(sanitized)
        } catch (_: Exception) {
            0.0
        }
    }

    fun onKeyTapped(key: String) {
        when (key) {
            "C" -> {
                expression = ""
                currentAmountStr = "0"
            }
            "⌫" -> {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                    val result = compute(expression)
                    currentAmountStr = formatNumber(result)
                }
            }
            "=" -> {
                val result = compute(expression)
                expression = formatNumber(result)
                currentAmountStr = formatNumber(result)
            }
            "%", "+", "-", "×", "÷" -> {
                if (expression.isNotEmpty() && !expression.last().toString().matches(Regex("""[+\-×÷%]"""))) {
                    expression += key
                }
            }
            ".", "0", "00", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> {
                expression += key
                val result = compute(expression)
                currentAmountStr = formatNumber(result)
            }
        }
    }

    fun applyPercentage(percent: Double) {
        val base = compute(expression)
        if (base > 0) {
            val delta = base * (percent / 100.0)
            val updated = base + delta
            expression = formatNumber(updated)
            currentAmountStr = formatNumber(updated)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkNavyBg,
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(primaryColor.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tagText,
                        color = primaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Display Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DisplayCardBg)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    if (expression.isNotBlank()) {
                        Text(
                            text = expression,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "₹ $currentAmountStr",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick GST & Discount Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionChip(label = "+5% GST", color = EmeraldGreen, onClick = { applyPercentage(5.0) })
                QuickActionChip(label = "+12% GST", color = EmeraldGreen, onClick = { applyPercentage(12.0) })
                QuickActionChip(label = "+18% GST", color = EmeraldGreen, onClick = { applyPercentage(18.0) })
                QuickActionChip(label = "-10% Off", color = CardinalRed, onClick = { applyPercentage(-10.0) })
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5-Row Calculator Grid
            val keypad = listOf(
                listOf("C", "%", "÷", "⌫"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("00", "0", ".", "=")
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                keypad.forEach { rowKeys ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowKeys.forEach { key ->
                            val isDigit = key in listOf("0", "00", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".")
                            val isClear = key in listOf("C", "⌫")
                            val isEquals = key == "="

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            isEquals -> primaryColor
                                            isClear -> RedKeyBg
                                            isDigit -> NumberKeyBg
                                            else -> OperatorKeyBg
                                        }
                                    )
                                    .clickable { onKeyTapped(key) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (key == "⌫") {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Backspace",
                                        tint = RedKeyText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text(
                                        text = key,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            isEquals -> Color.White
                                            isClear -> RedKeyText
                                            isDigit -> Color.White
                                            else -> OperatorBlue
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Confirmation Button
            val confirmedAmount = currentAmountStr.toDoubleOrNull() ?: 0.0
            Button(
                onClick = {
                    if (confirmedAmount > 0.0) {
                        onConfirmAmount(confirmedAmount)
                        onDismiss()
                    }
                },
                enabled = confirmedAmount > 0.0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    disabledContainerColor = primaryColor.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "✓ Use This Amount in Entry (₹ $currentAmountStr) ✓",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun RowScope.QuickActionChip(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .weight(1f)
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatNumber(value: Double): String {
    return if (value % 1.0 == 0.0) {
        "%.0f".format(value)
    } else {
        DecimalFormat("#.##").format(value)
    }
}

private fun simpleEvaluate(expression: String): Double {
    val tokens = mutableListOf<String>()
    var current = StringBuilder()

    for (c in expression) {
        if (c in "+-*/") {
            if (current.isNotEmpty()) {
                tokens.add(current.toString())
                current = StringBuilder()
            }
            tokens.add(c.toString())
        } else {
            current.append(c)
        }
    }
    if (current.isNotEmpty()) tokens.add(current.toString())
    if (tokens.isEmpty()) return 0.0

    val higherPrecedence = mutableListOf<String>()
    var i = 0
    while (i < tokens.size) {
        val t = tokens[i]
        if (t == "*" || t == "/") {
            val left = higherPrecedence.removeAt(higherPrecedence.lastIndex).toDoubleOrNull() ?: 0.0
            val right = if (i + 1 < tokens.size) tokens[i + 1].toDoubleOrNull() ?: 1.0 else 1.0
            val res = if (t == "*") left * right else if (right != 0.0) left / right else 0.0
            higherPrecedence.add(res.toString())
            i += 2
        } else {
            higherPrecedence.add(t)
            i++
        }
    }

    var total = higherPrecedence.getOrNull(0)?.toDoubleOrNull() ?: 0.0
    var j = 1
    while (j < higherPrecedence.size) {
        val op = higherPrecedence[j]
        val next = higherPrecedence.getOrNull(j + 1)?.toDoubleOrNull() ?: 0.0
        total = if (op == "+") total + next else total - next
        j += 2
    }
    return total
}
