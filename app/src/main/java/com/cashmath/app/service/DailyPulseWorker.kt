package com.cashmath.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.cashmath.app.data.AppDatabase
import com.cashmath.app.ui.screens.formatIndianCurrency
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.util.Calendar

class DailyPulseWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(context)
            val businesses = db.businessDao().getAllBusinesses().first()
            if (businesses.isEmpty()) return Result.success()

            val primaryBizId = businesses.first().id
            val parties = db.duePartyDao().getPartiesByBusiness(primaryBizId).first()
            val totalToReceive = parties.sumOf { it.toReceive }

            val channelId = "daily_pulse_channel"
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Daily Business Pulse",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "Daily 9:00 PM summary of business dues and balance"
                }
                manager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(com.cashmath.app.R.drawable.ic_stat_notification)
                .setContentTitle("CashMath Daily Pulse 📊")
                .setContentText("Pending Receivables: ${formatIndianCurrency(totalToReceive)}. Tap to view today's ledger.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            manager.notify(9001, notification)

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun scheduleDailyPulseCheck(context: Context) {
            val currentTime = Calendar.getInstance()
            val dueTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 21)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            if (dueTime.before(currentTime)) {
                dueTime.add(Calendar.DAY_OF_MONTH, 1)
            }
            val initialDelay = dueTime.timeInMillis - currentTime.timeInMillis

            val dailyWork = PeriodicWorkRequestBuilder<DailyPulseWorker>(Duration.ofHours(24))
                .setInitialDelay(Duration.ofMillis(initialDelay))
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "DailyPulseWork",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWork,
            )
        }
    }
}
