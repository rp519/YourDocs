package com.yourdocs.data.billing

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.premiumDataStore: DataStore<Preferences> by preferencesDataStore(name = "premium_preferences")

@Singleton
class PremiumRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val PURCHASE_TOKEN = stringPreferencesKey("purchase_token")
        val PURCHASE_TIME = longPreferencesKey("purchase_time")
    }

    val isPremium: Flow<Boolean> = context.premiumDataStore.data.map { prefs ->
        prefs[Keys.IS_PREMIUM] ?: false
    }

    suspend fun setPremiumStatus(isPremium: Boolean, token: String? = null, time: Long? = null) {
        context.premiumDataStore.edit { prefs ->
            prefs[Keys.IS_PREMIUM] = isPremium
            if (token != null) {
                prefs[Keys.PURCHASE_TOKEN] = token
            }
            if (time != null) {
                prefs[Keys.PURCHASE_TIME] = time
            }
        }
    }

    suspend fun clearPremiumStatus() {
        context.premiumDataStore.edit { prefs ->
            prefs[Keys.IS_PREMIUM] = false
            prefs.remove(Keys.PURCHASE_TOKEN)
            prefs.remove(Keys.PURCHASE_TIME)
        }
    }
}
