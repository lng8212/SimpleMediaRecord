package com.longkd.simplemediarecord.audio_recorder.recorder

import android.media.AudioManager
import android.util.Log
import com.longkd.simplemediarecord.audio_recorder.AudioFocusHandler
import com.longkd.simplemediarecord.audio_recorder.model.AudioDevicePair
import com.longkd.simplemediarecord.audio_recorder.recorder.itf.AudioRecorder
import com.longkd.simplemediarecord.audio_recorder.recorder.itf.AudioRecorderFactory
import com.longkd.simplemediarecord.audio_recorder.recorder.itf.TimerHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRecorderController @Inject constructor(
    private val recorderFactory: AudioRecorderFactory,
    private val audioFocusHandler: AudioFocusHandler,
    private val timerHandler: TimerHandler,
    private val audioInputDeviceHandler: AudioInputDeviceHandler
) {
    enum class State {
        PREPARING,
        RECORDING,
        PAUSED,
        ERROR,
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private lateinit var recorder: AudioRecorder

    private val _state = MutableStateFlow(State.PREPARING)
    val state: StateFlow<State>
        get() = _state

    private val _timer = MutableStateFlow(0L)
    val timer: StateFlow<Long>
        get() = _timer

    init {
        timerHandler.setOnTickListener { time ->
            _timer.value = time
        }

        audioFocusHandler.setOnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
                AudioManager.AUDIOFOCUS_LOSS -> {
                    scope.launch {
                        stop()
                    }
                }

                AudioManager.AUDIOFOCUS_GAIN -> resume()
            }
        }

    }

    fun setOnCurrentDeviceChange(listener: (AudioDevicePair) -> Unit) {
        audioInputDeviceHandler.setOnDeviceChangedListener(listener)
    }

    fun getCurrentState() = _state.value

    fun start() {
        try {
            if (audioFocusHandler.requestAudioFocus()) {
                recorder =
                    recorderFactory.createRecorder()
                recorder.start()
                _state.value = State.RECORDING
                timerHandler.start()
                audioInputDeviceHandler.updateTheCurrentMicrophone()
            } else {
                Log.d("Recorder", "start fail: cannot request audio focus")
            }

        } catch (e: Exception) {
            Log.e("Recorder", "prepare() failed", e)
            _state.value = State.ERROR
        }
    }

    suspend fun stop() {
        timerHandler.stop()
        audioFocusHandler.abandonAudioFocus()
        if (::recorder.isInitialized) {
            recorder.stop()
        }
        _state.value = State.PREPARING
    }

    private fun pause() {
        if (::recorder.isInitialized) {
            recorder.pause()
            timerHandler.pause()
            _state.value = State.PAUSED
        }
    }

    private fun resume() {
        if (::recorder.isInitialized) {
            recorder.resume()
            timerHandler.resume()
            _state.value = State.RECORDING
        }
    }

    fun toggleRecPause(): State {
        when (state.value) {
            State.RECORDING -> pause()
            State.PAUSED -> resume()
            else -> throw IllegalStateException()
        }
        return state.value
    }

    fun release() {
        scope.cancel()
        audioInputDeviceHandler.release()
        timerHandler.release()
        if (::recorder.isInitialized) {
            recorder.release()
        }
    }
}