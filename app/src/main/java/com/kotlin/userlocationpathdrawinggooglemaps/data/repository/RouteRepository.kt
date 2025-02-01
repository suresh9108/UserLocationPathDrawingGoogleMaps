package com.kotlin.userlocationpathdrawinggooglemaps.data.repository

import com.google.android.gms.maps.model.LatLng
import com.kotlin.userlocationpathdrawinggooglemaps.data.model.RoutePoint
import org.json.JSONObject

class RouteRepository {

    fun getRouteCoordinates(): List<RoutePoint> {
        val routeJson = """
            {
                "route": [
                    {"latitude": 12.9716, "longitude": 77.5946, "icon": "start_marker"},
                    {"latitude": 12.9062, "longitude": 77.4846, "icon": "waypoint_marker1"},
                    {"latitude": 12.7159, "longitude": 77.2810, "icon": "waypoint_marker2"},
                    {"latitude": 12.6516, "longitude": 77.2067, "icon": "waypoint_marker3"},
                    {"latitude": 12.5823, "longitude": 77.0429, "icon": "waypoint_marker4"},
                    {"latitude": 12.5222, "longitude": 76.8971, "icon": "waypoint_marker5"},
                    {"latitude": 12.4136, "longitude": 76.7041, "icon": "waypoint_marker6"},
                    {"latitude": 12.2958, "longitude": 76.6394, "icon": "end_marker"}
                ]
            }
        """.trimIndent()

        val jsonObject = JSONObject(routeJson)
        val jsonArray = jsonObject.getJSONArray("route")

        val routeList = mutableListOf<RoutePoint>()
        for (i in 0 until jsonArray.length()) {
            val location = jsonArray.getJSONObject(i)
            val lat = location.getDouble("latitude")
            val lng = location.getDouble("longitude")
            val icon = location.getString("icon")
            routeList.add(RoutePoint(LatLng(lat, lng), icon))
        }

        return routeList
    }
}
