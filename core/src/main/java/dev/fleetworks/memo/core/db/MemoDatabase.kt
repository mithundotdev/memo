package dev.fleetworks.memo.core.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [NoteEntity::class], version = 1, exportSchema = false)
abstract class MemoDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        const val FTS_TABLE = "notes_fts"

        private fun ensureFts(db: SupportSQLiteDatabase) {
            try {
                db.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS $FTS_TABLE USING fts5(noteId UNINDEXED, title, body, tokenize='unicode61')")
            } catch (_: Exception) {
            }
        }

        fun create(context: Context): MemoDatabase =
            Room.databaseBuilder(context, MemoDatabase::class.java, "memo.db")
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        ensureFts(db)
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        ensureFts(db)
                    }
                })
                .build()
    }
}
