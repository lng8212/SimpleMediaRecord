package com.longkd.simplemediarecord.audio_recorder.recorder.itf

import com.longkd.simplemediarecord.audio_recorder.AudioManager

interface TimerHandler : AudioManager {
    fun start()
    fun pause()
    fun resume()
    fun stop()
    fun getElapsedTime(): Long
    fun setOnTickListener(listener: (Long) -> Unit)
}