package com.longkd.simplemediarecord.audio_recorder.recorder

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.longkd.simplemediarecord.audio_recorder.recorder.itf.TimerHandler

class DefaultTimerHandler : TimerHandler {
    private var elapsedTime: Long = 0L
    private var startTime: Long = 0L
    private var isRunning = false
    private var tickListener: ((Long) -> Unit)? = null
    private var handler: Handler? = null
    private var ticker: Runnable? = null

    override fun start() {
        if (isRunning) return

        startTime = SystemClock.elapsedRealtime() - elapsedTime
        isRunning = true

        handler = Handler(Looper.getMainLooper())
        ticker = object : Runnable {
            override fun run() {
                if (isRunning) {
                    elapsedTime = SystemClock.elapsedRealtime() - startTime
                    tickListener?.invoke(elapsedTime)
                    handler?.postDelayed(this, 100)
                }
            }
        }
        handler?.post(ticker!!)
    }

    override fun pause() {
        if (!isRunning) return

        isRunning = false
        handler?.removeCallbacks(ticker!!)
    }

    override fun resume() {
        if (isRunning) return

        startTime = SystemClock.elapsedRealtime() - elapsedTime
        isRunning = true
        handler?.post(ticker!!)
    }

    override fun stop() {
        isRunning = false
        handler?.removeCallbacks(ticker!!)
        elapsedTime = 0L
    }

    override fun getElapsedTime(): Long = elapsedTime

    override fun setOnTickListener(listener: (Long) -> Unit) {
        tickListener = listener
    }

    override fun release() {
        isRunning = false
        handler?.removeCallbacks(ticker!!)
        handler = null
        ticker = null
    }
}