package com.cashmath.app.service

import android.content.Context
import android.content.Intent
import android.net.Uri

object InviteManager {
    private const val DEEP_LINK_BASE = "https://cashmath.app/invite"
    
    fun createWhatsAppInviteIntent(bookName: String, bookId: String, inviteCode: String): Intent {
        val link = "$DEEP_LINK_BASE?bookId=$bookId&code=$inviteCode"
        val message = "I've invited you to collaborate on '$bookName' on CashMath. Join here to track entries together: $link"
        
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            setPackage("com.whatsapp")
        }
    }

    fun parseInviteUri(uri: Uri): Pair<String?, String?> {
        val bookId = uri.getQueryParameter("bookId")
        val inviteCode = uri.getQueryParameter("code")
        return Pair(bookId, inviteCode)
    }
}
