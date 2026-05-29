package com.example.group_g_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.group_g_mobile.data.*
import com.example.group_g_mobile.ui.LocalDemoApp
import com.example.group_g_mobile.ui.theme.Group_G_MobileTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var fileManager: InternalFileManager
    private lateinit var appDatabase: AppDatabase
    private lateinit var noteDao: NoteDao
    private lateinit var mockServerApi: MockServerApi
    private lateinit var noteRepository: NoteRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Storage Managers & Data Layers
        preferencesManager = PreferencesManager(applicationContext)
        fileManager = InternalFileManager(applicationContext)
        appDatabase = AppDatabase.getDatabase(applicationContext)
        noteDao = appDatabase.noteDao()
        mockServerApi = MockServerApi()
        noteRepository = NoteRepository(noteDao, mockServerApi, lifecycleScope)

        // Initialize logging
        SyncLogger.clear()
        SyncLogger.log("SYSTEM: Application started.")
        SyncLogger.log("Key-Value: Loaded Preferences DataStore.")
        SyncLogger.log("File System: Ready.")
        SyncLogger.log("Database: Room database connection opened.")

        enableEdgeToEdge()
        
        setContent {
            val isDarkTheme by preferencesManager.isDarkModeFlow.collectAsState(initial = false)

            Group_G_MobileTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                LocalDemoApp(
                    preferencesManager = preferencesManager,
                    fileManager = fileManager,
                    repository = noteRepository,
                    api = mockServerApi,
                    isDarkTheme = isDarkTheme,
                    onThemeChange = { isDark ->
                        lifecycleScope.launch {
                            preferencesManager.saveDarkMode(isDark)
                        }
                    }
                )
            }
        }
    }
}