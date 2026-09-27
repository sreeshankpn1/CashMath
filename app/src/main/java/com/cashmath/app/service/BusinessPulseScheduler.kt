package com.cashmath.app.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.util.Calendar

object BusinessPulseScheduler {

    private const val PREFS_NAME = "cashmath_settings_prefs"
    private const val LEGACY_PREFS_NAME = "rupeemath_settings_prefs"
    private const val WORK_NAME = "cashmath_daily_pulse"

    fun scheduleFromPreferences(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val legacyPrefs = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)

        val isEnabled = if (prefs.contains("pulse_enabled")) {
            prefs.getBoolean("pulse_enabled", true)
        } else {
            legacyPrefs.getBoolean("pulse_enabled", true)
        }

        val hour = if (prefs.contains("pulse_hour")) {
            prefs.getInt("pulse_hour", 21)
        } else {
            legacyPrefs.getInt("pulse_hour", 21)
        }

        val minute = if (prefs.contains("pulse_minute")) {
            prefs.getInt("pulse_minute", 0)
        } else {
            legacyPrefs.getInt("pulse_minute", 0)
        }

        schedule(context, hour, minute, isEnabled)
    }

    fun schedule(context: Context, hour: Int, minute: Int, isEnabled: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, BusinessPulseReceiver::class.java).apply {
            action = BusinessPulseReceiver.ACTION_DAILY_PULSE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            BusinessPulseReceiver.NOTIFICATION_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (!isEnabled) {
            alarmManager.cancel(pendingIntent)
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            return
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // Schedule Exact Alarm with AllowWhileIdle
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }

        // WorkManager background backup
        val initialDelayMillis = calendar.timeInMillis - System.currentTimeMillis()
        if (initialDelayMillis > 0) {
            val workRequest = PeriodicWorkRequestBuilder<DailyPulseWorker>(Duration.ofHours(24))
                .setInitialDelay(Duration.ofMillis(initialDelayMillis))
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }
}
