package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedRecordingDao {
    @Query("SELECT * FROM saved_recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<SavedRecording>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: SavedRecording)

    @Query("DELETE FROM saved_recordings WHERE id = :id")
    suspend fun deleteRecordingById(id: Int)
}

@Dao
interface FavoriteWordDao {
    @Query("SELECT * FROM favorite_words")
    fun getAllFavorites(): Flow<List<FavoriteWord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(fav: FavoriteWord)

    @Delete
    suspend fun deleteFavorite(fav: FavoriteWord)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_words WHERE wordId = :id)")
    fun isFavorite(id: String): Flow<Boolean>
}

@Dao
interface CustomVocabularyDao {
    @Query("SELECT * FROM custom_vocabularies ORDER BY timestamp DESC")
    fun getAllCustomWords(): Flow<List<CustomVocabulary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomWord(word: CustomVocabulary)

    @Query("DELETE FROM custom_vocabularies WHERE id = :id")
    suspend fun deleteCustomWordById(id: String)
}
