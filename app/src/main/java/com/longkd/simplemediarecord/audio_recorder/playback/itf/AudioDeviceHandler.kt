package com.longkd.simplemediarecord.audio_recorder.playback.itf

import com.longkd.simplemediarecord.audio_recorder.AudioManager

interface AudioDeviceHandler : AudioManager {
    fun setupAudioRouting()
    fun setOnDeviceChangedListener(listener: (String) -> Unit)
    fun getCurrentDevice(): String
}