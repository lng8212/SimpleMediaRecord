package com.longkd.simplemediarecord.util.timer

import android.os.Handler
import android.util.Log

class Stopwatch {

    private var current: Long = System.currentTimeMillis()
    internal var start: Long = current
    private set
    private var lapTime: Long = 0
    internal var elapsedTime: Long = 0
        private set
    internal var isStarted = false
        private set
    internal var isPaused = false
        private set
    private var logEnabled = false
    private var onTickListener: OnTickListener? = null
    private val handler = Handler()

    var clockDelay: Long = 100
    private val runnable = Runnable { run() }

    fun setOnTickListener(onTickListener: OnTickListener) {
        this.onTickListener = onTickListener
    }

    fun start() {
        require(!isStarted) { "Already Started" }
        resetStopwatch()
        handler.post(runnable)
    }

    fun stop() {
        require(isStarted) { "Not Started" }
        updateElapsed(System.currentTimeMillis())
        isStarted = false
        isPaused = false
        handler.removeCallbacks(runnable)
    }

    fun pause() {
        require(isStarted) { "Not Started" }
        require(!isPaused) { "Already Paused" }
        updateElapsed(System.currentTimeMillis())
        isPaused = true
        handler.removeCallbacks(runnable)
    }

    fun resume() {
        require(isStarted) { "Not Started" }
        require(isPaused) { "Not Paused" }
        isPaused = false
        current = System.currentTimeMillis()
        handler.post(runnable)
    }

    private fun updateElapsed(time: Long) {
        elapsedTime += time - current
        lapTime += time - current
        current = time
    }

    private fun run() {
        if (!isStarted || isPaused) {
            handler.removeCallbacks(runnable)
            return
        }
        updateElapsed(System.currentTimeMillis())
        handler.postDelayed(runnable, clockDelay)

        logElapsedTime()
        onTickListener?.onTick(this)
    }

    private fun logElapsedTime() {
        if (logEnabled) {
            Log.d("STOPWATCH", "${elapsedTime / 1000} seconds, ${elapsedTime % 1000} milliseconds")
        }
    }

    private fun resetStopwatch() {
        isStarted = true
        isPaused = false
        start = System.currentTimeMillis()
        current = start
        lapTime = 0
        elapsedTime = 0
    }

    interface OnTickListener {
        fun onTick(stopwatch: Stopwatch)
    }
}
