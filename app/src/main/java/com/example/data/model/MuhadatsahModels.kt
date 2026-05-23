package com.example.data.model

import java.util.UUID

// Representation of a conversation lesson or topic
data class Topic(
    val id: String,
    val title: String,          // e.g., "At-Ta'aruf (Perkenalan)"
    val titleArabic: String,    // e.g., "التَّعَارُفُ"
    val description: String,     // e.g., "Belajar memperkenalkan diri dan menyapa teman baru"
    val category: String,       // e.g., "Dasar", "Sekolah", "Rumah"
    val dialogs: List<DialogLine>,
    val iconName: String        // Icon identifier
)

// Representation of a single line inside a conversation
data class DialogLine(
    val id: Int,
    val speaker: String,        // e.g., "Ahmad", "Thariq"
    val arabic: String,         // Arabic text
    val transliteration: String,// How to read it in Latin
    val translation: String,     // Indonesian translation
    val gender: String = "M"    // "M" or "F" for visual avatar representation
)

// Representation of an interactive vocabulary item
data class VocabularyItem(
    val id: String = UUID.randomUUID().toString(),
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val category: String,       // e.g., "Kata Benda", "Kata Kerja", "Keterangan"
    val topicContext: String,   // Reference to which topic it fits best
    val exampleArabic: String = "",
    val exampleTranslation: String = "",
    val isCustom: Boolean = false // If added by student
)
