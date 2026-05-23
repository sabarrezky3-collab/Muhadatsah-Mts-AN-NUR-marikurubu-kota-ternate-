package com.example.ui.components

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class AudioPlayerManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    
    private val _isPlaying = MutableStateFlow<String?>(null) // Contains filepath of playing audio, or null
    val isPlaying: StateFlow<String?> = _isPlaying

    fun playAudio(filePath: String, onFinished: () -> Unit = {}) {
        stopAudio()

        val file = File(filePath)
        if (!file.exists()) {
            Log.e("AudioPlayerManager", "Audio File does not exist: $filePath")
            return
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.fromFile(file))
                prepare()
                start()
                _isPlaying.value = filePath
                setOnCompletionListener {
                    _isPlaying.value = null
                    onFinished()
                    release()
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Playback failed", e)
            _isPlaying.value = null
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "release/stop MediaPlayer failed", e)
        } finally {
            mediaPlayer = null
            _isPlaying.value = null
        }
    }

    fun release() {
        stopAudio()
    }
}
