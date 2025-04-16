package com.longkd.simplemediarecord.audio_recorder.playback

import com.longkd.simplemediarecord.audio_recorder.playback.itf.AudioDeviceHandler
import com.longkd.simplemediarecord.audio_recorder.playback.itf.AudioPlayer
import com.longkd.simplemediarecord.audio_recorder.playback.itf.AudioPlayerCallback
import javax.inject.Inject


class AudioPlayerController @Inject constructor(
    private val audioPlayer: AudioPlayer,
    private val audioDeviceHandler: AudioDeviceHandler
) {
    private var audioDeviceListener: ((String) -> Unit)? = null

    init {
        setupAudioDeviceListener()
    }

    fun loadAudio(filePath: String, listener: OnPreparedListener) {
        audioPlayer.loadAudio(filePath, object : AudioPlayerCallback {
            override fun onPrepared(duration: Int) {
                listener.onPrepared(duration)
            }

            override fun onError(error: String) {
                listener.onError(error)
            }
        })
    }

    fun play() = audioPlayer.play()
    fun pause() = audioPlayer.pause()
    fun stop() = audioPlayer.stop()
    fun seekTo(position: Int) = audioPlayer.seekTo(position)
    fun getCurrentPosition() = audioPlayer.getCurrentPosition()
    fun getDuration() = audioPlayer.getDuration()

    fun setOnCompletionListener(listener: () -> Unit) {
        audioPlayer.setOnCompletionListener(listener)
    }

    fun setOnAudioDeviceChangedListener(listener: (String) -> Unit) {
        audioDeviceListener = listener
    }

    private fun setupAudioDeviceListener() {
        audioDeviceHandler.setOnDeviceChangedListener { deviceType ->
            audioDeviceListener?.invoke(deviceType)
        }
    }

    fun getCurrentDevice(): String = audioDeviceHandler.getCurrentDevice()

    fun release() {
        audioPlayer.release()
    }

    interface OnPreparedListener {
        fun onPrepared(duration: Int)
        fun onError(error: String)
    }
}
