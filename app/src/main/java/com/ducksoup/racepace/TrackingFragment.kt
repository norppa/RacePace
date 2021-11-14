package com.ducksoup.racepace

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.round

class TrackingFragment : Fragment() {
    private lateinit var map: MapView
    private lateinit var timeTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var speedTextView: TextView

    private lateinit var clock: Clock

    private lateinit var locationManager: LocationManager

    private lateinit var currentLocation: Location
    private var totalDistance = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().load(requireContext(), requireActivity().getPreferences(Context.MODE_PRIVATE))
        return inflater.inflate(R.layout.fragment_tracking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        map = view.findViewById(R.id.map)
        timeTextView = view.findViewById(R.id.timeText)
        distanceTextView = view.findViewById(R.id.distanceText)
        speedTextView = view.findViewById(R.id.speedText)
        clock = Clock { seconds ->
            updateTime(seconds)
            updateSpeed(seconds)
        }
        initLocationManager()

        view.findViewById<Button>(R.id.stopBtn).setOnClickListener {
            clock.stop()
            locationManager.removeUpdates(locationListener)
        }
    }

    private fun initMap(location: Location) {
        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), map)
        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation()

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.controller.setZoom(16.0)
        map.controller.setCenter(GeoPoint(location))
        map.overlays.add(locationOverlay)
    }


    private val locationListener = LocationListener { location ->
        Log.i("foobar", "locationListener $location")
        updateDistance(location)
    }

    private fun pad(n: Int): String = if (n < 10) "0$n" else "$n"

    private fun getTimeStr(seconds: Int): String {
        val s = seconds % 60
        val m = seconds / 60
        val h = m / 60

        return (if (h > 0) "$h:" else "") + "${pad(m)}:${pad(s)}"
    }

    private fun updateTime(seconds: Int) {
        timeTextView.text = getTimeStr(seconds)
    }

    private fun updateSpeed(seconds: Int) {
        val speed = round(totalDistance / 1000 / (seconds.toDouble() / 3600))
        speedTextView.text = "Speed: $speed km/h"

    }

    private fun updateDistance(location: Location) {
        totalDistance += distance(currentLocation, location)
        val distanceString = "Distance: ${round(totalDistance)}"
        distanceTextView.text = distanceString
    }

    @SuppressLint("MissingPermission")
    private fun initLocationManager() {
        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.getCurrentLocation(
            LocationManager.GPS_PROVIDER,
            null,
            ContextCompat.getMainExecutor(requireContext()),
            { location ->
                Log.i("foobar", "initial location")
                currentLocation = location
                initMap(location)
                clock.start()
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    2F,
                    locationListener
                )
            }
        )

    }

    private fun distance(l1: Location, l2: Location): Float {
        val results = FloatArray(1)
        Location.distanceBetween(l1.latitude, l1.longitude, l2.latitude, l2.longitude, results)
        return results[0]
    }


    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}