package com.longkd.simplemediarecord.audio_recorder.recorder

import android.content.Context
import com.longkd.simplemediarecord.audio_recorder.recorder.itf.AudioRecorder
import com.longkd.simplemediarecord.audio_recorder.recorder.itf.AudioRecorderFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DefaultAudioRecorderFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioFilePath: String
) : AudioRecorderFactory {
    override fun createRecorder(): AudioRecorder {
        return MediaRecorderImpl(context, audioFilePath)
    }
}