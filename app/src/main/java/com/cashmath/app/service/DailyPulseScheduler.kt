package com.cashmath.app.service

import android.content.Context

object DailyPulseScheduler {

    fun schedule(context: Context, hour: Int, minute: Int, isEnabled: Boolean) {
        BusinessPulseScheduler.schedule(context, hour, minute, isEnabled)
    }
}
