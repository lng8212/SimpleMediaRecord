package com.longkd.simplemediarecord.util.timer

import android.os.Handler
import android.util.Log
import android.widget.TextView
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.LinkedList

class Stopwatch {

    private val splits = LinkedList<Split>()
    private var textView: TextView? = null
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

    fun setDebugMode(debugMode: Boolean) {
        logEnabled = debugMode
    }

    fun setTextView(textView: TextView?) {
        this.textView = textView
    }

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

    fun split() {
        require(isStarted) { "Not Started" }
        val split = Split(elapsedTime, lapTime)
        lapTime = 0
        logSplit(split)
        splits.add(split)
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
        textView?.text = getFormattedTime(elapsedTime)
    }

    private fun logSplit(split: Split) {
        if (logEnabled) {
            Log.d("STOPWATCH", "split at ${split.splitTime}. Lap = ${split.lapTime}")
        }
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
        splits.clear()
    }

    interface OnTickListener {
        fun onTick(stopwatch: Stopwatch)
    }

    companion object {
        fun getFormattedTime(elapsedTime: Long): String {
            val milliseconds = ((elapsedTime % 1000) / 10).toInt()
            val seconds = ((elapsedTime / 1000) % 60).toInt()
            val minutes = (elapsedTime / (60 * 1000) % 60).toInt()
            val hours = (elapsedTime / (60 * 60 * 1000)).toInt()

            val f: NumberFormat = DecimalFormat("00")
            return buildString {
                when {
                    minutes == 0 -> append(f.format(seconds)).append('.')
                        .append(f.format(milliseconds))

                    hours == 0 -> append(f.format(minutes)).append(":")
                        .append(f.format(seconds)).append('.').append(f.format(milliseconds))

                    else -> append(hours).append(":").append(f.format(minutes)).append(":")
                        .append(f.format(seconds))
                }
            }
        }
    }
}
