package com.longkd.simplemediarecord.playback

import android.content.Context
import android.media.AudioManager
import android.util.Log

class AudioPlayerManager(context: Context) {
    private val player = Player()
    private val audioFocusManager = AudioFocusManager(context)
    private val audioDeviceManager = AudioDeviceManager(context)

    private var isPlaying = false

    init {
        setupListeners()
    }

    fun startPlayback(audioFilePath: String) {
        if (audioFocusManager.requestAudioFocus()) {
            player.setDataSource(audioFilePath)
            player.setAudioAttributes()
            audioDeviceManager.setupAudioRouting()
            player.prepare()
            player.start()
            isPlaying = true
        } else {
            Log.e("AudioPlayerManager", "Cannot play - audio focus not granted")
        }
    }

    fun stopPlayback() {
        if (isPlaying) {
            player.stop()
            audioFocusManager.abandonAudioFocus()
            isPlaying = false
        }
    }

    fun release() {
        stopPlayback()
        player.release()
        audioDeviceManager.release()
    }

    private fun setupListeners() {
        audioFocusManager.setOnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    stopPlayback()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    player.pause()
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    if (isPlaying) {
                        player.start()
                    }
                }
            }
        }

        player.setOnCompletionListener {
            stopPlayback()
        }

        player.setOnErrorListener { _, _, _ ->
            stopPlayback()
            true
        }

        audioDeviceManager.setOnDeviceChangedListener { deviceType ->
            // Handle device routing changes
            Log.d("AudioPlayerManager", "Audio device changed: $deviceType")
        }
    }
}