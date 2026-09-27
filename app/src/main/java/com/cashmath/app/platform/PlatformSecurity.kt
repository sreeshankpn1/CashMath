package com.cashmath.app.platform

import androidx.fragment.app.FragmentActivity
import com.cashmath.app.security.BiometricSecurityManager

class PlatformSecurity(private val activity: FragmentActivity) {
    fun authenticate(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        BiometricSecurityManager.authenticate(
            activity = activity,
            title = title,
            subtitle = subtitle,
            onSuccess = onSuccess,
            onError = onError
        )
    }
}
