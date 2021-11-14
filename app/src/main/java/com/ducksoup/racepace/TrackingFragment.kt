package com.ducksoup.racepace

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.round

class TrackingFragment : Fragment() {
    private lateinit var map: MapView
    private lateinit var button: Button
    private lateinit var timeTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var speedTextView: TextView

    private lateinit var tracker: Tracker

    private val path = Polyline()
    private var tracking = false

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
        button = view.findViewById(R.id.button)
        timeTextView = view.findViewById(R.id.timeText)
        distanceTextView = view.findViewById(R.id.distanceText)
        speedTextView = view.findViewById(R.id.speedText)

        tracker = Tracker(
            requireActivity(),
            requireContext(),
            ::updateTime,
            ::updateSpeed,
            ::updatePath,
            ::updateDistance,
        ) { location -> initMap(location) }

        button.setOnClickListener {
            if (tracking) {
                tracker.stop()
                button.text = "START"
            } else {
                tracker.start()
                timeTextView.text = "00:00"
                distanceTextView.text = "0 m"
                speedTextView.text = "0 km/h"
                button.text = "STOP"
                path.setPoints(listOf())
            }
            tracking = !tracking
        }

    }

    private fun initMap(location: Location) {
        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), map)
        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation()

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.controller.setZoom(19.0)
        map.controller.setCenter(GeoPoint(location))
        map.overlays.add(locationOverlay)
        map.overlayManager.add(path)
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

    private fun updateSpeed(speed: Double) {
        speedTextView.text = "Speed: ${round(speed)} km/h"

    }

    private fun updateDistance(distance: Double) {
        val distanceString = "Distance: ${round(distance)}"
        distanceTextView.text = distanceString
    }

    private fun updatePath(location: Location) {
        path.addPoint(GeoPoint(location))
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