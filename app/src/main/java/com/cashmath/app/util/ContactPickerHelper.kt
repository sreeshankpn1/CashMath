package com.cashmath.app.util

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

object ContactPickerHelper {

    fun extractContactDetails(context: Context, contactUri: Uri): Pair<String, String> {
        var name = ""
        var phone = ""
        try {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )
            context.contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                    if (nameIdx != -1) {
                        name = cursor.getString(nameIdx) ?: ""
                    }
                    if (phoneIdx != -1) {
                        phone = cursor.getString(phoneIdx) ?: ""
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Pair(name, cleanPhoneNumber(phone))
    }

    fun cleanPhoneNumber(raw: String): String {
        val digits = raw.replace(Regex("[^0-9+]"), "")
        return when {
            digits.startsWith("+") -> digits.replace("+", "")
            digits.length == 10 -> "91$digits"
            digits.startsWith("0") && digits.length == 11 -> "91" + digits.substring(1)
            else -> digits
        }
    }
}
