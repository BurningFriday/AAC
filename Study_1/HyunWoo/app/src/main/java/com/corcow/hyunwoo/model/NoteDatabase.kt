package com.corcow.hyunwoo.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Note::class], version = 1)
abstract class NoteDatabase: RoomDatabase() {
    abstract fun noteDao(): NoteDao

    // DB instance : inner_singleton
    companion object {
        private lateinit var instance: NoteDatabase

        @Synchronized
        fun getInstance(application: Context) : NoteDatabase {
            if (!::instance.isInitialized) {
                instance = Room.databaseBuilder(application, NoteDatabase::class.java,
                    "note_database")
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return instance
        }
    }
}
