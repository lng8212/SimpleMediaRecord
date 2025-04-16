package com.longkd.simplemediarecord.recorder

interface AudioRecorder {
    fun start()
    fun pause()
    fun resume()
    suspend fun stop()
}