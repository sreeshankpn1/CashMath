package com.cashmath.app.parser

data class ParsedTransaction(
    val amount: Double,
    val isDebit: Boolean, // true = Cash Out, false = Cash In
    val payeeOrSender: String,
    val reference: String?,
    val rawText: String,
    val appName: String
)

object UpiTransactionParser {
    // Regex for Indian Currency (₹, Rs., INR)
    private val amountRegex = Regex("""(?:Rs\.?|INR|₹)\s*([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
    private val debitKeywords = listOf("debited", "paid to", "sent to", "transferred to", "spent", "withdrawn", "payment of")
    private val creditKeywords = listOf("credited", "received from", "deposited", "added", "received")

    fun parse(packageName: String, title: String, text: String): ParsedTransaction? {
        val fullText = "$title $text"
        val amountMatch = amountRegex.find(fullText) ?: return null
        val cleanAmountStr = amountMatch.groupValues[1].replace(",", "")
        val amount = cleanAmountStr.toDoubleOrNull() ?: return null

        val isDebit = debitKeywords.any { fullText.contains(it, ignoreCase = true) }
        val isCredit = creditKeywords.any { fullText.contains(it, ignoreCase = true) }

        if (!isDebit && !isCredit) return null

        // Extract merchant/recipient name
        val payeeRegex = Regex("""(?:paid to|sent to|to|from)\s+([A-Za-z0-9\s&._-]+?)(?:\s+on|\s+using|\s+ref|\s+upi|\.|\$|$)""", RegexOption.IGNORE_CASE)
        val payee = payeeRegex.find(fullText)?.groupValues?.get(1)?.trim() ?: title.ifBlank { "Direct UPI" }

        val appName = when {
            packageName.contains("phonepe") -> "PhonePe"
            packageName.contains("nbu.paisa") -> "Google Pay"
            packageName.contains("paytm") -> "Paytm"
            packageName.contains("upiapp") -> "BHIM"
            else -> "Bank SMS"
        }

        return ParsedTransaction(
            amount = amount,
            isDebit = isDebit,
            payeeOrSender = payee,
            reference = null,
            rawText = fullText,
            appName = appName
        )
    }
}
