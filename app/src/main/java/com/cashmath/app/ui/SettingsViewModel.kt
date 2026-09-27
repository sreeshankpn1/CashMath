package com.cashmath.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.cashmath.app.data.AppThemeMode
import com.cashmath.app.data.SettingsRepository
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)

    val isBiometricEnabled: StateFlow<Boolean> = repository.isBiometricEnabled
    val themeMode: StateFlow<AppThemeMode> = repository.themeMode

    fun setBiometricEnabled(enabled: Boolean) {
        repository.setBiometricEnabled(enabled)
    }

    fun setThemeMode(mode: AppThemeMode) {
        repository.setThemeMode(mode)
    }
}
