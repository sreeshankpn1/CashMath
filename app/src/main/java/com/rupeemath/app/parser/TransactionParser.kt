package com.rupeemath.app.parser

import com.rupeemath.app.data.TransactionEntity
import com.rupeemath.app.data.TransactionType
import java.util.regex.Pattern

object TransactionParser {

    // Regex for Indian Currency amounts: ₹500, Rs. 500, INR 500.50
    private val amountRegex = Pattern.compile("(?i)(?:(?:rs\\.?|inr|₹)\\s*([0-9,]+(?:\\.[0-9]{1,2})?))")

    private val expenseKeywords = listOf("paid", "debited", "sent to", "spent", "transfer to")
    private val incomeKeywords = listOf("received", "credited", "added to", "cashback")

    fun parse(packageName: String, title: String, text: String): TransactionEntity? {
        val combined = "$title $text"
        val lower = combined.lowercase()

        // 1. Extract Amount
        val matcher = amountRegex.matcher(combined)
        val amount = if (matcher.find()) {
            matcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: return null
        } else {
            return null
        }

        // 2. Identify Debit vs Credit
        val type = when {
            expenseKeywords.any { lower.contains(it) } -> TransactionType.EXPENSE
            incomeKeywords.any { lower.contains(it) } -> TransactionType.INCOME
            else -> return null
        }

        // 3. Identify Source App
        val source = when {
            packageName.contains("phonepe") -> "PhonePe"
            packageName.contains("nbu.paisa") -> "Google Pay"
            packageName.contains("paytm") -> "Paytm"
            packageName.contains("upiapp") -> "BHIM"
            else -> "Bank SMS"
        }

        // 4. Clean Merchant / Payee name
        val merchant = title.trim().ifBlank { "Direct UPI" }

        return TransactionEntity(
            amount = amount,
            type = type,
            merchantOrPayee = merchant,
            sourceApp = source,
            rawText = text
        )
    }
}
