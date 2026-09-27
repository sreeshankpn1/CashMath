package com.cashmath.app.utils

import androidx.compose.ui.graphics.Color
import com.cashmath.app.data.EmiLoanEntity
import java.util.Calendar

enum class EmiStatusType { PAID, DUE_TODAY, OVERDUE, UPCOMING }

data class LoanEmiStatus(
    val type: EmiStatusType,
    val label: String,
    val containerColorLight: Color,
    val textColorLight: Color,
    val containerColorDark: Color,
    val textColorDark: Color
)

object LoanStatusHelper {

    fun getStatus(loan: EmiLoanEntity): LoanEmiStatus {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        val totalMonths = if (loan.totalTenureMonths > 0) loan.totalTenureMonths else 1
        if (loan.paidMonths >= totalMonths) {
            return LoanEmiStatus(
                type = EmiStatusType.PAID,
                label = "Fully Paid ✓",
                containerColorLight = Color(0xFFECFDF5),
                textColorLight = Color(0xFF047857),
                containerColorDark = Color(0xFF064E3B).copy(alpha = 0.6f),
                textColorDark = Color(0xFF34D399)
            )
        }

        val dueDay = loan.dueDayOfMonth.coerceIn(1, 31)

        return when {
            currentDay == dueDay -> LoanEmiStatus(
                type = EmiStatusType.DUE_TODAY,
                label = "⏰ Due Today",
                containerColorLight = Color(0xFFFEF3C7),
                textColorLight = Color(0xFFB45309),
                containerColorDark = Color(0xFF78350F).copy(alpha = 0.6f),
                textColorDark = Color(0xFFFBBF24)
            )
            currentDay > dueDay -> {
                val overdueDays = currentDay - dueDay
                LoanEmiStatus(
                    type = EmiStatusType.OVERDUE,
                    label = "⚠️ Overdue ($overdueDays days)",
                    containerColorLight = Color(0xFFFEE2E2),
                    textColorLight = Color(0xFFB91C1C),
                    containerColorDark = Color(0xFF7F1D1D).copy(alpha = 0.6f),
                    textColorDark = Color(0xFFF87171)
                )
            }
            else -> {
                LoanEmiStatus(
                    type = EmiStatusType.UPCOMING,
                    label = "Due on ${dueDay}th",
                    containerColorLight = Color(0xFFF1F5F9),
                    textColorLight = Color(0xFF475569),
                    containerColorDark = Color(0xFF1E293B),
                    textColorDark = Color(0xFF94A3B8)
                )
            }
        }
    }
}
