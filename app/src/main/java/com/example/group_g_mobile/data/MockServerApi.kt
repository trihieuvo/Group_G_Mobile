package com.example.group_g_mobile.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

class MockServerApi {

    // States that can be modified via UI to simulate real-world conditions
    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

    private val _shouldServerFail = MutableStateFlow(false)
    val shouldServerFail: StateFlow<Boolean> = _shouldServerFail.asStateFlow()

    fun setNetworkAvailable(available: Boolean) {
        _isNetworkAvailable.value = available
    }

    fun setServerFail(fail: Boolean) {
        _shouldServerFail.value = fail
    }

    /**
     * Simulates sending a note to the server.
     * Throws exceptions depending on mock configuration to demonstrate offline/error handling.
     */
    suspend fun uploadNote(content: String, timestamp: Long): Boolean {
        SyncLogger.log("API: Connecting to server (Time: $timestamp)...")
        // Simulate network delay
        delay(1500)

        if (!_isNetworkAvailable.value) {
            SyncLogger.log("API Error: No network connection.")
            throw IOException("No internet connection available")
        }

        if (_shouldServerFail.value) {
            SyncLogger.log("API Error 500: Server simulated failure.")
            throw RuntimeException("Server error (500): Internal server error")
        }

        SyncLogger.log("API Success: Uploaded content: \"$content\"")
        // Simulates successful upload
        return true
    }
}
