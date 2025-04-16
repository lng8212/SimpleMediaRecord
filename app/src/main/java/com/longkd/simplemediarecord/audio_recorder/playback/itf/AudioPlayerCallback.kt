package com.longkd.simplemediarecord.audio_recorder.playback.itf

interface AudioPlayerCallback {
    fun onPrepared(duration: Int)
    fun onError(error: String)
}