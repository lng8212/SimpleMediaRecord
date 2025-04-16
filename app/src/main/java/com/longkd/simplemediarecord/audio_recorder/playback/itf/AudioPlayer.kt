package com.longkd.simplemediarecord.audio_recorder.playback.itf

import com.longkd.simplemediarecord.audio_recorder.AudioManager

interface AudioPlayer : AudioManager {
    fun loadAudio(filePath: String, listener: AudioPlayerCallback)
    fun play()
    fun pause()
    fun stop()
    fun seekTo(position: Int)
    fun getCurrentPosition(): Int
    fun getDuration(): Int
    fun setOnCompletionListener(listener: () -> Unit)
}