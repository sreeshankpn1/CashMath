package com.cashmath.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.cashmath.app.data.AppDatabase
import com.cashmath.app.data.TransactionType
import com.cashmath.app.ui.MainActivity
import com.cashmath.app.ui.screens.formatIndianCurrency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class BusinessPulseReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == ACTION_DAILY_PULSE || action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val businesses = db.businessDao().getAllBusinesses().first()
                    
                    var totalIn = 0.0
                    var totalOut = 0.0

                    if (businesses.isNotEmpty()) {
                        val calendar = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val startOfDay = calendar.timeInMillis
                        val endOfDay = startOfDay + (24 * 60 * 60 * 1000)

                        val todayTxs = db.transactionDao().getTransactionsBetweenSync(startOfDay, endOfDay)
                        todayTxs.forEach { tx ->
                            if (tx.type == TransactionType.INCOME) {
                                totalIn += tx.amount
                            } else {
                                totalOut += tx.amount
                            }
                        }
                    }

                    showPulseNotification(context, totalIn, totalOut)
                } catch (_: Exception) {
                    showPulseNotification(context, 0.0, 0.0)
                } finally {
                    // Reschedule for tomorrow
                    BusinessPulseScheduler.scheduleFromPreferences(context)
                }
            }
        }
    }

    private fun showPulseNotification(context: Context, totalIn: Double, totalOut: Double) {
        val channelId = "cashmath_daily_pulse_channel"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "CashMath Daily Pulse",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Evening business pulse and closing cash summary"
                enableVibration(true)
                setShowBadge(true)
            }
            manager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_REQUEST_CODE,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val netBalance = totalIn - totalOut
        val summaryText = if (totalIn > 0 || totalOut > 0) {
            "Cash In: ${formatIndianCurrency(totalIn)}  |  Cash Out: ${formatIndianCurrency(totalOut)}  |  Net: ${formatIndianCurrency(netBalance)}"
        } else {
            "₹0 logged today. Tap to record your closing cash & dues."
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.cashmath.app.R.drawable.ic_stat_notification)
            .setContentTitle("CashMath Evening Pulse (9:00 PM)")
            .setContentText(summaryText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Today's Business Summary:\n• $summaryText\n\nAll accounts updated."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(PULSE_NOTIFICATION_ID, notification)
    }

    companion object {
        const val ACTION_DAILY_PULSE = "com.cashmath.app.ACTION_DAILY_PULSE"
        const val NOTIFICATION_REQUEST_CODE = 9001
        const val PULSE_NOTIFICATION_ID = 9001
    }
}
