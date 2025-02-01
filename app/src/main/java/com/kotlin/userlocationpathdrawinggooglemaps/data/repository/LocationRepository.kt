package com.kotlin.userlocationpathdrawinggooglemaps.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*

class LocationRepository(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val _locationLiveData = MutableLiveData<Location?>()
    val locationLiveData: LiveData<Location?> get() = _locationLiveData

    @SuppressLint("MissingPermission")
    fun fetchUserLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                _locationLiveData.postValue(it)  // ✅ Ensure LiveData is updated
            }
        }.addOnFailureListener {
            Log.e("MapsViewModel", "Failed to get location", it)
        }
    }

}