package com.ducksoup.racepace

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

class SetupFragment : Fragment() {
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.INTERNET
    )
    private val permissionsRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = requiredPermissions.all { requiredPermission ->
                permissions.getOrDefault(requiredPermission, false)
            }
            onPermissionsConfirmed(allGranted)
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setup, container, false)
    }

    private fun onPermissionsConfirmed(granted: Boolean) {
        if (granted) {
            findNavController().navigate(R.id.action_setupFragment_to_trackingFragment)
        } else {
            Snackbar.make(requireView(), "You must give permissions, dude!", Snackbar.LENGTH_SHORT).show()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.startButton).setOnClickListener {
            checkPermissions()
        }
    }

    private fun checkPermissions() {

        fun isGranted(permission: String): Boolean {
            return ContextCompat.checkSelfPermission(requireContext(), permission) ==
                    PackageManager.PERMISSION_GRANTED
        }

        if (requiredPermissions.all(::isGranted)) {
            onPermissionsConfirmed(true)
        } else {
            permissionsRequest.launch(requiredPermissions)
        }
    }

}