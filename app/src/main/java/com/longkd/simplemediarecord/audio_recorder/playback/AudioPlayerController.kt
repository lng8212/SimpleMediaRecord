package com.longkd.simplemediarecord.audio_recorder.playback

import com.longkd.simplemediarecord.audio_recorder.model.AudioDevicePair
import com.longkd.simplemediarecord.audio_recorder.playback.itf.AudioDeviceHandler
import com.longkd.simplemediarecord.audio_recorder.playback.itf.AudioPlayer
import com.longkd.simplemediarecord.audio_recorder.playback.itf.AudioPlayerCallback
import javax.inject.Inject
import javax.inject.Named


class AudioPlayerController @Inject constructor(
    private val audioPlayer: AudioPlayer,
    @Named("output_handler") private val audioDeviceHandler: AudioDeviceHandler
) {
    private var audioDeviceListener: ((AudioDevicePair) -> Unit)? = null

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

    fun setOnAudioDeviceChangedListener(listener: (AudioDevicePair) -> Unit) {
        audioDeviceListener = listener
    }

    private fun setupAudioDeviceListener() {
        audioDeviceHandler.setOnDeviceChangedListener { deviceType ->
            audioDeviceListener?.invoke(deviceType)
        }
    }

    fun getCurrentDevice(): AudioDevicePair? = audioDeviceHandler.getCurrentDevice()

    fun getAvailableOutputDevices(): List<AudioDevicePair> {
        return audioDeviceHandler.getAvailableDevices()
    }

    fun selectAudioDevice(deviceId: Int): Boolean {
        return audioDeviceHandler.selectDevice(deviceId)
    }

    fun setOnDeviceListChangedListener(listener: (List<AudioDevicePair>) -> Unit) {
        audioDeviceHandler.setOnDeviceListChangedListener(listener)
    }

    fun resetDeviceSelection() {
        audioDeviceHandler.resetDeviceSelection()
    }

    fun release() {
        audioPlayer.release()
    }

    interface OnPreparedListener {
        fun onPrepared(duration: Int)
        fun onError(error: String)
    }
}