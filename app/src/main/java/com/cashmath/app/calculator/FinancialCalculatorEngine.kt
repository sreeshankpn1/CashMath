package com.cashmath.app.calculator

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class FinancialCalculatorEngine {
    private val _display = MutableStateFlow("0")
    val display = _display.asStateFlow()

    private val _formula = MutableStateFlow("")
    val formula = _formula.asStateFlow()

    private val _history = MutableStateFlow<List<CalculationHistoryItem>>(emptyList())
    val history = _history.asStateFlow()

    private var currentVal = 0.0
    private var pendingOp = ""
    private var isNewEntry = true

    fun onDigit(digit: String) {
        if (isNewEntry || _display.value == "0") {
            _display.value = digit
            isNewEntry = false
        } else {
            _display.value += digit
        }
    }

    fun onDecimal() {
        if (!_display.value.contains(".")) {
            _display.value += "."
            isNewEntry = false
        }
    }

    fun onOperator(op: String) {
        val input = _display.value.toDoubleOrNull() ?: 0.0
        if (pendingOp.isNotEmpty() && !isNewEntry) {
            calculateResult()
        } else {
            currentVal = input
        }
        pendingOp = op
        _formula.value = "${formatNumber(currentVal)} $op"
        isNewEntry = true
    }

    fun onGst(percent: Double) {
        val base = _display.value.toDoubleOrNull() ?: 0.0
        val gstAmount = base * (percent / 100.0)
        val total = base + gstAmount
        _formula.value = "${formatNumber(base)} + $percent% GST"
        _display.value = formatNumber(total)
        recordHistory(_formula.value, total)
        isNewEntry = true
    }

    fun onDiscount(percent: Double) {
        val base = _display.value.toDoubleOrNull() ?: 0.0
        val discountAmount = base * (percent / 100.0)
        val total = base - discountAmount
        _formula.value = "${formatNumber(base)} - $percent% OFF"
        _display.value = formatNumber(total)
        recordHistory(_formula.value, total)
        isNewEntry = true
    }

    fun calculateResult() {
        val secondVal = _display.value.toDoubleOrNull() ?: 0.0
        val fullFormula = "${_formula.value} ${formatNumber(secondVal)}"
        var res = currentVal
        when (pendingOp) {
            "+" -> res += secondVal
            "-" -> res -= secondVal
            "×" -> res *= secondVal
            "÷" -> if (secondVal != 0.0) res /= secondVal
        }
        _display.value = formatNumber(res)
        _formula.value = ""
        recordHistory(fullFormula, res)
        pendingOp = ""
        currentVal = res
        isNewEntry = true
    }

    fun onClear() {
        _display.value = "0"
        _formula.value = ""
        currentVal = 0.0
        pendingOp = ""
        isNewEntry = true
    }

    fun onBackspace() {
        if (_display.value.length > 1) {
            _display.value = _display.value.dropLast(1)
        } else {
            _display.value = "0"
            isNewEntry = true
        }
    }

    fun loadFromHistory(item: CalculationHistoryItem) {
        _display.value = formatNumber(item.result)
        _formula.value = item.expression
        currentVal = item.result
        isNewEntry = true
    }

    private fun recordHistory(exp: String, res: Double) {
        if (exp.isNotBlank()) {
            _history.value = listOf(CalculationHistoryItem(expression = exp, result = res)) + _history.value.take(19)
        }
    }

    private fun formatNumber(num: Double): String {
        return if (num % 1.0 == 0.0) String.format(Locale.getDefault(), "%,.0f", num) else String.format(Locale.getDefault(), "%,.2f", num)
    }
}
