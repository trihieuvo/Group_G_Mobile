package com.example.group_g_mobile.data

data class UserProfile(
    val username: String,
    val isDarkMode: Boolean,
    val avatarPath: String?
)

data class Note(
    val id: Int = 0,
    val content: String,
    val timestamp: Long,
    val isSynced: Boolean,
    val syncFailed: Boolean = false
)
