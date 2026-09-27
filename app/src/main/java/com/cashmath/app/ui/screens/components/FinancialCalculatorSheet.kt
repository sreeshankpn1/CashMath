package com.cashmath.app.ui.screens.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashmath.app.calculator.FinancialCalculatorEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialCalculatorSheet(
    onDismiss: () -> Unit,
    onApplyAmount: (amount: Double, formulaNote: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.PartiallyExpanded }
    )
    val engine = remember { FinancialCalculatorEngine() }
    val display by engine.display.collectAsState()
    val formula by engine.formula.collectAsState()
    val history by engine.history.collectAsState()
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0B101B),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // --- TOP: Formula & Display Display Window ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                border = BorderStroke(1.dp, Color(0xFF26334D))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formula.ifBlank { " " },
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "₹ $display",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- TAPE HISTORY CAROUSEL ---
            if (history.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(history) { item ->
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                engine.loadFromHistory(item)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${item.expression} = ₹${item.result}", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // --- COMMERCIAL GST & DISCOUNT CONVERSION STRIP ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(5.0 to "+5% GST", 12.0 to "+12% GST", 18.0 to "+18% GST").forEach { (rate, label) ->
                    AssistChip(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            engine.onGst(rate)
                        },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981)) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF064E3B).copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, Color(0xFF059669)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(32.dp)
                    )
                }
                AssistChip(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        engine.onDiscount(10.0)
                    },
                    label = { Text("-10% Off", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF87171)) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF450A0A).copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, Color(0xFFDC2626)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- CRED-STYLE KEYPAD TILES ---
            val keys = listOf(
                listOf("C", "÷", "×", "DEL"),
                listOf("7", "8", "9", "-"),
                listOf("4", "5", "6", "+"),
                listOf("1", "2", "3", "."),
                listOf("00", "0", "=")
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (row in keys) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (key in row) {
                            val weight = if (key == "=") 2f else 1f
                            CalculatorKey(
                                text = key,
                                modifier = Modifier.weight(weight),
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    when (key) {
                                        "C" -> engine.onClear()
                                        "DEL" -> engine.onBackspace()
                                        "+", "-", "×", "÷" -> engine.onOperator(key)
                                        "=" -> engine.calculateResult()
                                        "." -> engine.onDecimal()
                                        else -> engine.onDigit(key)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- BOTTOM PRIMARY ACTION: INSERT INTO AMOUNT ---
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val amount = display.replace(",", "").toDoubleOrNull() ?: 0.0
                    onApplyAmount(amount, formula)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Use This Amount in Entry (₹ $display) ✓", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
fun CalculatorKey(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isOperator = text in listOf("+", "-", "×", "÷", "=")
    val isClear = text in listOf("C", "DEL")

    val bg = when {
        text == "=" -> Color(0xFF10B981)
        isOperator -> Color(0xFF26334D)
        isClear -> Color(0xFF33202A)
        else -> Color(0xFF172033)
    }

    val fg = when {
        text == "=" -> Color.White
        isOperator -> Color(0xFF38BDF8)
        isClear -> Color(0xFFF87171)
        else -> Color.White
    }

    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, Color(0xFF2B3A55).copy(alpha = 0.6f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (text == "DEL") {
                Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
            } else {
                Text(text = text, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = fg)
            }
        }
    }
}
