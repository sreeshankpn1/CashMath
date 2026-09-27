package com.cashmath.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cashmath.app.data.AppDatabase
import com.cashmath.app.data.SettingsRepository
import com.cashmath.app.data.TransactionEntity
import com.cashmath.app.data.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

class CashMathNotificationListener : NotificationListenerService() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val notificationIdCounter = AtomicInteger(1000)

    private val targetPackages = setOf(
        "com.phonepe.app",
        "com.google.android.apps.nbu.paisa.user",
        "net.one97.paytm",
        "in.org.npci.upiapp",
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

        val settingsRepo = SettingsRepository(applicationContext)
        if (!settingsRepo.isSmsTrackingEnabled.value) return

        val classified = TransactionSmsClassifier.classify(title, text) ?: return

        PendingTransactionManager.postTransaction(
            PendingTransactionManager.PendingTransaction(
                amount = classified.amount,
                isCashIn = !classified.isDebit,
                senderOrBank = classified.partyOrSender,
                rawMessage = text
            )
        )

        scope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val requireConfirmation = settingsRepo.requireUserConfirmation.value

            if (!requireConfirmation) {
                val bookId = settingsRepo.defaultCashbookId.value

                val transaction = TransactionEntity(
                    bookId = bookId,
                    amount = classified.amount,
                    type = if (classified.isDebit) TransactionType.EXPENSE else TransactionType.INCOME,
                    partyName = classified.partyOrSender,
                    paymentMode = "Auto SMS/App",
                    remark = "Auto-tracked via SMS/Notification",
                    transactionTimestamp = System.currentTimeMillis(),
                    createdByName = "Auto-Tracker",
                )
                db.transactionDao().insert(transaction)
                db.bookDao().recalculateBookBalance(bookId)
            }

            notifyUserDetected(classified, requireConfirmation)
        }
    }

    private fun notifyUserDetected(classified: ClassifiedSmsTransaction, requiredConfirmation: Boolean) {
        val channelId = "upi_tracked_channel"
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Auto-Tracked Transactions",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Notifications for auto-tracked UPI & Bank transactions"
            }
            manager.createNotificationChannel(channel)
        }

        val direction = if (classified.isDebit) "Debited" else "Credited"
        val notificationTitle = if (requiredConfirmation) {
            "Review Transaction: ₹${String.format(Locale.getDefault(), "%.2f", classified.amount)}"
        } else {
            "Auto-Tracked $direction: ₹${String.format(Locale.getDefault(), "%.2f", classified.amount)}"
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.cashmath.app.R.drawable.ic_stat_notification)
            .setContentTitle(notificationTitle)
            .setContentText("${classified.partyOrSender} • Tap to confirm book assignment")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(notificationIdCounter.incrementAndGet(), notification)
    }

    companion object {
        @Suppress("unused")
        fun isNotificationAccessGranted(context: Context): Boolean {
            val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
            return enabledPackages.contains(context.packageName)
        }
    }
}
