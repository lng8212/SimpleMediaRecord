package com.longkd.simplemediarecord.audio_recorder.recorder.itf

import com.longkd.simplemediarecord.audio_recorder.AudioManager

interface CallStateHandler : AudioManager {
    fun startListening(onCallDetected: () -> Unit)
    fun stopListening()
}