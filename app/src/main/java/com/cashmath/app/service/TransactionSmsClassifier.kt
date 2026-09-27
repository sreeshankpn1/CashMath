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
        "balance inquiry", "merchant payments"
    )

    private val debitVerbs = listOf(
        "debited by", "debited with", "spent on", "paid to", "transferred to", "withdrawn"
    )

    private val creditVerbs = listOf(
        "credited with", "credited by", "received from", "deposited"
    )

    private val referenceTokens = listOf(
        "a/c", "ending", "upi ref", "vpa", "ref no", "acc", "account", "a/c no"
    )

    private val amountPattern = Pattern.compile("""(?:INR|Rs\.?|₹)\s*([\d,]+(?:\.\d{1,2})?)""", Pattern.CASE_INSENSITIVE)

    fun classify(title: String, body: String): ClassifiedSmsTransaction? {
        val fullText = "$title $body"
        val lowerText = fullText.lowercase(Locale.ENGLISH)

        // 1. Discard promotional / marketing / spam messages
        if (promoKeywords.any { lowerText.contains(it) }) {
            return null
        }

        // 2. Check for required reference token
        val hasReference = referenceTokens.any { lowerText.contains(it) }
        if (!hasReference) {
            return null
        }

        // 3. Determine action verb & transaction direction
        val hasDebit = debitVerbs.any { lowerText.contains(it) }
        val hasCredit = creditVerbs.any { lowerText.contains(it) }

        if (!hasDebit && !hasCredit) {
            return null
        }

        val isDebit = hasDebit

        // 4. Extract Amount after INR / Rs. / ₹
        val matcher = amountPattern.matcher(fullText)
        if (!matcher.find()) {
            return null
        }

        val amountStr = matcher.group(1)?.replace(",", "") ?: return null
        val amount = amountStr.toDoubleOrNull() ?: return null
        if (amount <= 0.0) return null

        // 5. Extract Party or Merchant Name if available
        val partyName = extractPartyName(lowerText) ?: if (isDebit) "Vendor / Merchant" else "Payer"

        return ClassifiedSmsTransaction(
            amount = amount,
            isDebit = isDebit,
            partyOrSender = partyName,
            referenceToken = referenceTokens.firstOrNull { lowerText.contains(it) },
            rawText = body
        )
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
