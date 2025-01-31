package com.kotlin.userlocationpathdrawinggooglemaps.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*

class LocationRepository(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val _locationLiveData = MutableLiveData<Location?>()
    val locationLiveData: LiveData<Location?> get() = _locationLiveData

    @SuppressLint("MissingPermission")
    fun fetchUserLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            _locationLiveData.postValue(location)
        }.addOnFailureListener {
            _locationLiveData.postValue(null)
        }
    }
}