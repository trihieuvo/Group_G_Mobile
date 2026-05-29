package com.example.group_g_mobile.data

import androidx.room.*

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    suspend fun getAllNotes(): List<NoteEntity>

    @Query("UPDATE notes SET is_synced = :isSynced, sync_failed = :syncFailed WHERE id = :id")
    suspend fun updateNoteSyncStatus(id: Int, isSynced: Boolean, syncFailed: Boolean): Int

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: Int): Int
}
