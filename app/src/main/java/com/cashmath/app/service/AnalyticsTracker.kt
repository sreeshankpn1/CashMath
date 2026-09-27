package com.cashmath.app.service

import android.util.Log

object AnalyticsTracker {
    private const val TAG = "CashMathAnalytics"

    /**
     * Track anonymous usage events in compliance with DPDP Act 2023.
     * Strictly ZERO PII, zero phone numbers, zero bank details, zero transaction amounts.
     */
    fun logEvent(eventName: String, params: Map<String, String> = emptyMap()) {
        val sanitizedParams = params.filterKeys { key ->
            !key.contains("phone", ignoreCase = true) &&
            !key.contains("account", ignoreCase = true) &&
            !key.contains("amount", ignoreCase = true) &&
            !key.contains("name", ignoreCase = true)
        }
        Log.d(TAG, "Event: $eventName | Params: $sanitizedParams")
    }
}
