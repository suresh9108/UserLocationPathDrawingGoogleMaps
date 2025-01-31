package com.kotlin.userlocationpathdrawinggooglemaps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import android.location.Location
import com.kotlin.userlocationpathdrawinggooglemaps.repository.LocationRepository

class MapsViewModel(application: Application) : AndroidViewModel(application) {

    private val locationRepository = LocationRepository(application)

    val locationLiveData: LiveData<Location?> = locationRepository.locationLiveData

    fun fetchUserLocation() {
        locationRepository.fetchUserLocation()
    }

}