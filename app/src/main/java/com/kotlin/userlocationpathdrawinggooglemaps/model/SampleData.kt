package com.kotlin.userlocationpathdrawinggooglemaps.model

class SampleData {
    companion object {
        val routeJson = """
        {
    "route": [
        {
            "latitude": 12.9716,
            "longitude": 77.5946,
            "icon": "start_marker"
        },
        {
            "latitude": 12.9062,
            "longitude": 77.4846,
            "icon": "waypoint_marker1"
        },
        {
            "latitude": 12.7159,
            "longitude": 77.2810,
            "icon": "waypoint_marker2"
        },
        {
            "latitude": 12.6516,
            "longitude": 77.2067,
            "icon": "waypoint_marker3"
        },
        {
            "latitude": 12.5823,
            "longitude": 77.0429,
            "icon": "waypoint_marker4"
        },
        {
            "latitude": 12.5222,
            "longitude": 76.8971,
            "icon": "waypoint_marker5"
        },
        {
            "latitude": 12.4136,
            "longitude": 76.7041,
            "icon": "waypoint_marker6"
        },
        {
            "latitude": 12.2958,
            "longitude": 76.6394,
            "icon": "end_marker"
        }
    ]
}
    """.trimIndent()
    }
}