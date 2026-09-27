package com.cashmath.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import com.cashmath.app.util.ContactPickerHelper
import java.net.URLEncoder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object WhatsAppReminderHelper {

    val DEFAULT_RECEIVE_TEMPLATE = """
*PAYMENT REMINDER: OUTSTANDING BALANCE*

Hi {party_name},

This is a follow-up regarding your pending balance of *₹{amount}* with *{business_name}*.

────────────────────────
📌 *Balance Due:* ₹{amount}
📅 *Status:* Due for Immediate Settlement
────────────────────────

Please arrange for the settlement today to keep your account up to date.

_If you have already transferred this amount, kindly reply with the confirmation or screenshot._

Thank you for your prompt attention.

*{business_name}*
    """.trimIndent()

    val DEFAULT_PAY_TEMPLATE = """
*PAYMENT SETTLEMENT UPDATE*

Hi {party_name},

Hope you are doing well.

According to our accounts at *{business_name}*, we have a pending payable balance of *₹{amount}* to settle with you.

────────────────────────
📌 *Amount to Pay You:* ₹{amount}
📅 *Status:* Ready for Settlement
────────────────────────

Please share your preferred UPI ID or Bank Account details so we can transfer this amount to you right away.

Thank you!

*{business_name}*
    """.trimIndent()

    fun generateStandardFilename(
        name: String,
        reportType: String,
        extension: String = "pdf"
    ): String {
        val cleanName = name.trim().replace(Regex("[^a-zA-Z0-9]"), "_").ifBlank { "CashMath" }
        val dateStr = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH).format(Date())
        return "${cleanName}_CashMath_${reportType}_$dateStr.$extension"
    }

    fun formatMessage(
        template: String,
        partyName: String,
        amount: Double,
        businessName: String
    ): String {
        val absAmount = abs(amount)
        val formattedAmount = NumberFormat.getNumberInstance(Locale("en", "IN"))
            .format(absAmount)

        return template
            .replace("RupeeMath", "CashMath")
            .replace("{party_name}", partyName.ifBlank { "Customer" })
            .replace("{name}", partyName.ifBlank { "Customer" })
            .replace("{amount}", formattedAmount)
            .replace("{business_name}", businessName.ifBlank { "our business" })
    }

    fun sendDirectWhatsApp(context: Context, phone: String, message: String) {
        launchWhatsApp(context, phone, message)
    }

    fun launchWhatsApp(context: Context, rawPhone: String, messageText: String) {
        var clean = rawPhone.replace(Regex("[^0-9]"), "")
        if (clean.startsWith("0")) clean = clean.substring(1)
        val finalPhone = if (clean.length == 10) "91$clean" else clean

        try {
            val encoded = URLEncoder.encode(messageText, "UTF-8")
            val uri = if (finalPhone.isNotBlank()) {
                "https://api.whatsapp.com/send?phone=$finalPhone&text=$encoded".toUri()
            } else {
                "https://api.whatsapp.com/send?text=$encoded".toUri()
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                setPackage("com.whatsapp")
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, messageText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(Intent.createChooser(fallbackIntent, "Send Reminder via"))
            } catch (_: Exception) {
                Toast.makeText(context, "Could not launch messaging app", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getMessage(
        context: Context,
        partyName: String,
        amountDue: Double,
        businessName: String = "CashMath Ledger",
        isReceivable: Boolean = true
    ): String {
        val prefs = context.getSharedPreferences("cashmath_settings_prefs", Context.MODE_PRIVATE)

        val customReceive = prefs.getString("custom_template_receive", null)
        val customPay = prefs.getString("custom_template_pay", null)

        val template = if (isReceivable) {
            if (!customReceive.isNullOrBlank()) customReceive else DEFAULT_RECEIVE_TEMPLATE
        } else {
            if (!customPay.isNullOrBlank()) customPay else DEFAULT_PAY_TEMPLATE
        }

        return formatMessage(template, partyName, amountDue, businessName)
    }

    fun sendPaymentReminder(
        context: Context,
        partyName: String,
        partyPhone: String,
        amountDue: Double,
        businessName: String = "CashMath Ledger",
        isReceivable: Boolean = true
    ) {
        val message = getMessage(context, partyName, amountDue, businessName, isReceivable)
        launchWhatsApp(context, partyPhone, message)
    }
}
