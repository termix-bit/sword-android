package com.sword.security.android.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sword.security.android.core.validation.InputValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.swordDataStore: DataStore<Preferences> by preferencesDataStore(name = "sword_preferences")

data class UserPreferences(
    val onboardingCompleted: Boolean = false,
    val animationsEnabled: Boolean = true,
    val requireAuthorizationConfirmation: Boolean = true,
    val networkTimeoutSeconds: Int = 10,
    val maxConcurrency: Int = 4,
    val proxyUrl: String = "",
    val dohProvider: String = "Cloudflare",
    val languageCode: String = "en",
    val themeMode: String = "dark"
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
        val REQUIRE_AUTH_CONFIRM = booleanPreferencesKey("require_auth_confirm")
        val NETWORK_TIMEOUT_SECONDS = intPreferencesKey("network_timeout_seconds")
        val MAX_CONCURRENCY = intPreferencesKey("max_concurrency")
        val PROXY_URL = stringPreferencesKey("proxy_url")
        val DOH_PROVIDER = stringPreferencesKey("doh_provider")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val preferencesFlow: Flow<UserPreferences> = context.swordDataStore.data.map { prefs ->
        UserPreferences(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            animationsEnabled = prefs[Keys.ANIMATIONS_ENABLED] ?: true,
            requireAuthorizationConfirmation = prefs[Keys.REQUIRE_AUTH_CONFIRM] ?: true,
            networkTimeoutSeconds = (prefs[Keys.NETWORK_TIMEOUT_SECONDS] ?: 10).coerceIn(3, 60),
            maxConcurrency = (prefs[Keys.MAX_CONCURRENCY] ?: 4).coerceIn(1, 8),
            proxyUrl = prefs[Keys.PROXY_URL] ?: "",
            dohProvider = prefs[Keys.DOH_PROVIDER] ?: "Cloudflare",
            languageCode = prefs[Keys.LANGUAGE_CODE] ?: "en",
            themeMode = prefs[Keys.THEME_MODE] ?: "dark"
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.swordDataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setAnimationsEnabled(enabled: Boolean) {
        context.swordDataStore.edit { it[Keys.ANIMATIONS_ENABLED] = enabled }
    }

    suspend fun setRequireAuthorizationConfirmation(required: Boolean) {
        context.swordDataStore.edit { it[Keys.REQUIRE_AUTH_CONFIRM] = required }
    }

    suspend fun setNetworkTimeoutSeconds(seconds: Int) {
        context.swordDataStore.edit { it[Keys.NETWORK_TIMEOUT_SECONDS] = seconds.coerceIn(3, 60) }
    }

    suspend fun setMaxConcurrency(limit: Int) {
        context.swordDataStore.edit { it[Keys.MAX_CONCURRENCY] = limit.coerceIn(1, 8) }
    }

    suspend fun setProxyUrl(rawProxy: String) {
        val validated = InputValidator.validateProxyUrl(rawProxy)
        context.swordDataStore.edit { it[Keys.PROXY_URL] = validated }
    }

    suspend fun setDohProvider(provider: String) {
        val safe = if (provider in listOf("Cloudflare", "Google", "Quad9")) provider else "Cloudflare"
        context.swordDataStore.edit { it[Keys.DOH_PROVIDER] = safe }
    }

    suspend fun setLanguageCode(code: String) {
        val safe = if (code in listOf("en", "fa")) code else "en"
        context.swordDataStore.edit { it[Keys.LANGUAGE_CODE] = safe }
    }

    suspend fun setThemeMode(mode: String) {
        val safe = if (mode in listOf("dark", "light", "system")) mode else "dark"
        context.swordDataStore.edit { it[Keys.THEME_MODE] = safe }
    }

    suspend fun resetDefaults() {
        context.swordDataStore.edit { prefs ->
            val onboarded = prefs[Keys.ONBOARDING_COMPLETED] ?: true
            prefs.clear()
            prefs[Keys.ONBOARDING_COMPLETED] = onboarded
        }
    }
}
