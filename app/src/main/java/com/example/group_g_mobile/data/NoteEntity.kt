package com.example.group_g_mobile.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    
    @ColumnInfo(name = "content")
    val content: String,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean,
    
    @ColumnInfo(name = "sync_failed")
    val syncFailed: Boolean = false,
)

fun NoteEntity.toNote(): Note {
    return Note(
        id = this.id,
        content = this.content,
        timestamp = this.timestamp,
        isSynced = this.isSynced,
        syncFailed = this.syncFailed
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = this.id,
        content = this.content,
        timestamp = this.timestamp,
        isSynced = this.isSynced,
        syncFailed = this.syncFailed
    )
}
