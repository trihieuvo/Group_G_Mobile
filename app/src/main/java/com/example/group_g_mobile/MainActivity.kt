package com.example.group_g_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.group_g_mobile.data.*
import com.example.group_g_mobile.ui.LocalDemoApp
import com.example.group_g_mobile.ui.theme.Group_G_MobileTheme

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var fileManager: InternalFileManager
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var mockServerApi: MockServerApi
    private lateinit var noteRepository: NoteRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Storage Managers & Data Layers
        preferencesManager = PreferencesManager(applicationContext)
        fileManager = InternalFileManager(applicationContext)
        dbHelper = DatabaseHelper(applicationContext)
        mockServerApi = MockServerApi()
        noteRepository = NoteRepository(dbHelper, mockServerApi, lifecycleScope)

        // Initialize logging
        SyncLogger.clear()
        SyncLogger.log("SYSTEM: Application started.")
        SyncLogger.log("Key-Value: Loaded SharedPreferences.")
        SyncLogger.log("File System: Ready.")
        SyncLogger.log("Database: SQLite database connection opened.")

        enableEdgeToEdge()
        
        setContent {
            var isDarkTheme by remember { 
                mutableStateOf(preferencesManager.isDarkMode()) 
            }

            Group_G_MobileTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                LocalDemoApp(
                    preferencesManager = preferencesManager,
                    fileManager = fileManager,
                    repository = noteRepository,
                    api = mockServerApi,
                    isDarkTheme = isDarkTheme,
                    onThemeChange = { isDark -> isDarkTheme = isDark }
                )
            }
        }
    }
}