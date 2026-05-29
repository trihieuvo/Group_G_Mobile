/*
package com.example.group_g_mobile.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "notes_demo.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_NOTES = "notes"
        const val COLUMN_ID = "id"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_TIMESTAMP = "timestamp"
        const val COLUMN_IS_SYNCED = "is_synced"
        const val COLUMN_SYNC_FAILED = "sync_failed"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NOTES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CONTENT TEXT NOT NULL,
                $COLUMN_TIMESTAMP INTEGER NOT NULL,
                $COLUMN_IS_SYNCED INTEGER NOT NULL DEFAULT 0,
                $COLUMN_SYNC_FAILED INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES")
        onCreate(db)
    }

   

    fun insertNote(content: String, timestamp: Long, isSynced: Boolean, syncFailed: Boolean): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CONTENT, content)
            put(COLUMN_TIMESTAMP, timestamp)
            put(COLUMN_IS_SYNCED, if (isSynced) 1 else 0)
            put(COLUMN_SYNC_FAILED, if (syncFailed) 1 else 0)
        }
        val id = db.insert(TABLE_NOTES, null, values)
        db.close()
        return id
    }

 
    fun getAllNotes(): List<Note> {
        val notesList = mutableListOf<Note>()
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_NOTES ORDER BY $COLUMN_TIMESTAMP DESC"
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                val isSynced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_SYNCED)) == 1
                val syncFailed = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SYNC_FAILED)) == 1

                notesList.add(Note(id, content, timestamp, isSynced, syncFailed))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return notesList
    }


    fun updateNoteSyncStatus(id: Int, isSynced: Boolean, syncFailed: Boolean) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_SYNCED, if (isSynced) 1 else 0)
            put(COLUMN_SYNC_FAILED, if (syncFailed) 1 else 0)
        }
        db.update(TABLE_NOTES, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    fun deleteNote(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_NOTES, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
    }
}
*/
