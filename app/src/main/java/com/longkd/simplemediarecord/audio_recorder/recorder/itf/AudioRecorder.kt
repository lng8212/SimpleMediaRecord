package com.longkd.simplemediarecord.audio_recorder.recorder.itf

import com.longkd.simplemediarecord.audio_recorder.AudioManager

interface AudioRecorder : AudioManager {
    fun start()
    fun pause()
    fun resume()
    suspend fun stop()
}