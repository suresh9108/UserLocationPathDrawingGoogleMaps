package com.kotlin.userlocationpathdrawinggooglemaps.data.model

import com.google.android.gms.maps.model.LatLng

data class RoutePoint(
    val latLng: LatLng,
    val icon: String
)
