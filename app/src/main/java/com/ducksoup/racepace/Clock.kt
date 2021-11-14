package com.ducksoup.racepace

import android.os.Handler
import android.os.Looper

class Clock(val callback: (seconds: Int) -> Unit) {
    private var totalTime = 0
    private val handler = Handler(Looper.getMainLooper())
    private val timer: Runnable = object : Runnable {
        override fun run() {
            totalTime += 1
            callback(totalTime)
            handler.postDelayed(this, 1000)
        }
    }



    fun start() {
        handler.postDelayed(timer, 1000)
    }

    fun stop() {
        handler.removeCallbacks(timer)
    }
}