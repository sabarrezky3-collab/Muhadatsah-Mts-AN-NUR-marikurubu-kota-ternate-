package com.example.ui.components

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecorderManager(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentFile: File? = null

    fun startRecording(fileName: String): File? {
        stopRecording() // ensure any active recording is cleaned up

        val outputDir = context.cacheDir
        val audioFile = try {
            File.createTempFile(fileName, ".3gp", outputDir)
        } catch (e: IOException) {
            Log.e("AudioRecorderManager", "Failed to create temp audio file", e)
            return null
        }

        currentFile = audioFile

        try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
                Log.d("AudioRecorderManager", "Recording started: ${audioFile.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e("AudioRecorderManager", "Failed to initialize MediaRecorder", e)
            mediaRecorder = null
            return null
        }
        return audioFile
    }

    fun stopRecording(): File? {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorderManager", "stop() failed or recorder was not active", e)
        } finally {
            mediaRecorder = null
        }
        return currentFile
    }

    fun release() {
        stopRecording()
    }
}
