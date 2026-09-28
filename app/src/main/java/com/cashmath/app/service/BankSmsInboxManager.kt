package com.cashmath.app.service

import android.content.Context
import com.cashmath.app.data.EmiLoanEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.math.abs

data class BankSmsItem(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val isCashIn: Boolean,
    val bankName: String,
    val snippet: String,
    val timestamp: Long,
    val suggestedLoanId: Long? = null,
    val suggestedLoanTitle: String? = null
)

object BankSmsInboxManager {

    private const val PREFS_NAME = "cashmath_sms_inbox_prefs"
    private const val KEY_PENDING_SMS_JSON = "key_pending_sms_json"

    private val _pendingSmsItems = MutableStateFlow<List<BankSmsItem>>(emptyList())
    val pendingSmsItems: StateFlow<List<BankSmsItem>> = _pendingSmsItems.asStateFlow()

    fun loadPendingItems(context: Context): List<BankSmsItem> {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = prefs.getString(KEY_PENDING_SMS_JSON, null) ?: return emptyList()
            parseJsonToItems(json)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun syncPendingItems(context: Context) {
        val loaded = loadPendingItems(context)
        _pendingSmsItems.value = loaded
    }

    fun savePendingItems(context: Context, items: List<BankSmsItem>) {
        try {
            val json = serializeItemsToJson(items)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PENDING_SMS_JSON, json)
                .apply()
            _pendingSmsItems.value = items
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addPendingItem(context: Context, item: BankSmsItem) {
        val current = loadPendingItems(context).toMutableList()
        // Deduplicate
        val isDuplicate = current.any {
            it.bankName.equals(item.bankName, ignoreCase = true) &&
                    abs(it.amount - item.amount) < 0.01 &&
                    abs(it.timestamp - item.timestamp) < 60000L
        }
        if (!isDuplicate) {
            current.add(0, item)
            savePendingItems(context, current)
        }
    }

    fun removeItem(context: Context, itemId: String) {
        val current = loadPendingItems(context).filter { it.id != itemId }
        savePendingItems(context, current)
    }

    fun clearAll(context: Context) {
        savePendingItems(context, emptyList())
    }

    @Deprecated("READ_SMS background scanning is disabled to comply with Google Play policies")
    fun scanRecent7DaysSms(context: Context, @Suppress("UNUSED_PARAMETER") loans: List<EmiLoanEntity> = emptyList()): List<BankSmsItem> {
        return loadPendingItems(context)
    }

    private fun serializeItemsToJson(items: List<BankSmsItem>): String {
        val sb = StringBuilder("[")
        items.forEachIndexed { index, item ->
            val escapedBank = item.bankName.replace("\"", "\\\"")
            val escapedSnippet = item.snippet.replace("\"", "\\\"").replace("\n", " ")
            val escapedLoanTitle = item.suggestedLoanTitle?.replace("\"", "\\\"") ?: ""

            sb.append("{")
                .append("\"id\":\"").append(item.id).append("\",")
                .append("\"amount\":").append(item.amount).append(",")
                .append("\"isCashIn\":").append(item.isCashIn).append(",")
                .append("\"bankName\":\"").append(escapedBank).append("\",")
                .append("\"snippet\":\"").append(escapedSnippet).append("\",")
                .append("\"timestamp\":").append(item.timestamp).append(",")
                .append("\"suggestedLoanId\":").append(item.suggestedLoanId ?: "null").append(",")
                .append("\"suggestedLoanTitle\":\"").append(escapedLoanTitle).append("\"")
                .append("}")
            if (index < items.size - 1) sb.append(",")
        }
        sb.append("]")
        return sb.toString()
    }

    private fun parseJsonToItems(json: String): List<BankSmsItem> {
        val list = mutableListOf<BankSmsItem>()
        if (json.isBlank() || json == "[]") return list

        try {
            val clean = json.trim().removePrefix("[").removeSuffix("]")
            if (clean.isBlank()) return list

            val objects = clean.split("},{")
            for (rawObj in objects) {
                val obj = rawObj.removePrefix("{").removeSuffix("}")
                val pairs = obj.split(",")
                val map = mutableMapOf<String, String>()
                for (p in pairs) {
                    val kv = p.split("\":")
                    if (kv.size == 2) {
                        val key = kv[0].replace("\"", "").trim()
                        val value = kv[1].replace("\"", "").trim()
                        map[key] = value
                    }
                }

                val id = map["id"] ?: UUID.randomUUID().toString()
                val amount = map["amount"]?.toDoubleOrNull() ?: 0.0
                val isCashIn = map["isCashIn"]?.toBoolean() ?: false
                val bankName = map["bankName"] ?: "Bank"
                val snippet = map["snippet"] ?: ""
                val timestamp = map["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
                val loanIdStr = map["suggestedLoanId"]
                val suggestedLoanId = if (loanIdStr != null && loanIdStr != "null") loanIdStr.toLongOrNull() else null
                val suggestedLoanTitle = map["suggestedLoanTitle"]?.ifBlank { null }

                if (amount > 0.0) {
                    list.add(
                        BankSmsItem(
                            id = id,
                            amount = amount,
                            isCashIn = isCashIn,
                            bankName = bankName,
                            snippet = snippet,
                            timestamp = timestamp,
                            suggestedLoanId = suggestedLoanId,
                            suggestedLoanTitle = suggestedLoanTitle
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return list
    }
}
