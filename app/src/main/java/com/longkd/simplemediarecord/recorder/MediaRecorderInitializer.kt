package com.longkd.simplemediarecord.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.IOException

class MediaRecorderInitializer @Throws(IOException::class) constructor(
    context: Context,
) : AudioRecorder {

    val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
    } else {
        MediaRecorder()
    }.apply {
        setAudioSource(MediaRecorder.AudioSource.DEFAULT)
        setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        setOutputFile(context.filesDir.absolutePath + "/demo.3gp")

        prepare()
    }

    override fun start() {
        recorder.start()
    }

    override fun pause() {
        recorder.pause()
    }

    override fun resume() {
        recorder.resume()
    }

    override suspend fun stop() {
        recorder.apply {
            stop()
            release()
        }
    }
}