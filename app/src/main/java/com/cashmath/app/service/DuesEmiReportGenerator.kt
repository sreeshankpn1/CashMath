package com.cashmath.app.service

import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.cashmath.app.data.DuePartyEntity
import com.cashmath.app.data.EmiLoanEntity
import com.cashmath.app.ui.DuesEmiUiState
import com.cashmath.app.ui.screens.formatIndianCurrency
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DuesEmiReportGenerator {

    fun generatePdf(
        businessName: String,
        dueParties: List<DuePartyEntity>,
        emiLoans: List<EmiLoanEntity>,
        uiState: DuesEmiUiState,
        file: File
    ): File {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            isFakeBoldText = true
        }
        val headerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
        }
        val sectionPaint = Paint().apply {
            color = Color.rgb(10, 37, 64)
            textSize = 13f
            isFakeBoldText = true
        }
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
        }
        val tableHeaderPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            isFakeBoldText = true
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var y = 40f

        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        // Header
        canvas.drawText("CashMath Consolidated Dues & EMI Statement", 40f, y, titlePaint)
        y += 22f
        canvas.drawText("Business: $businessName", 40f, y, headerPaint)
        y += 15f
        canvas.drawText("Generated on: $currentDate", 40f, y, headerPaint)
        y += 25f

        // Summary Box
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRect(40f, y, 555f, y + 65f, paint)
        y += 18f
        canvas.drawText("Total Dues Receivable: ${formatIndianCurrency(uiState.totalToReceive)}", 55f, y, tableHeaderPaint)
        canvas.drawText("Total Dues Payable: ${formatIndianCurrency(uiState.totalToPay)}", 300f, y, tableHeaderPaint)
        y += 18f
        canvas.drawText("Net Dues Balance: ${formatIndianCurrency(uiState.netDues)}", 55f, y, tableHeaderPaint)
        y += 18f
        canvas.drawText("Monthly EMI Commitment: ${formatIndianCurrency(uiState.activeMonthlyEmiTotal)}", 55f, y, tableHeaderPaint)
        canvas.drawText("Total Outstanding Loans: ${formatIndianCurrency(uiState.totalOutstandingEmi)}", 300f, y, tableHeaderPaint)
        y += 35f

        // Section 1: Customers To Receive
        canvas.drawText("1. Customers / Parties (You'll Get)", 40f, y, sectionPaint)
        y += 18f
        canvas.drawText("Name", 40f, y, tableHeaderPaint)
        canvas.drawText("Phone", 220f, y, tableHeaderPaint)
        canvas.drawText("Amount Receivable", 420f, y, tableHeaderPaint)
        y += 8f
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 18f

        val receivingParties = dueParties.filter { it.toReceive > 0 }
        if (receivingParties.isEmpty()) {
            canvas.drawText("No pending receivables.", 40f, y, textPaint)
            y += 20f
        } else {
            receivingParties.forEach { party ->
                if (y > 780f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = 40f
                }
                canvas.drawText(party.name, 40f, y, textPaint)
                canvas.drawText(party.phone ?: "-", 220f, y, textPaint)
                canvas.drawText(formatIndianCurrency(party.toReceive), 420f, y, textPaint)
                y += 18f
            }
        }
        y += 15f

        // Section 2: Suppliers To Pay
        if (y > 720f) {
            pdfDocument.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            y = 40f
        }

        canvas.drawText("2. Suppliers / Parties (You'll Pay)", 40f, y, sectionPaint)
        y += 18f
        canvas.drawText("Name", 40f, y, tableHeaderPaint)
        canvas.drawText("Phone", 220f, y, tableHeaderPaint)
        canvas.drawText("Amount Payable", 420f, y, tableHeaderPaint)
        y += 8f
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 18f

        val payingParties = dueParties.filter { it.toPay > 0 }
        if (payingParties.isEmpty()) {
            canvas.drawText("No pending payables.", 40f, y, textPaint)
            y += 20f
        } else {
            payingParties.forEach { party ->
                if (y > 780f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = 40f
                }
                canvas.drawText(party.name, 40f, y, textPaint)
                canvas.drawText(party.phone ?: "-", 220f, y, textPaint)
                canvas.drawText(formatIndianCurrency(party.toPay), 420f, y, textPaint)
                y += 18f
            }
        }
        y += 15f

        // Section 3: Active Loans & EMIs
        if (y > 700f) {
            pdfDocument.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            y = 40f
        }

        canvas.drawText("3. Active Loans & EMI Tracker", 40f, y, sectionPaint)
        y += 18f
        canvas.drawText("Loan / Bank", 40f, y, tableHeaderPaint)
        canvas.drawText("Monthly EMI", 220f, y, tableHeaderPaint)
        canvas.drawText("Tenure", 340f, y, tableHeaderPaint)
        canvas.drawText("Remaining Balance", 430f, y, tableHeaderPaint)
        y += 8f
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 18f

        if (emiLoans.isEmpty()) {
            canvas.drawText("No active loans recorded.", 40f, y, textPaint)
        } else {
            emiLoans.forEach { loan ->
                if (y > 780f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = 40f
                }
                val totalMonths = if (loan.totalTenureMonths > 0) loan.totalTenureMonths else if (loan.monthlyEmi > 0) ((loan.monthlyEmi * loan.totalTenureMonths) / loan.monthlyEmi).toInt() else 0
                val paidMonths = loan.paidMonths.coerceAtLeast(0)
                val remainingMonths = (totalMonths - paidMonths).coerceAtLeast(0)
                val remainingAmount = loan.monthlyEmi * remainingMonths
                val loanLabel = "${loan.loanName} (${loan.lenderBank})"
                canvas.drawText(if (loanLabel.length > 25) loanLabel.substring(0, 22) + "..." else loanLabel, 40f, y, textPaint)
                canvas.drawText(formatIndianCurrency(loan.monthlyEmi), 220f, y, textPaint)
                canvas.drawText("$paidMonths/$totalMonths Mo", 340f, y, textPaint)
                canvas.drawText(formatIndianCurrency(remainingAmount), 430f, y, textPaint)
                y += 18f
            }
        }

        // Footer & Watermark
        y = (y + 30f).coerceAtMost(750f)
        canvas.drawLine(40f, y, 555f, y, headerPaint)
        y += 15f
        canvas.drawText("Generated by CashMath - Smart Digital Ledger", 40f, y, headerPaint)

        pdfDocument.finishPage(page)

        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return file
    }

    fun generateDuesOnlyPdf(
        businessName: String,
        dueParties: List<DuePartyEntity>,
        uiState: DuesEmiUiState,
        file: File
    ): File {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint().apply { color = Color.BLACK; textSize = 18f; isFakeBoldText = true }
        val headerPaint = Paint().apply { color = Color.DKGRAY; textSize = 12f }
        val sectionPaint = Paint().apply { color = Color.rgb(10, 37, 64); textSize = 13f; isFakeBoldText = true }
        val textPaint = Paint().apply { color = Color.BLACK; textSize = 10f }
        val tableHeaderPaint = Paint().apply { color = Color.BLACK; textSize = 10f; isFakeBoldText = true }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var y = 40f

        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        canvas.drawText("CashMath Dues & Khata Statement", 40f, y, titlePaint)
        y += 22f
        canvas.drawText("Business: $businessName", 40f, y, headerPaint)
        y += 15f
        canvas.drawText("Generated on: $currentDate", 40f, y, headerPaint)
        y += 25f

        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRect(40f, y, 555f, y + 45f, paint)
        y += 18f
        canvas.drawText("Total Dues Receivable: ${formatIndianCurrency(uiState.totalToReceive)}", 55f, y, tableHeaderPaint)
        canvas.drawText("Total Dues Payable: ${formatIndianCurrency(uiState.totalToPay)}", 300f, y, tableHeaderPaint)
        y += 18f
        canvas.drawText("Net Dues Balance: ${formatIndianCurrency(uiState.netDues)}", 55f, y, tableHeaderPaint)
        y += 30f

        canvas.drawText("Customers & Suppliers Khata List", 40f, y, sectionPaint)
        y += 18f
        canvas.drawText("Name", 40f, y, tableHeaderPaint)
        canvas.drawText("Phone", 220f, y, tableHeaderPaint)
        canvas.drawText("To Receive (Get)", 340f, y, tableHeaderPaint)
        canvas.drawText("To Pay (Give)", 450f, y, tableHeaderPaint)
        y += 8f
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 18f

        dueParties.forEach { party ->
            if (y > 780f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 40f
            }
            canvas.drawText(party.name, 40f, y, textPaint)
            canvas.drawText(party.phone ?: "-", 220f, y, textPaint)
            canvas.drawText(if (party.toReceive > 0) formatIndianCurrency(party.toReceive) else "-", 340f, y, textPaint)
            canvas.drawText(if (party.toPay > 0) formatIndianCurrency(party.toPay) else "-", 450f, y, textPaint)
            y += 18f
        }

        y = (y + 30f).coerceAtMost(750f)
        canvas.drawLine(40f, y, 555f, y, headerPaint)
        y += 15f
        canvas.drawText("Generated by CashMath - Smart Digital Ledger", 40f, y, headerPaint)

        pdfDocument.finishPage(page)
        FileOutputStream(file).use { out -> pdfDocument.writeTo(out) }
        pdfDocument.close()
        return file
    }

    fun generateEmiOnlyPdf(
        businessName: String,
        emiLoans: List<EmiLoanEntity>,
        uiState: DuesEmiUiState,
        file: File
    ): File {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint().apply { color = Color.BLACK; textSize = 18f; isFakeBoldText = true }
        val headerPaint = Paint().apply { color = Color.DKGRAY; textSize = 12f }
        val sectionPaint = Paint().apply { color = Color.rgb(10, 37, 64); textSize = 13f; isFakeBoldText = true }
        val textPaint = Paint().apply { color = Color.BLACK; textSize = 10f }
        val tableHeaderPaint = Paint().apply { color = Color.BLACK; textSize = 10f; isFakeBoldText = true }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var y = 40f

        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        canvas.drawText("CashMath Loan & EMI Schedule Report", 40f, y, titlePaint)
        y += 22f
        canvas.drawText("Business: $businessName", 40f, y, headerPaint)
        y += 15f
        canvas.drawText("Generated on: $currentDate", 40f, y, headerPaint)
        y += 25f

        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRect(40f, y, 555f, y + 45f, paint)
        y += 18f
        canvas.drawText("Monthly EMI Commitment: ${formatIndianCurrency(uiState.activeMonthlyEmiTotal)}", 55f, y, tableHeaderPaint)
        canvas.drawText("Total Outstanding Loans: ${formatIndianCurrency(uiState.totalOutstandingEmi)}", 300f, y, tableHeaderPaint)
        y += 30f

        canvas.drawText("Active Loan & EMI Schedule", 40f, y, sectionPaint)
        y += 18f
        canvas.drawText("Loan / Bank", 40f, y, tableHeaderPaint)
        canvas.drawText("Monthly EMI", 220f, y, tableHeaderPaint)
        canvas.drawText("Tenure", 340f, y, tableHeaderPaint)
        canvas.drawText("Remaining Principal", 430f, y, tableHeaderPaint)
        y += 8f
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 18f

        if (emiLoans.isEmpty()) {
            canvas.drawText("No active loans recorded.", 40f, y, textPaint)
        } else {
            emiLoans.forEach { loan ->
                if (y > 780f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = 40f
                }
                val totalMonths = if (loan.totalTenureMonths > 0) loan.totalTenureMonths else if (loan.monthlyEmi > 0) ((loan.monthlyEmi * loan.totalTenureMonths) / loan.monthlyEmi).toInt() else 0
                val paidMonths = loan.paidMonths.coerceAtLeast(0)
                val remainingMonths = (totalMonths - paidMonths).coerceAtLeast(0)
                val remainingAmount = loan.monthlyEmi * remainingMonths
                val loanLabel = "${loan.loanName} (${loan.lenderBank})"
                canvas.drawText(if (loanLabel.length > 25) loanLabel.substring(0, 22) + "..." else loanLabel, 40f, y, textPaint)
                canvas.drawText(formatIndianCurrency(loan.monthlyEmi), 220f, y, textPaint)
                canvas.drawText("$paidMonths/$totalMonths Mo", 340f, y, textPaint)
                canvas.drawText(formatIndianCurrency(remainingAmount), 430f, y, textPaint)
                y += 18f
            }
        }

        y = (y + 30f).coerceAtMost(750f)
        canvas.drawLine(40f, y, 555f, y, headerPaint)
        y += 15f
        canvas.drawText("Generated by CashMath - Smart Digital Ledger", 40f, y, headerPaint)

        pdfDocument.finishPage(page)
        FileOutputStream(file).use { out -> pdfDocument.writeTo(out) }
        pdfDocument.close()
        return file
    }

    fun generateCsv(
        dueParties: List<DuePartyEntity>,
        emiLoans: List<EmiLoanEntity>,
        file: File
    ): File {
        FileWriter(file).use { writer ->
            writer.append("--- KHATA PARTIES (CUSTOMERS & SUPPLIERS) ---\n")
            writer.append("ID,Party Name,Phone,Amount To Receive (INR),Amount To Pay (INR)\n")
            dueParties.forEach { p ->
                writer.append("${p.id},\"${p.name}\",\"${p.phone ?: ""}\",${p.toReceive},${p.toPay}\n")
            }

            writer.append("\n--- ACTIVE LOANS & EMI TRACKER ---\n")
            writer.append("ID,Loan Name,Lender Bank,Monthly EMI (INR),Due Day,Paid Months,Total Tenure Months,Remaining Months,Remaining Principal (INR)\n")
            emiLoans.forEach { l ->
                val totalMonths = if (l.totalTenureMonths > 0) l.totalTenureMonths else if (l.monthlyEmi > 0) ((l.monthlyEmi * l.totalTenureMonths) / l.monthlyEmi).toInt() else 0
                val paidMonths = l.paidMonths.coerceAtLeast(0)
                val remainingMonths = (totalMonths - paidMonths).coerceAtLeast(0)
                val remainingAmount = l.monthlyEmi * remainingMonths
                writer.append("${l.id},\"${l.loanName}\",\"${l.lenderBank}\",${l.monthlyEmi},${l.dueDayOfMonth},$paidMonths,$totalMonths,$remainingMonths,$remainingAmount\n")
            }
        }
        return file
    }

    fun generateDuesOnlyCsv(dueParties: List<DuePartyEntity>, file: File): File {
        FileWriter(file).use { writer ->
            writer.append("--- KHATA PARTIES (CUSTOMERS & SUPPLIERS) ---\n")
            writer.append("ID,Party Name,Phone,Amount To Receive (INR),Amount To Pay (INR)\n")
            dueParties.forEach { p ->
                writer.append("${p.id},\"${p.name}\",\"${p.phone ?: ""}\",${p.toReceive},${p.toPay}\n")
            }
        }
        return file
    }

    fun generateEmiOnlyCsv(emiLoans: List<EmiLoanEntity>, file: File): File {
        FileWriter(file).use { writer ->
            writer.append("--- ACTIVE LOANS & EMI TRACKER ---\n")
            writer.append("ID,Loan Name,Lender Bank,Monthly EMI (INR),Due Day,Paid Months,Total Tenure Months,Remaining Months,Remaining Principal (INR)\n")
            emiLoans.forEach { l ->
                val totalMonths = if (l.totalTenureMonths > 0) l.totalTenureMonths else if (l.monthlyEmi > 0) ((l.monthlyEmi * l.totalTenureMonths) / l.monthlyEmi).toInt() else 0
                val paidMonths = l.paidMonths.coerceAtLeast(0)
                val remainingMonths = (totalMonths - paidMonths).coerceAtLeast(0)
                val remainingAmount = l.monthlyEmi * remainingMonths
                writer.append("${l.id},\"${l.loanName}\",\"${l.lenderBank}\",${l.monthlyEmi},${l.dueDayOfMonth},$paidMonths,$totalMonths,$remainingMonths,$remainingAmount\n")
            }
        }
        return file
    }
}
