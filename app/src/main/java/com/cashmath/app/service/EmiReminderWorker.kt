package com.cashmath.app.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.cashmath.app.R
import com.cashmath.app.data.AppDatabase
import com.cashmath.app.ui.MainActivity
import com.cashmath.app.ui.screens.formatIndianCurrency
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.util.Calendar

class EmiReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(context)
            val businesses = db.businessDao().getAllBusinesses().first()
            if (businesses.isEmpty()) return Result.success()

            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 1)
            }
            val tomorrowDay = calendar.get(Calendar.DAY_OF_MONTH)

            for (biz in businesses) {
                val loans = db.emiLoanDao().getLoansByBusiness(biz.id).first()
                val dueTomorrow = loans.filter {
                    (it.dueDayOfMonth == tomorrowDay) && (it.paidMonths < it.totalTenureMonths)
                }

                for (loan in dueTomorrow) {
                    showNotification(loan.id.toInt(), loan.loanName, loan.lenderBank, loan.monthlyEmi)
                }
            }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun showNotification(id: Int, loanName: String, bank: String, emiAmount: Double) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val channelId = "emi_due_channel"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "EMI Due Reminders",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notifications for upcoming EMI loan due dates"
            }
            manager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle("EMI Due Tomorrow: $loanName")
            .setContentText("${formatIndianCurrency(emiAmount)} due tomorrow ($bank). Tap to mark paid.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(id, notification)
    }

    companion object {
        fun scheduleDailyEmiCheck(context: Context) {
            val dailyWorkRequest = PeriodicWorkRequestBuilder<EmiReminderWorker>(Duration.ofHours(24))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build(),
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "EmiDailyReminderWork",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest,
            )
        }
    }
}
