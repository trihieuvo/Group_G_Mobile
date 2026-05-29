package com.example.group_g_mobile.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteRepository(
    private val noteDao: NoteDao,
    private val api: MockServerApi,
    private val scope: CoroutineScope,
) {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    init {
        // Load initial data from Room
        loadNotesFromDb()
    }

    private fun loadNotesFromDb() {
        scope.launch(Dispatchers.IO) {
            val list = noteDao.getAllNotes().map { it.toNote() }
            _notes.value = list
        }
    }

    /**
     * Offline-first addition of a note.
     * Inserts into local database immediately, then triggers asynchronous sync to server.
     */
    suspend fun addNote(content: String) = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        SyncLogger.log("Local DB: Saving note to Room...")
        
        // 1. Save to local Room first (Offline-First)
        val id = noteDao.insertNote(
            NoteEntity(
                content = content,
                timestamp = timestamp,
                isSynced = false,
                syncFailed = false
            )
        )
        
        SyncLogger.log("Local DB: Saved note ID #$id locally (Status: Pending 🕒)")
        
        // Refresh local list immediately so the user sees the note in "Pending" state
        loadNotesFromDb()

        if (id != -1L) {
            // 2. Try to sync to the server in the background
            syncNoteToServer(id.toInt(), content, timestamp)
        }
    }

    /**
     * Tries to sync a specific note to the server.
     */
    private suspend fun syncNoteToServer(id: Int, content: String, timestamp: Long) = withContext(Dispatchers.IO) {
        try {
            SyncLogger.log("Repository: Uploading note ID #$id to server...")
            val success = api.uploadNote(content, timestamp)
            if (success) {
                // Update local DB: Synced successfully
                noteDao.updateNoteSyncStatus(id, isSynced = true, syncFailed = false)
                SyncLogger.log("Local DB: Updated Note ID #$id -> Synced (✔️)")
            } else {
                noteDao.updateNoteSyncStatus(id, isSynced = false, syncFailed = true)
                SyncLogger.log("Local DB: Updated Note ID #$id -> Sync Failed (❌)")
            }
        } catch (e: Exception) {
            // Update local DB: Sync failed (either due to network or server error)
            noteDao.updateNoteSyncStatus(id, isSynced = false, syncFailed = true)
            SyncLogger.log("Local DB: Note ID #$id upload failed: ${e.message}")
        } finally {
            // Refresh the local list to update UI with correct status icons
            loadNotesFromDb()
        }
    }

    /**
     * Scans Room for all unsynced notes and attempts to sync them.
     * Usually called when network status changes back to online.
     */
    suspend fun syncPendingNotes() = withContext(Dispatchers.IO) {
        val localNotes = noteDao.getAllNotes().map { it.toNote() }
        val pendingNotes = localNotes.filter { !it.isSynced }
        
        if (pendingNotes.isEmpty()) {
            SyncLogger.log("Repository: No pending notes to sync.")
            return@withContext
        }

        SyncLogger.log("Repository: Found ${pendingNotes.size} pending notes. Starting sync...")
        for (note in pendingNotes) {
            // Reset failure state to show it is attempting to sync again
            noteDao.updateNoteSyncStatus(note.id, isSynced = false, syncFailed = false)
            loadNotesFromDb()

            syncNoteToServer(note.id, note.content, note.timestamp)
        }
    }

    /**
     * Deletes a note locally.
     */
    suspend fun deleteNote(id: Int) = withContext(Dispatchers.IO) {
        noteDao.deleteNote(id)
        SyncLogger.log("Local DB: Deleted Note ID #$id")
        loadNotesFromDb()
    }
}
