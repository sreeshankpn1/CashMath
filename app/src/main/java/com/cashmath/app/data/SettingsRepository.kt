package com.cashmath.app.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode { LIGHT, DARK, SYSTEM }

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("cashmath_settings_prefs", Context.MODE_PRIVATE)

    private val _isBiometricEnabled = MutableStateFlow(
        prefs.getBoolean("key_biometric_enabled", true),
    )
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _themeMode = MutableStateFlow(
        try {
            AppThemeMode.valueOf(prefs.getString("key_theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            AppThemeMode.SYSTEM
        }
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _isSmsTrackingEnabled = MutableStateFlow(
        prefs.getBoolean("key_sms_tracking_enabled", true)
    )
    val isSmsTrackingEnabled: StateFlow<Boolean> = _isSmsTrackingEnabled.asStateFlow()

    private val _requireUserConfirmation = MutableStateFlow(
        prefs.getBoolean("key_sms_require_confirmation", true)
    )
    val requireUserConfirmation: StateFlow<Boolean> = _requireUserConfirmation.asStateFlow()

    private val _defaultCashbookId = MutableStateFlow(
        prefs.getLong("key_sms_default_cashbook_id", 1L)
    )
    val defaultCashbookId: StateFlow<Long> = _defaultCashbookId.asStateFlow()

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("key_biometric_enabled", enabled) }
        _isBiometricEnabled.value = enabled
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit { putString("key_theme_mode", mode.name) }
        _themeMode.value = mode
    }

    fun setSmsTrackingEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("key_sms_tracking_enabled", enabled) }
        _isSmsTrackingEnabled.value = enabled
    }

    fun setRequireUserConfirmation(required: Boolean) {
        prefs.edit { putBoolean("key_sms_require_confirmation", required) }
        _requireUserConfirmation.value = required
    }

    fun setDefaultCashbookId(id: Long) {
        prefs.edit { putLong("key_sms_default_cashbook_id", id) }
        _defaultCashbookId.value = id
    }
}
