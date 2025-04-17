package com.longkd.simplemediarecord.audio_recorder

interface AudioFocusHandler : AudioManager {
    fun requestAudioFocus(): Boolean
    fun abandonAudioFocus()
    fun setOnAudioFocusChangeListener(listener: (Int) -> Unit)
}