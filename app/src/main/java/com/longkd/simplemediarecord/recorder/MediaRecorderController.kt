package com.longkd.simplemediarecord.recorder

import android.content.Context
import android.util.Log
import com.longkd.simplemediarecord.util.timer.Stopwatch
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRecorderController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioFilePath: String,
    private val callStateObserver: CallStateObserver
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

    private lateinit var stopwatch: Stopwatch

    val timer: StateFlow<Long>
        get() = _timer

    fun getCurrentState() = _state.value

    fun start() {
        if (!::stopwatch.isInitialized) {
            stopwatch = Stopwatch()
            stopwatch.setOnTickListener(object : Stopwatch.OnTickListener {
                override fun onTick(stopwatch: Stopwatch) {
                    _timer.value = stopwatch.elapsedTime
                }
            })
        }

        callStateObserver.startListening {
            if (getCurrentState() == State.RECORDING) {
                pause()
            }
        }

        try {
            recorder = MediaRecorderInitializer(context = context, audioFilePath = audioFilePath)
        } catch (e: IOException) {
            Log.e("Recorder", "prepare() failed", e)
            _state.value = State.ERROR
        }

        recorder.start()
        _state.value = State.RECORDING
        stopwatch.start()
    }

    suspend fun stop() {
        stopwatch.stop()
        callStateObserver.stopListening()
        coroutineScope {
            recorder.stop()
        }
        _state.value = State.PREPARING
    }

    private fun pause() {
        recorder.pause()
        stopwatch.pause()
        _state.value = State.PAUSED
    }

    private fun resume() {
        recorder.resume()
        stopwatch.resume()
        _state.value = State.RECORDING
    }

    fun toggleRecPause(): State {
        when (state.value) {
            State.RECORDING -> {
                pause()
            }

            State.PAUSED -> {
                resume()
            }

            else -> throw IllegalStateException()
        }
        return state.value
    }
}