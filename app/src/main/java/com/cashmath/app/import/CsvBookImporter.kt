package com.cashmath.app.import

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.cashmath.app.data.TransactionEntity
import com.cashmath.app.data.TransactionType
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.abs

data class ImportPreviewData(
    val suggestedBookName: String,
    val totalCashIn: Double,
    val totalCashOut: Double,
    val netBalance: Double,
    val transactions: List<TransactionEntity>
)

object CsvBookImporter {

    fun parseFileForPreview(
        context: Context,
        uri: Uri,
        businessId: Long = 1L
    ): ImportPreviewData? {
        val fileName = getFileName(context, uri).substringBeforeLast(".")
        val transactions = mutableListOf<TransactionEntity>()
        var totalIn = 0.0
        var totalOut = 0.0

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                val lines = reader.readLines()
                if (lines.isEmpty()) return null

                // Detect delimiter (, or ; or tab)
                val firstLine = lines.first()
                val delimiter = when {
                    firstLine.contains("\t") -> "\t"
                    firstLine.contains(";") -> ";"
                    else -> ","
                }

                val headers = firstLine.split(delimiter).map { it.trim().lowercase() }
                val descIdx = headers.indexOfFirst { it.contains("desc") || it.contains("particular") || it.contains("name") || it.contains("party") || it.contains("note") }
                val inIdx = headers.indexOfFirst { it.contains("in") || it.contains("credit") || it.contains("deposit") || it.contains("received") }
                val outIdx = headers.indexOfFirst { it.contains("out") || it.contains("debit") || it.contains("withdrawal") || it.contains("paid") }

                for (i in 1 until lines.size) {
                    val line = lines[i].trim()
                    if (line.isBlank()) continue
                    val cols = line.split(delimiter).map { it.trim().replace("\"", "").replace("₹", "").replace(",", "") }

                    val desc = if (descIdx in cols.indices) cols[descIdx] else "Entry $i"
                    val cashIn = if (inIdx in cols.indices) cols[inIdx].toDoubleOrNull() ?: 0.0 else 0.0
                    val cashOut = if (outIdx in cols.indices) cols[outIdx].toDoubleOrNull() ?: 0.0 else 0.0

                    if (cashIn > 0.0) {
                        totalIn += cashIn
                        transactions.add(
                            TransactionEntity(
                                bookId = 0L, // Assigned after book creation
                                amount = cashIn,
                                type = TransactionType.INCOME,
                                partyName = desc,
                                remark = "Imported from $fileName",
                                transactionTimestamp = System.currentTimeMillis()
                            )
                        )
                    } else if (cashOut > 0.0) {
                        totalOut += cashOut
                        transactions.add(
                            TransactionEntity(
                                bookId = 0L,
                                amount = cashOut,
                                type = TransactionType.EXPENSE,
                                partyName = desc,
                                remark = "Imported from $fileName",
                                transactionTimestamp = System.currentTimeMillis()
                            )
                        )
                    } else {
                        val amtIdx = headers.indexOfFirst { it.contains("amount") || it.contains("amt") }
                        val amt = if (amtIdx in cols.indices) cols[amtIdx].toDoubleOrNull() ?: 0.0 else 0.0
                        if (amt != 0.0) {
                            val isIncome = amt > 0
                            val absAmt = abs(amt)
                            if (isIncome) totalIn += absAmt else totalOut += absAmt
                            transactions.add(
                                TransactionEntity(
                                    bookId = 0L,
                                    amount = absAmt,
                                    type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
                                    partyName = desc,
                                    remark = "Imported from $fileName",
                                    transactionTimestamp = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {
            return null
        }

        if (transactions.isEmpty()) return null

        return ImportPreviewData(
            suggestedBookName = fileName.ifBlank { "Imported Ledger" },
            totalCashIn = totalIn,
            totalCashOut = totalOut,
            netBalance = totalIn - totalOut,
            transactions = transactions
        )
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var name = "Imported_Ledger"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }
}
