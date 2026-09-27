package com.cashmath.app.service

import com.cashmath.app.data.TransactionEntity
import com.cashmath.app.parser.DateTimeUtils
import java.io.File
import java.io.FileWriter

object CsvReportGenerator {
    fun generateCsv(
        transactions: List<TransactionEntity>,
        file: File,
        businessName: String = "CashMath Business",
        phone: String? = null,
        gstin: String? = null
    ): File {
        FileWriter(file).use { writer ->
            writer.append("Business Name: $businessName\n")
            if (!phone.isNullOrBlank() || !gstin.isNullOrBlank()) {
                writer.append("Phone: ${phone ?: "N/A"}  |  GSTIN: ${gstin ?: "N/A"}\n")
            }
            writer.append("ID,Date,Time,Merchant/Party,Category,Mode,Type,Amount (INR),Note\n")
            
            transactions.forEach { tx ->
                val dateTime = DateTimeUtils.formatTransactionTime(tx.transactionTimestamp)
                val date = dateTime.split(",")[0]
                val time = dateTime.split(",")[1].trim()
                
                writer.append("${tx.id},")
                writer.append("\"$date\",")
                writer.append("\"$time\",")
                writer.append("\"${tx.partyName ?: ""}\",")
                writer.append("\"${tx.category ?: ""}\",")
                writer.append("\"${tx.paymentMode}\",")
                writer.append("${tx.type.name},")
                writer.append("${tx.amount},")
                writer.append("\"${tx.deletionReason ?: ""}\"\n")
            }
        }
        return file
    }
}
