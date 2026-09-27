package com.cashmath.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NumberKeyBg = Color(0xFF192132)
private val OperatorKeyBg = Color(0xFF202A3E)
private val KeypadBorder = Color(0xFF222C42)
private val OperatorBlue = Color(0xFF5E9CFF)
private val RedKeyBg = Color(0xFF381F26)
private val RedKeyText = Color(0xFFFF6B6B)

@Composable
fun CashMathCalculatorKeypad(
    expression: String,
    onExpressionChange: (String) -> Unit,
    onEvaluated: (Double) -> Unit,
    accentColor: Color = Color(0xFF00E676),
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    fun compute(expr: String): Double {
        if (expr.isBlank()) return 0.0
        return try {
            val sanitized = expr.replace("×", "*").replace("÷", "/")

            // Handle Percentage (%) math: 5000 + 18% or 5000 - 10% or 5000 * 5%
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

    fun onKeyClick(key: String) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        when (key) {
            "C" -> {
                onExpressionChange("")
                onEvaluated(0.0)
            }
            "⌫" -> {
                if (expression.isNotEmpty()) {
                    val newExpr = expression.dropLast(1)
                    onExpressionChange(newExpr)
                    onEvaluated(compute(newExpr))
                }
            }
            "=" -> {
                val result = compute(expression)
                val resultStr = if (result % 1.0 == 0.0) "%.0f".format(result) else result.toString()
                onExpressionChange(resultStr)
                onEvaluated(result)
            }
            "%", "+", "-", "×", "÷" -> {
                if (expression.isNotEmpty() && !expression.last().toString().matches(Regex("""[+\-×÷%]"""))) {
                    val newExpr = expression + key
                    onExpressionChange(newExpr)
                    onEvaluated(compute(newExpr))
                }
            }
            else -> {
                val newExpr = expression + key
                onExpressionChange(newExpr)
                onEvaluated(compute(newExpr))
            }
        }
    }

    // 5-Row Calculator Keypad Grid
    val keypad = listOf(
        listOf("C", "%", "÷", "⌫"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("00", "0", ".", "=")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keypad.forEach { row ->
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
                            .height(50.dp)
                            .scale(scale)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when {
                                    isEquals -> accentColor
                                    isClear -> RedKeyBg
                                    isOperator -> OperatorKeyBg
                                    else -> NumberKeyBg
                                }
                            )
                            .border(1.dp, KeypadBorder, RoundedCornerShape(10.dp))
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
                                tint = RedKeyText,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = key,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isEquals -> Color.Black
                                    isClear -> RedKeyText
                                    isOperator -> OperatorBlue
                                    else -> Color.White
                                }
                            )
                        }
                    }
                }
            }
        }
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
