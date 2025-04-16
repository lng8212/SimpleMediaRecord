package com.longkd.simplemediarecord.audio_recorder.recorder

import android.util.Log
import com.longkd.simplemediarecord.audio_recorder.recorder.itf.AudioRecorder
import com.longkd.simplemediarecord.audio_recorder.recorder.itf.AudioRecorderFactory
import com.longkd.simplemediarecord.audio_recorder.recorder.itf.CallStateHandler
import com.longkd.simplemediarecord.audio_recorder.recorder.itf.TimerHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRecorderController @Inject constructor(
    private val recorderFactory: AudioRecorderFactory,
    private val callStateHandler: CallStateHandler,
    private val timerHandler: TimerHandler
) {
    enum class State {
        PREPARING,
        RECORDING,
        PAUSED,
        ERROR,
    }

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
    }

    fun getCurrentState() = _state.value

    fun start() {
        callStateHandler.startListening {
            if (getCurrentState() == State.RECORDING) {
                pause()
            }
        }

        try {
            recorder = recorderFactory.createRecorder()
            recorder.start()
            _state.value = State.RECORDING
            timerHandler.start()
        } catch (e: Exception) {
            Log.e("Recorder", "prepare() failed", e)
            _state.value = State.ERROR
        }
    }

    suspend fun stop() {
        timerHandler.stop()
        callStateHandler.stopListening()
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
        callStateHandler.release()
        timerHandler.release()
        if (::recorder.isInitialized) {
            recorder.release()
        }
    }
}