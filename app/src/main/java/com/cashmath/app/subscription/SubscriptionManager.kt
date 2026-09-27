package com.cashmath.app.subscription

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.subDataStore by preferencesDataStore("subscription_prefs")

enum class UserPlan { FREE, FOUNDER_ANNUAL, LIFETIME_PRO }

data class SubscriptionState(
    val isPro: Boolean,
    val plan: UserPlan,
    val expiryTimestamp: Long,
    val maxBusinessesAllowed: Int,
    val maxMembersPerBusiness: Int
)

class SubscriptionManager(private val context: Context) {
    companion object {
        val IS_PRO_KEY = booleanPreferencesKey("is_pro_user")
        val EXPIRY_KEY = longPreferencesKey("sub_expiry_timestamp")
        val PLAN_KEY = booleanPreferencesKey("is_lifetime")
    }

    val subscriptionState: Flow<SubscriptionState> = context.subDataStore.data.map { prefs ->
        val isPro = prefs[IS_PRO_KEY] ?: false
        val expiry = prefs[EXPIRY_KEY] ?: 0L
        val isLifetime = prefs[PLAN_KEY] ?: false

        val activePro = isPro && (isLifetime || expiry > System.currentTimeMillis())

        SubscriptionState(
            isPro = activePro,
            plan = if (isLifetime) UserPlan.LIFETIME_PRO else if (activePro) UserPlan.FOUNDER_ANNUAL else UserPlan.FREE,
            expiryTimestamp = expiry,
            maxBusinessesAllowed = if (activePro) Int.MAX_VALUE else 1,
            maxMembersPerBusiness = if (activePro) Int.MAX_VALUE else 2
        )
    }

    suspend fun activateSubscription(isLifetime: Boolean, durationDays: Int = 365) {
        context.subDataStore.edit { prefs ->
            prefs[IS_PRO_KEY] = true
            prefs[PLAN_KEY] = isLifetime
            prefs[EXPIRY_KEY] = if (isLifetime) Long.MAX_VALUE else System.currentTimeMillis() + (durationDays * 24L * 60 * 60 * 1000)
        }
    }

    suspend fun clearSubscription() {
        context.subDataStore.edit { prefs ->
            prefs[IS_PRO_KEY] = false
            prefs[PLAN_KEY] = false
            prefs[EXPIRY_KEY] = 0L
        }
    }
}
