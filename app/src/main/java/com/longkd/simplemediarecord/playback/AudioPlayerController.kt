package com.longkd.simplemediarecord.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class AudioPlayerController @Inject constructor(@ApplicationContext private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val audioFocusManager = AudioFocusManager(context)
    private val audioDeviceManager = AudioDeviceManager(context)

    private var audioDeviceListener: ((String) -> Unit)? = null
    private var completionListener: (() -> Unit)? = null

    init {
        setupAudioDeviceListener()
        audioFocusManager.setOnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
                AudioManager.AUDIOFOCUS_LOSS -> stop()
                AudioManager.AUDIOFOCUS_GAIN -> play()
            }
        }
    }

    interface OnPreparedListener {
        fun onPrepared(duration: Int)
        fun onError(error: String)
    }

    fun loadAudio(filePath: String, listener: OnPreparedListener) {
        try {
            releaseMediaPlayer()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )

                setOnPreparedListener {
                    listener.onPrepared(duration)
                    audioDeviceManager.setupAudioRouting()
                }

                setOnErrorListener { _, what, extra ->
                    listener.onError("Error code: $what, extra: $extra")
                    true
                }

                setOnCompletionListener {
                    completionListener?.invoke()
                }

                prepareAsync()
            }
        } catch (e: Exception) {
            listener.onError("Exception: ${e.message}")
        }
    }

    fun play() {
        if (audioFocusManager.requestAudioFocus()) {
            mediaPlayer?.start()
            audioDeviceManager.setupAudioRouting()
        }
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun stop() {
        mediaPlayer?.stop()
        audioFocusManager.abandonAudioFocus()
    }

    @Suppress("unused")
    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun setOnCompletionListener(listener: () -> Unit) {
        completionListener = listener
    }

    fun setOnAudioDeviceChangedListener(listener: (String) -> Unit) {
        audioDeviceListener = listener
    }

    private fun setupAudioDeviceListener() {
        audioDeviceManager.setOnDeviceChangedListener { deviceType ->
            audioDeviceListener?.invoke(deviceType)
        }
    }

    fun release() {
        stop()
        releaseMediaPlayer()
        audioDeviceManager.release()
        audioFocusManager.abandonAudioFocus()
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            reset()
            release()
        }
        mediaPlayer = null
    }
}