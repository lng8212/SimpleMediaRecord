package com.longkd.simplemediarecord.audio_recorder.playback

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import com.longkd.simplemediarecord.audio_recorder.AudioFocusHandler
import com.longkd.simplemediarecord.audio_recorder.playback.itf.AudioDeviceHandler
import com.longkd.simplemediarecord.audio_recorder.playback.itf.AudioPlayer
import com.longkd.simplemediarecord.audio_recorder.playback.itf.AudioPlayerCallback
import javax.inject.Inject

class DefaultAudioPlayer @Inject constructor(
    private val audioFocusHandler: AudioFocusHandler,
    private val audioDeviceHandler: AudioDeviceHandler
) : AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var completionListener: (() -> Unit)? = null

    init {
        audioFocusHandler.setOnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
                AudioManager.AUDIOFOCUS_LOSS -> stop()
                AudioManager.AUDIOFOCUS_GAIN -> play()
            }
        }
    }

    override fun loadAudio(filePath: String, listener: AudioPlayerCallback) {
        try {
            releaseMediaPlayer()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )

                setOnPreparedListener {
                    listener.onPrepared(duration)
                    audioDeviceHandler.setupAudioRouting()
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

    override fun play() {
        if (audioFocusHandler.requestAudioFocus()) {
            mediaPlayer?.start()
            audioDeviceHandler.setupAudioRouting()
        }
    }

    override fun pause() {
        mediaPlayer?.pause()
    }

    override fun stop() {
        mediaPlayer?.stop()
        audioFocusHandler.abandonAudioFocus()
    }

    override fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    override fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        completionListener = listener
    }

    override fun release() {
        stop()
        releaseMediaPlayer()
        audioDeviceHandler.release()
        audioFocusHandler.release()
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