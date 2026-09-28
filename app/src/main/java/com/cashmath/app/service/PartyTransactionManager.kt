package com.cashmath.app.service

import android.content.Context
import com.cashmath.app.data.DuePartyEntity
import com.cashmath.app.data.model.PartyTransactionEntity
import java.util.UUID
import kotlin.math.abs

object PartyTransactionManager {

    private const val PREFS_NAME = "cashmath_party_tx_prefs"

    fun getTransactions(context: Context, party: DuePartyEntity): List<PartyTransactionEntity> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString("party_tx_${party.id}", null)
        val list = if (!json.isNullOrBlank()) {
            parseJsonToTransactions(json)
        } else {
            emptyList()
        }

        // If no stored transactions exist but party has non-zero balance, seed initial opening balance entry
        if (list.isEmpty()) {
            val net = party.toReceive - party.toPay
            if (net != 0.0) {
                val seed = PartyTransactionEntity(
                    id = UUID.randomUUID().toString(),
                    partyId = party.id.toString(),
                    amount = abs(net),
                    isCredit = (party.toPay > 0),
                    date = party.updatedAt,
                    remarks = "Opening Balance",
                    paymentMode = "Cash"
                )
                val seededList = listOf(seed)
                saveTransactions(context, party.id, seededList)
                return seededList
            }
        }

        return list.sortedByDescending { it.date }
    }

    fun addTransaction(context: Context, partyId: Long, tx: PartyTransactionEntity) {
        val current = getTransactionsRaw(context, partyId).toMutableList()
        current.add(0, tx)
        saveTransactions(context, partyId, current)
    }

    fun clearTransactions(context: Context, partyId: Long) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove("party_tx_$partyId")
            .apply()
    }

    private fun getTransactionsRaw(context: Context, partyId: Long): List<PartyTransactionEntity> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString("party_tx_$partyId", null) ?: return emptyList()
        return parseJsonToTransactions(json)
    }

    private fun saveTransactions(context: Context, partyId: Long, list: List<PartyTransactionEntity>) {
        val json = serializeTransactionsToJson(list)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("party_tx_$partyId", json)
            .apply()
    }

    private fun serializeTransactionsToJson(list: List<PartyTransactionEntity>): String {
        val sb = StringBuilder("[")
        list.forEachIndexed { index, tx ->
            val escapedRemarks = tx.remarks.replace("\"", "\\\"").replace("\n", " ")
            val escapedMode = tx.paymentMode.replace("\"", "\\\"")

            sb.append("{")
                .append("\"id\":\"").append(tx.id).append("\",")
                .append("\"partyId\":\"").append(tx.partyId).append("\",")
                .append("\"amount\":").append(tx.amount).append(",")
                .append("\"isCredit\":").append(tx.isCredit).append(",")
                .append("\"date\":").append(tx.date).append(",")
                .append("\"remarks\":\"").append(escapedRemarks).append("\",")
                .append("\"paymentMode\":\"").append(escapedMode).append("\"")
                .append("}")
            if (index < list.size - 1) sb.append(",")
        }
        sb.append("]")
        return sb.toString()
    }

    private fun parseJsonToTransactions(json: String): List<PartyTransactionEntity> {
        val list = mutableListOf<PartyTransactionEntity>()
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
                val partyId = map["partyId"] ?: ""
                val amount = map["amount"]?.toDoubleOrNull() ?: 0.0
                val isCredit = map["isCredit"]?.toBoolean() ?: false
                val date = map["date"]?.toLongOrNull() ?: System.currentTimeMillis()
                val remarks = map["remarks"] ?: ""
                val mode = map["paymentMode"] ?: "Cash"

                if (amount >= 0.0) {
                    list.add(
                        PartyTransactionEntity(
                            id = id,
                            partyId = partyId,
                            amount = amount,
                            isCredit = isCredit,
                            date = date,
                            remarks = remarks,
                            paymentMode = mode
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
