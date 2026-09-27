package com.cashmath.app.utils

data class ParsedVoiceEntry(
    val amount: Double,
    val isCashIn: Boolean,
    val partyOrNote: String,
    val rawText: String
)

object VoiceEntryParser {
    private val cashInKeywords = listOf(
        "received", "got", "in", "cash in", "credit", "credited", "from", 
        "kitti", "kiti", "aaya", "jama"
    )
    private val cashOutKeywords = listOf(
        "paid", "spent", "out", "cash out", "debit", "debited", "to", "gave", 
        "koduthu", "gaya", "kharch"
    )

    fun parse(spokenText: String): ParsedVoiceEntry? {
        val lower = spokenText.lowercase().trim()
        if (lower.isBlank()) return null

        // 1. Extract Numeric Amount (supports "5000", "5,000", "500.50")
        val amountRegex = Regex("""(?:rs\.?|inr|₹)?\s*([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
        val match = amountRegex.findAll(lower).mapNotNull {
            val clean = it.groupValues[1].replace(",", "")
            clean.toDoubleOrNull()
        }.firstOrNull { it > 0.0 } ?: return null

        // 2. Determine Cash In vs Cash Out
        val hasIn = cashInKeywords.any { lower.contains(it) }
        val hasOut = cashOutKeywords.any { lower.contains(it) }

        val isCashIn = when {
            hasIn && !hasOut -> true
            hasOut && !hasIn -> false
            lower.contains("from") -> true
            lower.contains("to") -> false
            else -> true
        }

        // 3. Clean up the party / remarks note
        var cleanedNote = lower
            .replace(match.toInt().toString(), "")
            .replace(match.toString(), "")
            .replace("₹", "")
            .replace("rupees", "")
            .replace("rs", "")

        (cashInKeywords + cashOutKeywords).forEach { keyword ->
            cleanedNote = cleanedNote.replace(Regex("\\b$keyword\\b"), "")
        }

        val finalNote = cleanedNote.replace(Regex("\\s+"), " ").trim().replaceFirstChar { it.uppercase() }

        return ParsedVoiceEntry(
            amount = match,
            isCashIn = isCashIn,
            partyOrNote = finalNote.ifBlank { "Voice Entry" },
            rawText = spokenText
        )
    }
}
