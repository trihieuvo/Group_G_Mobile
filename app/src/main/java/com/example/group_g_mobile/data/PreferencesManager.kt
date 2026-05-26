package com.example.group_g_mobile.data

import android.content.Context
import android.content.SharedPreferences

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
