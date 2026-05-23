package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MuhadatsahDatabase
import com.example.data.local.SavedRecording
import com.example.data.model.DialogLine
import com.example.data.model.Topic
import com.example.data.model.VocabularyItem
import com.example.data.repository.MuhadatsahRepository
import com.example.ui.components.AudioPlayerManager
import com.example.ui.components.AudioRecorderManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class MuhadatsahViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MuhadatsahDatabase.getDatabase(application)
    private val repository = MuhadatsahRepository(
        recordingDao = database.savedRecordingDao(),
        favoriteWordDao = database.favoriteWordDao(),
        customVocabularyDao = database.customVocabularyDao()
    )

    // Hardware Managers
    private val audioRecorderManager = AudioRecorderManager(application)
    private val audioPlayerManager = AudioPlayerManager(application)
    
    private var _arabicTtsManager: com.example.ui.components.ArabicTTSManager? = null
    
    @Synchronized
    private fun getTtsManager(): com.example.ui.components.ArabicTTSManager {
        return _arabicTtsManager ?: com.example.ui.components.ArabicTTSManager(getApplication()).also {
            _arabicTtsManager = it
        }
    }

    // Expose lists of predefined lessons
    val topics: List<Topic> = repository.topics

    // Selected Muhadatsah Topic
    private val _selectedTopic = MutableStateFlow<Topic>(topics.first())
    val selectedTopic: StateFlow<Topic> = _selectedTopic.asStateFlow()

    // Recording States
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingLineId = MutableStateFlow<Int?>(null) // null if not recording, otherwise the DialogLine ID
    val recordingLineId: StateFlow<Int?> = _recordingLineId.asStateFlow()

    // Live Pronunciation Assessment State
    private val _latestEvaluationResult = MutableStateFlow<com.example.ui.components.PronunciationResult?>(null)
    val latestEvaluationResult: StateFlow<com.example.ui.components.PronunciationResult?> = _latestEvaluationResult.asStateFlow()

    // Playback active tracker
    val playingFilePath: StateFlow<String?> = audioPlayerManager.isPlaying

    // Dictionary Query states
    private val _dictionaryQuery = MutableStateFlow("")
    val dictionaryQuery: StateFlow<String> = _dictionaryQuery.asStateFlow()

    private val _selectedDictionaryCategory = MutableStateFlow("Semua")
    val selectedDictionaryCategory: StateFlow<String> = _selectedDictionaryCategory.asStateFlow()

    private val _isFavoriteOnly = MutableStateFlow(false)
    val isFavoriteOnly: StateFlow<Boolean> = _isFavoriteOnly.asStateFlow()

    // Saved recordings from Database
    val savedRecordings: StateFlow<List<SavedRecording>> = repository.savedRecordings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Favorite Word IDs Set
    val favoriteWordIds: StateFlow<Set<String>> = repository.favoriteWordIds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    // Filtered Dictionary Flow
    val filteredDictionary: StateFlow<List<VocabularyItem>> = combine(
        repository.dictionary,
        _dictionaryQuery,
        _selectedDictionaryCategory,
        favoriteWordIds,
        _isFavoriteOnly
    ) { dict, query, category, favs, isFavOnly ->
        dict.filter { item ->
            // Search criteria
            val matchesQuery = query.isEmpty() || 
                item.arabic.contains(query, ignoreCase = true) ||
                item.transliteration.contains(query, ignoreCase = true) ||
                item.translation.contains(query, ignoreCase = true)

            // Category criteria
            val matchesCategory = category == "Semua" || 
                (category == "Sekolah" && item.category == "Sekolah") ||
                (category == "Rumah" && item.category == "Rumah") ||
                (category == "Harian" && item.category == "Harian") ||
                (category == "Kata Kerja" && item.category == "Kata Kerja") ||
                (category == "Ditambahkan" && item.isCustom)

            // Favorite criteria
            val matchesFav = !isFavOnly || favs.contains(item.id)

            matchesQuery && matchesCategory && matchesFav
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Selected Practice Voice File Temp Path
    private var tempRecordingFile: File? = null
    private var recordingStartTime: Long = 0

    // ----------------------------------------------------
    // Operations & Actions
    // ----------------------------------------------------

    fun selectTopic(topicId: String) {
        val topic = topics.find { it.id == topicId }
        if (topic != null) {
            _selectedTopic.value = topic
        }
    }

    // Toggle favorite state
    fun toggleFavorite(item: VocabularyItem) {
        viewModelScope.launch {
            val isCurrentFav = favoriteWordIds.value.contains(item.id)
            repository.toggleFavorite(item.id, isCurrentFav)
        }
    }

    // Add custom word to local dictionary
    fun addCustomWord(arabic: String, transliteration: String, translation: String, category: String) {
        viewModelScope.launch {
            repository.addCustomVocabulary(
                arabic = arabic,
                transliteration = transliteration,
                translation = translation,
                category = category
            )
        }
    }

    // Delete custom vocabulary
    fun deleteCustomWord(id: String) {
        viewModelScope.launch {
            repository.deleteCustomWord(id)
        }
    }

    // Start Audio Recording for a Dialogue Line
    fun startRecording(line: DialogLine) {
        if (_isRecording.value) return
        
        val topic = _selectedTopic.value
        val recordName = "practice_${topic.id}_line_${line.id}"
        
        tempRecordingFile = audioRecorderManager.startRecording(recordName)
        if (tempRecordingFile != null) {
            _isRecording.value = true
            _recordingLineId.value = line.id
            recordingStartTime = System.currentTimeMillis()
        }
    }

    // Stop and save Dialogue audio recording
    fun stopAndSaveRecording(line: DialogLine) = viewModelScope.launch {
        if (!_isRecording.value || _recordingLineId.value != line.id) return@launch
        
        _isRecording.value = false
        _recordingLineId.value = null
        
        val savedFile = audioRecorderManager.stopRecording()
        val duration = System.currentTimeMillis() - recordingStartTime

        if (savedFile != null && savedFile.exists() && duration > 500) {
            val topic = selectedTopic.value
            
            // Execute interactive pronunciation criteria check
            val evalResult = com.example.ui.components.PronunciationEngine.evaluate(
                targetText = line.arabic,
                durationMs = duration,
                audioFile = savedFile
            )
            
            _latestEvaluationResult.value = evalResult

            repository.saveVoiceRecording(
                topicId = topic.id,
                dialogId = line.id,
                topicTitle = topic.title,
                filePath = savedFile.absolutePath,
                durationMs = duration,
                speakerName = line.speaker,
                arabicText = line.arabic,
                translationText = line.translation,
                accuracyScore = evalResult.score,
                feedbackText = evalResult.feedback,
                pronouncedText = evalResult.pronouncedText
            )
            Log.d("MuhadatsahViewModel", "Recording evaluated and saved with score ${evalResult.score}: ${savedFile.absolutePath}")
        } else {
            // Delete corrupt files
            savedFile?.delete()
            Log.e("MuhadatsahViewModel", "Recording discarded: duration too short or file doesn't exist.")
        }
        tempRecordingFile = null
    }

    // Reset latest evaluation state
    fun clearLatestEvaluation() {
        _latestEvaluationResult.value = null
    }

    // Play standard Arabic recitation via Text-to-Speech
    fun speakStandardArabic(text: String) {
        getTtsManager().speak(text)
    }

    // Stop standard Arabic recitation
    fun stopStandardArabic() {
        _arabicTtsManager?.stop()
    }

    // Discard Recording (or emergency cancel)
    fun cancelRecording() {
        if (!_isRecording.value) return
        _isRecording.value = false
        _recordingLineId.value = null
        audioRecorderManager.stopRecording()
        tempRecordingFile?.delete()
        tempRecordingFile = null
    }

    // Play specific recorded file
    fun playRecording(filePath: String) {
        audioPlayerManager.playAudio(filePath)
    }

    // Stop player
    fun stopPlayback() {
        audioPlayerManager.stopAudio()
    }

    // Delete Recording completely
    fun deleteRecording(recordingId: Int, filePath: String) {
        viewModelScope.launch {
            repository.deleteRecording(recordingId)
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    // Set filters for the dictionary
    fun setDictionaryQuery(query: String) {
        _dictionaryQuery.value = query
    }

    fun setDictionaryCategory(category: String) {
        _selectedDictionaryCategory.value = category
    }

    fun toggleFavoriteOnlyFilter() {
        _isFavoriteOnly.value = !_isFavoriteOnly.value
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayerManager.release()
        audioRecorderManager.release()
        _arabicTtsManager?.release()
    }
}
