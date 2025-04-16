package com.longkd.simplemediarecord.audio_recorder.recorder.itf

interface AudioRecorderFactory {
    fun createRecorder(): AudioRecorder
}