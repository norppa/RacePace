package com.ducksoup.racepace

import android.os.Handler
import android.os.Looper

class Timer {

    private var time = 0
    private var tick: (time: Int) -> Unit = {}
    private val handler = Handler(Looper.getMainLooper())
    private val timer: Runnable = object : Runnable {
        override fun run() {
            time += 1
            tick(time)
            handler.postDelayed(this, 1000)
        }
    }

    fun start() {
        time = 0
        handler.postDelayed(timer, 1000)
    }

    fun stop() {
        handler.removeCallbacks(timer)
    }

    fun setTick(callback: (time: Int) -> Unit) {
        tick = callback
    }
}