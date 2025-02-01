package com.kotlin.userlocationpathdrawinggooglemaps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.kotlin.userlocationpathdrawinggooglemaps.R
import com.kotlin.userlocationpathdrawinggooglemaps.data.repository.LocationRepository

class MapsViewModel(application: Application) : AndroidViewModel(application) {

    private val locationRepository = LocationRepository(application)

    val locationLiveData: LiveData<Location?> = locationRepository.locationLiveData

    fun fetchUserLocation() {
        locationRepository.fetchUserLocation()
    }



}