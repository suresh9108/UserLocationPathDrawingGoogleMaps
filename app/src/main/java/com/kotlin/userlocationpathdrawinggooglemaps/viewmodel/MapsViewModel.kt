package com.kotlin.userlocationpathdrawinggooglemaps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.kotlin.userlocationpathdrawinggooglemaps.data.model.RoutePoint
import com.kotlin.userlocationpathdrawinggooglemaps.data.repository.LocationRepository
import com.kotlin.userlocationpathdrawinggooglemaps.data.repository.RouteRepository

class MapsViewModel(application: Application) : AndroidViewModel(application) {

    private val locationRepository = LocationRepository(application)
    private val routeRepository = RouteRepository()

    val locationLiveData: LiveData<Location?> = locationRepository.locationLiveData
    private val _routeLiveData = MutableLiveData<List<RoutePoint>>()
    val routeLiveData: LiveData<List<RoutePoint>> get() = _routeLiveData

    fun fetchUserLocation() {
        locationRepository.fetchUserLocation()
    }

    fun fetchRouteData() {
        _routeLiveData.value = routeRepository.getRouteCoordinates()
    }
}
