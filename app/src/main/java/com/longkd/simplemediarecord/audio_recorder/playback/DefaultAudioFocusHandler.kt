package com.longkd.simplemediarecord.audio_recorder.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import com.longkd.simplemediarecord.audio_recorder.playback.itf.AudioFocusHandler

class DefaultAudioFocusHandler(context: Context) : AudioFocusHandler {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusChangeListener: ((Int) -> Unit)? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    private val audioFocusRequestOreo: AudioFocusRequest by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(afChangeListener)
                .build()
        } else {
            TODO("VERSION.SDK_INT < O")
        }
    }

    private val afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        audioFocusChangeListener?.invoke(focusChange)
    }

    override fun setOnAudioFocusChangeListener(listener: (Int) -> Unit) {
        audioFocusChangeListener = listener
    }

    override fun requestAudioFocus(): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = audioFocusRequestOreo
            audioManager.requestAudioFocus(audioFocusRequestOreo)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                afChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }

        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    override fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(afChangeListener)
        }
        audioFocusRequest = null
    }

    override fun release() {
        abandonAudioFocus()
    }
}