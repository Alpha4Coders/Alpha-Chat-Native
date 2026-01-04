package com.example.alpha_chat_native.data.remote

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "alpha_chat_prefs")

/**
 * Manages session cookie storage for authenticated API requests.
 * The cookie is captured from GitHub OAuth WebView callback.
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val SESSION_COOKIE_KEY = stringPreferencesKey("session_cookie")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    /**
     * Save the session cookie from OAuth callback
     */
    suspend fun saveSessionCookie(cookie: String) {
        context.dataStore.edit { prefs ->
            prefs[SESSION_COOKIE_KEY] = cookie
        }
    }

    /**
     * Get the session cookie for API requests (blocking for interceptor)
     */
    fun getSessionCookie(): String? = runBlocking {
        try {
            context.dataStore.data.first()[SESSION_COOKIE_KEY]
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Save the current user ID for quick access
     */
    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = userId
        }
    }

    /**
     * Get the cached user ID
     */
    fun getUserId(): String? = runBlocking {
        try {
            context.dataStore.data.first()[USER_ID_KEY]
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if user has a session
     */
    fun hasSession(): Boolean = getSessionCookie() != null

    /**
     * Clear all session data on logout
     */
    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
