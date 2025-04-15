package com.longkd.simplemediarecord.recorder

import android.content.Context
import android.util.Log
import com.longkd.simplemediarecord.recorder.receiver.InComingCallBroadcastReceiver
import com.longkd.simplemediarecord.recorder.receiver.LowBatteryBroadcastReceiver
import com.longkd.simplemediarecord.recorder.receiver.LowStorageBroadcastReceiver
import com.longkd.simplemediarecord.util.timer.Stopwatch
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRecorderController @Inject constructor(@ApplicationContext private val context: Context) {

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

    private lateinit var lowBatteryBroadcastReceiver: LowBatteryBroadcastReceiver
    private lateinit var lowStorageBroadcastReceiver: LowStorageBroadcastReceiver
    private lateinit var callBroadcastReceiver: InComingCallBroadcastReceiver

    fun getCurrentState() = _state.value

    fun initRecorder() {
        stopwatch = Stopwatch()
        stopwatch.setOnTickListener(object : Stopwatch.OnTickListener {
            override fun onTick(stopwatch: Stopwatch) {
                _timer.value = stopwatch.elapsedTime
            }
        })
        try {
            recorder = MediaRecorderInitializer(context = context)
        } catch (e: IOException) {
            Log.e("Recorder", "prepare() failed", e)
            _state.value = State.ERROR
        }
    }

    fun start() {
        recorder.start()
        _state.value = State.RECORDING
        stopwatch.start()
    }

    fun release() {
        context.unregisterReceiver(callBroadcastReceiver)
        context.unregisterReceiver(lowBatteryBroadcastReceiver)
        context.unregisterReceiver(lowStorageBroadcastReceiver)
    }

    suspend fun stop() {
        stopwatch.stop()
        coroutineScope {
            recorder.stop()
        }
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