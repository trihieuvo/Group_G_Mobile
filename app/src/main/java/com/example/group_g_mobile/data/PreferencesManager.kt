package com.example.group_g_mobile.data

import android.content.Context
// import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")



class PreferencesManager(private val context: Context) {
    companion object {
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    }

    val usernameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USERNAME] ?: "Guest"
    }

    val isDarkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DARK_MODE] ?: false
    }

    suspend fun saveUsername(username: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USERNAME] = username
        }
    }

    suspend fun saveDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DARK_MODE] = isDark
        }
    }
}


/* OLD SHAREDPREFERENCES IMPLEMENTATION
class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "app_preferences",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_USERNAME = "username"
        private const val KEY_DARK_MODE = "is_dark_mode"
    }

    fun getUsername(): String {
        return sharedPreferences.getString(KEY_USERNAME, "Guest") ?: "Guest"
    }

    fun saveUsername(username: String) {
        sharedPreferences.edit().putString(KEY_USERNAME, username).apply()
    }

    fun isDarkMode(): Boolean {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false)
    }

    fun saveDarkMode(isDark: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, isDark).apply()
    }
}
*/