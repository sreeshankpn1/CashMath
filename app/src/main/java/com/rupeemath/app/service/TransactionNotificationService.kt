package com.rupeemath.app.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.rupeemath.app.data.AppDatabase
import com.rupeemath.app.parser.TransactionParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionNotificationService : NotificationListenerService() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val targetPackages = setOf(
        "com.phonepe.app",
        "com.google.android.apps.nbu.paisa.user",
        "net.one97.paytm",
        "in.org.npci.upiapp"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkg = sbn.packageName ?: return
        val isTargetApp = targetPackages.contains(pkg) || pkg.contains("mms") || pkg.contains("messaging")

        if (!isTargetApp) return

        val extras = sbn.notification?.extras ?: return
        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        val parsed = TransactionParser.parse(pkg, title, text)
        if (parsed != null) {
            Log.i("RupeeMath", "Tracked: ₹${parsed.amount} [${parsed.type}] from ${parsed.sourceApp}")
            coroutineScope.launch {
                val db = AppDatabase.getDatabase(applicationContext)
                db.transactionDao().insert(parsed)
            }
        }
    }
}
