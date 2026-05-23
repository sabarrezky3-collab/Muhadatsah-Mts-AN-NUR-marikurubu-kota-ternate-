package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_recordings")
data class SavedRecording(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val topicId: String,
    val dialogId: Int, // Line number or -1 for total topic practice
    val topicTitle: String,
    val filePath: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMs: Long = 0,
    val speakerName: String,
    val arabicText: String,
    val translationText: String,
    val accuracyScore: Int = 0,
    val feedbackText: String = "",
    val pronouncedText: String = ""
)

@Entity(tableName = "favorite_words")
data class FavoriteWord(
    @PrimaryKey val wordId: String
)

@Entity(tableName = "custom_vocabularies")
data class CustomVocabulary(
    @PrimaryKey val id: String,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)
