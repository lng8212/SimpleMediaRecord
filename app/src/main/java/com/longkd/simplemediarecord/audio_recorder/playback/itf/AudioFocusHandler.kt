package com.longkd.simplemediarecord.audio_recorder.playback.itf

import com.longkd.simplemediarecord.audio_recorder.AudioManager

interface AudioFocusHandler : AudioManager {
    fun requestAudioFocus(): Boolean
    fun abandonAudioFocus()
    fun setOnAudioFocusChangeListener(listener: (Int) -> Unit)
}