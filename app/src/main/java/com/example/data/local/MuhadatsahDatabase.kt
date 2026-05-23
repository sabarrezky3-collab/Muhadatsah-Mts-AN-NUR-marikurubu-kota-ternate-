package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SavedRecording::class, FavoriteWord::class, CustomVocabulary::class],
    version = 3,
    exportSchema = false
)
abstract class MuhadatsahDatabase : RoomDatabase() {
    abstract fun savedRecordingDao(): SavedRecordingDao
    abstract fun favoriteWordDao(): FavoriteWordDao
    abstract fun customVocabularyDao(): CustomVocabularyDao

    companion object {
        @Volatile
        private var INSTANCE: MuhadatsahDatabase? = null

        fun getDatabase(context: Context): MuhadatsahDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MuhadatsahDatabase::class.java,
                    "muhadatsah_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
