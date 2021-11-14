package com.ducksoup.racepace

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.content.ContextCompat

@SuppressLint("MissingPermission")
class Tracker(
    activity: Activity,
    context: Context,
    onTimeChange: (time: Int) -> Unit,
    onSpeedChange: (speed: Double) -> Unit,
    onLocationChange: (location: Location) -> Unit,
    onDistanceChange: (distance: Double) -> Unit,
    onFirstFix: (location: Location) -> Unit
) {

    private val timer = Timer()
    private var distance = 0.0
    private lateinit var location: Location

    private val provider = LocationManager.GPS_PROVIDER

    private val locationListener = LocationListener { location ->
        distance += distanceBetween(this.location, location)
        this.location = location
        onLocationChange(location)
        onDistanceChange(distance)
    }
    private var locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    init {
        timer.setTick { time ->
            onTimeChange(time)
            onSpeedChange((distance / 1000) / (time.toDouble() / 3600))
        }

        locationManager.getCurrentLocation(
            LocationManager.GPS_PROVIDER,
            null,
            ContextCompat.getMainExecutor(context),
            { location ->
                this.location = location
                onFirstFix(location)
            }
        )
    }

    fun start() {
        locationManager.requestLocationUpdates(provider, 1000L, 2F, locationListener)
        timer.start()
    }

    fun stop() {
        locationManager.removeUpdates(locationListener)
        timer.stop()
    }

    private fun distanceBetween(l1: Location, l2: Location): Double {
        val results = FloatArray(1)
        Location.distanceBetween(l1.latitude, l1.longitude, l2.latitude, l2.longitude, results)
        return results[0].toDouble()
    }


}