package com.longkd.simplemediarecord.playback

import android.media.AudioAttributes
import android.media.MediaPlayer

class Player {
    private var mediaPlayer: MediaPlayer? = null
    private var onCompletionListener: (() -> Unit)? = null
    private var onErrorListener: ((MediaPlayer, Int, Int) -> Boolean)? = null

    fun setDataSource(path: String) {
        releaseMediaPlayer()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(path)
        }
    }

    fun setAudioAttributes() {
        mediaPlayer?.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
    }

    fun prepare() {
        mediaPlayer?.prepare()
    }

    fun start() {
        mediaPlayer?.start()
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun stop() {
        mediaPlayer?.stop()
    }

    fun release() {
        releaseMediaPlayer()
    }

    fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
        mediaPlayer?.setOnCompletionListener {
            onCompletionListener?.invoke()
        }
    }

    fun setOnErrorListener(listener: (MediaPlayer, Int, Int) -> Boolean) {
        onErrorListener = listener
        mediaPlayer?.setOnErrorListener { mp, what, extra ->
            onErrorListener?.invoke(mp, what, extra) ?: false
        }
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