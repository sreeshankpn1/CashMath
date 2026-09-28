package com.cashmath.app.service

import java.util.Locale
import java.util.regex.Pattern

data class ClassifiedSmsTransaction(
    val amount: Double,
    val isDebit: Boolean,
    val partyOrSender: String,
    val referenceToken: String?,
    val rawText: String
)

object TransactionSmsClassifier {

    private val promoKeywords = listOf(
        "cashback", "cash back", "offer", "coupon", "reward", "congratulations",
        "grab", "eligible", "apply now", "win", "voucher", "discount", "bonus",
        "loan approved", "pre-approved", "credit card offer", "otp", "statement for",
        "balance inquiry"
    )

    // Regex for standard UPI & Bank Debit transactions
    private val upiDebitPattern = Pattern.compile(
        """(?:Paid|Sent|transferred|debited)\s+(?:₹|Rs\.?|INR)?\s*([\d,]+(?:\.\d{1,2})?)\s+(?:to|for)\s+([A-Za-z0-9\s.]+?)(?:\.|\s+using|\s+from|$)""",
        Pattern.CASE_INSENSITIVE
    )

    // Regex for standard UPI & Bank Credit transactions
    private val upiCreditPattern = Pattern.compile(
        """(?:Received|credited)\s+(?:₹|Rs\.?|INR)?\s*([\d,]+(?:\.\d{1,2})?)\s+(?:from|by)\s+([A-Za-z0-9\s.]+?)(?:\.|\s+using|\s+in|$)""",
        Pattern.CASE_INSENSITIVE
    )

    private val fallbackAmountPattern = Pattern.compile("""(?:INR|Rs\.?|₹)\s*([\d,]+(?:\.\d{1,2})?)""", Pattern.CASE_INSENSITIVE)

    fun classify(title: String, body: String): ClassifiedSmsTransaction? {
        val fullText = "$title $body"
        val lowerText = fullText.lowercase(Locale.ENGLISH)

        // 1. Discard promotional / marketing / spam messages
        if (promoKeywords.any { lowerText.contains(it) }) {
            return null
        }

        // 2. Try structured UPI Debit pattern match
        val debitMatcher = upiDebitPattern.matcher(fullText)
        if (debitMatcher.find()) {
            val amountStr = debitMatcher.group(1)?.replace(",", "") ?: ""
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            val recipient = debitMatcher.group(2)?.trim()?.ifBlank { null } ?: "Vendor / Merchant"
            if (amount > 0.0) {
                return ClassifiedSmsTransaction(
                    amount = amount,
                    isDebit = true,
                    partyOrSender = recipient,
                    referenceToken = "UPI",
                    rawText = body
                )
            }
        }

        // 3. Try structured UPI Credit pattern match
        val creditMatcher = upiCreditPattern.matcher(fullText)
        if (creditMatcher.find()) {
            val amountStr = creditMatcher.group(1)?.replace(",", "") ?: ""
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            val sender = creditMatcher.group(2)?.trim()?.ifBlank { null } ?: "Payer"
            if (amount > 0.0) {
                return ClassifiedSmsTransaction(
                    amount = amount,
                    isDebit = false,
                    partyOrSender = sender,
                    referenceToken = "UPI",
                    rawText = body
                )
            }
        }

        // 4. Fallback Keyword & Amount Classification
        val hasDebit = listOf("debited", "paid", "sent", "spent", "withdrawn").any { lowerText.contains(it) }
        val hasCredit = listOf("credited", "received", "deposited").any { lowerText.contains(it) }

        if (!hasDebit && !hasCredit) {
            return null
        }

        val fallbackMatcher = fallbackAmountPattern.matcher(fullText)
        if (fallbackMatcher.find()) {
            val amountStr = fallbackMatcher.group(1)?.replace(",", "") ?: ""
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            if (amount > 0.0) {
                val partyName = extractPartyName(lowerText) ?: if (hasDebit) "Vendor / Merchant" else "Payer"
                return ClassifiedSmsTransaction(
                    amount = amount,
                    isDebit = hasDebit,
                    partyOrSender = partyName,
                    referenceToken = "Bank SMS",
                    rawText = body
                )
            }
        }

        return null
    }

    private fun extractPartyName(lowerText: String): String? {
        val patterns = listOf(
            Regex("""paid to\s+([a-zA-Z0-9\s]+?)(?:\s+on|\s+ref|\s+a/c|\.|$)"""),
            Regex("""received from\s+([a-zA-Z0-9\s]+?)(?:\s+on|\s+ref|\s+a/c|\.|$)"""),
            Regex("""spent on\s+([a-zA-Z0-9\s]+?)(?:\s+on|\s+ref|\s+a/c|\.|$)""")
        )

        for (pattern in patterns) {
            val match = pattern.find(lowerText)
            if (match != null) {
                val candidate = match.groupValues[1].trim()
                if (candidate.length in 2..30) {
                    return candidate.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                }
            }
        }
        return null
    }
}
