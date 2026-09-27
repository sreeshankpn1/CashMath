package com.cashmath.app.security

import androidx.fragment.app.FragmentActivity

object BiometricAuthHelper {
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Authentication Required",
        subtitle: String = "Verify identity to proceed with edit",
        onSuccess: () -> Unit,
    ) {
        BiometricSecurityManager.authenticate(
            activity = activity,
            title = title,
            subtitle = subtitle,
            onSuccess = onSuccess
        )
    }
}
