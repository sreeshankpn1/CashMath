package com.cashmath.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat

@Composable
fun CashMathEntryKeypad(
    expression: String,
    onExpressionChange: (String) -> Unit,
    onEvaluated: (Double) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF10B981)
) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var isCalculatorExpanded by remember { mutableStateOf(false) }

    val keypadBg = if (isDark) Color(0xFF131927) else Color(0xFFFFFFFF)
    val numberKeyBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val keyBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val keyText = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)

    val operatorKeyBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val operatorText = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)

    val redClearBg = if (isDark) Color(0xFF4C0519).copy(alpha = 0.4f) else Color(0xFFFFF1F2)
    val redClearText = if (isDark) Color(0xFFFB7185) else Color(0xFFE11D48)

    val chipBg = if (isDark) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFFECFDF5)
    val chipBorder = if (isDark) Color(0xFF059669) else Color(0xFFA7F3D0)
    val chipText = if (isDark) Color(0xFF34D399) else Color(0xFF059669)

    fun compute(expr: String): Double {
        if (expr.isBlank()) return 0.0
        return try {
            val sanitized = expr.replace("×", "*").replace("÷", "/")

            // Percentage (%) math: 10000 + 18% or 5000 - 10%
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

    fun applyPercentage(percent: Double) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val base = compute(expression)
        if (base > 0) {
            val delta = base * (percent / 100.0)
            val updated = base + delta
            val formattedStr = if (updated % 1.0 == 0.0) "%.0f".format(updated) else DecimalFormat("#.##").format(updated)
            onExpressionChange(formattedStr)
            onEvaluated(updated)
        }
    }

    fun handleBackspace(current: String): String {
        val clean = current.replace("₹", "").replace(",", "").trim()
        return if (clean.length > 1) {
            clean.dropLast(1)
        } else {
            "0"
        }
    }

    fun onKeyClick(key: String) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        when (key) {
            "C" -> {
                onExpressionChange("0")
                onEvaluated(0.0)
            }
            "⌫" -> {
                val newExpr = handleBackspace(expression)
                onExpressionChange(newExpr)
                onEvaluated(compute(newExpr))
            }
            "=" -> {
                val result = compute(expression)
                val resultStr = if (result % 1.0 == 0.0) "%.0f".format(result) else DecimalFormat("#.##").format(result)
                onExpressionChange(resultStr)
                onEvaluated(result)
            }
            "%", "+", "-", "×", "÷" -> {
                if (!isCalculatorExpanded) isCalculatorExpanded = true
                if (expression.isNotBlank() && expression != "0" && !expression.last().toString().matches(Regex("""[+\-×÷%]"""))) {
                    val newExpr = expression + key
                    onExpressionChange(newExpr)
                    onEvaluated(compute(newExpr))
                }
            }
            else -> {
                val newExpr = if (expression == "0" || expression.isBlank()) key else expression + key
                onExpressionChange(newExpr)
                onEvaluated(compute(newExpr))
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(keypadBg)
            .border(1.dp, keyBorder, RoundedCornerShape(16.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Toggle Row for GST Chips & Mode Expansion
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                GstQuickChip(label = "+5% GST", chipBg = chipBg, chipBorder = chipBorder, chipText = chipText, onClick = { applyPercentage(5.0) })
                GstQuickChip(label = "+12% GST", chipBg = chipBg, chipBorder = chipBorder, chipText = chipText, onClick = { applyPercentage(12.0) })
                GstQuickChip(label = "+18% GST", chipBg = chipBg, chipBorder = chipBorder, chipText = chipText, onClick = { applyPercentage(18.0) })
                GstQuickChip(
                    label = "-10% Off",
                    chipBg = if (isDark) Color(0xFF881337).copy(alpha = 0.4f) else Color(0xFFFFE4E6),
                    chipBorder = if (isDark) Color(0xFFBE123C) else Color(0xFFFECDD3),
                    chipText = if (isDark) Color(0xFFFB7185) else Color(0xFFE11D48),
                    onClick = { applyPercentage(-10.0) }
                )
            }

            Box(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isCalculatorExpanded) accentColor.copy(alpha = 0.15f) else numberKeyBg)
                    .border(1.dp, if (isCalculatorExpanded) accentColor else keyBorder, CircleShape)
                    .clickable { isCalculatorExpanded = !isCalculatorExpanded },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = "Toggle Calculator",
                    tint = if (isCalculatorExpanded) accentColor else keyText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Keypad Grid
        val keypadRows = if (isCalculatorExpanded) {
            listOf(
                listOf("C", "%", "÷", "⌫"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("00", "0", ".", "=")
            )
        } else {
            listOf(
                listOf("7", "8", "9", "÷"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("00", "0", "⌫", "=")
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            keypadRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { key ->
                        val isEquals = key == "="
                        val isClear = key in listOf("C", "⌫")
                        val isOperator = key in listOf("%", "÷", "×", "-", "+")

                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()

                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.92f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "keyScale"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .scale(scale)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        isEquals -> accentColor
                                        isClear -> redClearBg
                                        isOperator -> operatorKeyBg
                                        else -> numberKeyBg
                                    }
                                )
                                .border(1.dp, keyBorder, RoundedCornerShape(10.dp))
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) { onKeyClick(key) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (key == "⌫") {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    tint = redClearText,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = key,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isEquals -> Color.White
                                        isClear -> redClearText
                                        isOperator -> operatorText
                                        else -> keyText
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.GstQuickChip(
    label: String,
    chipBg: Color,
    chipBorder: Color,
    chipText: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(chipBg)
            .border(1.dp, chipBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = chipText,
            textAlign = TextAlign.Center
        )
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
