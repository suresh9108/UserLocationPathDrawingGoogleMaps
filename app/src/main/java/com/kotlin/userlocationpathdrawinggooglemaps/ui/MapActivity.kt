package com.kotlin.userlocationpathdrawinggooglemaps.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.kotlin.userlocationpathdrawinggooglemaps.viewmodel.MapsViewModel
import com.kotlin.userlocationpathdrawinggooglemaps.utils.OnSnapPositionChangeListener
import com.kotlin.userlocationpathdrawinggooglemaps.R
import com.kotlin.userlocationpathdrawinggooglemaps.utils.SingleItemScrollBehavior
import com.kotlin.userlocationpathdrawinggooglemaps.utils.SnapOnScrollListener
import com.kotlin.userlocationpathdrawinggooglemaps.databinding.ActivityMapBinding
import com.kotlin.userlocationpathdrawinggooglemaps.model.SampleData
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min


class MapActivity : AppCompatActivity(), OnMapReadyCallback, OnSnapPositionChangeListener {

    private lateinit var binding: ActivityMapBinding
    private val mapsViewModel: MapsViewModel by viewModels()
    private lateinit var mMap: GoogleMap
    private var isMapVisible: Boolean = true
    private lateinit var snapHelper: LinearSnapHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        checkAndRequestLocationPermission()
        observeLocation()
        onclicks()

        binding.recyclerView.visibility = View.GONE

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.layoutManager = SingleItemScrollBehavior(this)
        binding.recyclerView.adapter = HorizantalListAdapter(getRouteCoordinates())

        snapHelper = LinearSnapHelper()


        snapHelper = object : LinearSnapHelper() {
            override fun findTargetSnapPosition(
                layoutManager: RecyclerView.LayoutManager,
                velocityX: Int,
                velocityY: Int
            ): Int {
                val centerView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION

                val position = layoutManager.getPosition(centerView)
                var targetPosition = -1
                if (layoutManager.canScrollHorizontally()) {
                    targetPosition = if (velocityX < 0) {
                        position - 1
                    } else {
                        position + 1
                    }
                }

                if (layoutManager.canScrollVertically()) {
                    targetPosition = if (velocityY < 0) {
                        position - 1
                    } else {
                        position + 1
                    }
                }

                val firstItem = 0
                val lastItem = layoutManager.itemCount - 1
                targetPosition =
                    min(lastItem.toDouble(), max(targetPosition.toDouble(), firstItem.toDouble()))
                        .toInt()
                return targetPosition
            }
        }

        snapHelper.attachToRecyclerView(binding.recyclerView)
        binding.recyclerView.addOnScrollListener(SnapOnScrollListener(snapHelper, this))
        mapFragment.getMapAsync { map ->
            mMap = map

            mMap.setOnMarkerClickListener { marker ->

                val index = marker.title?.toIntOrNull()

                Log.e("TAG", "onCreate: $index", )
                index?.let {
                   // updateMapMarker(index)
                    // Toggle the RecyclerView visibility
                    if (binding.recyclerView.visibility == View.GONE ) {
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.recyclerView.smoothScrollToPosition(it)
                    } else {
                        binding.recyclerView.visibility = View.GONE
                    }
                    return@setOnMarkerClickListener true
                }
                false
            }
        }
    }


    override fun onResume() {
        super.onResume()
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapsViewModel.fetchUserLocation()
        }
    }

    override fun onSnapPositionChange(position: Int) {
        updateMapMarker(position)
    }

    private fun updateMapMarker(position: Int) {
        val location = getRouteCoordinates()[position]
        if (::mMap.isInitialized) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }

    private fun onclicks() {




        binding.gpsButton.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Check if location services are enabled
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                if (isGpsEnabled || isNetworkEnabled) {
                    // Fetch the user's location
                    mapsViewModel.fetchUserLocation()
                    Toast.makeText(this, "Fetching your location...", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enable GPS or network location!", Toast.LENGTH_LONG).show()
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            } else {
                Toast.makeText(this, "Location permission not granted!", Toast.LENGTH_SHORT).show()
                checkAndRequestLocationPermission() // Request permissions again if not granted
            }
        }


        binding.toggleSwitch.setOnClickListener {
          toggleBehaviour()
        }

        binding.searchIv.setOnClickListener {
            toggleBehaviour()
        }
    }

    private fun toggleBehaviour() {
        if (isMapVisible) {
            findViewById<FragmentContainerView>(R.id.mapFragment).visibility = View.GONE
            findViewById<FrameLayout>(R.id.listFragmentContainer).visibility = View.VISIBLE
            binding.toggleSwitch.setImageResource(R.drawable.icon_map)
            binding.searchIv.setImageResource(R.drawable.icon_arrow_left)
            binding.gpsButton.visibility=View.GONE


            supportFragmentManager.beginTransaction()
                .replace(R.id.listFragmentContainer, ListFragment())
                .commit()

            isMapVisible = false
        } else {
            findViewById<FragmentContainerView>(R.id.mapFragment).visibility = View.VISIBLE
            findViewById<FrameLayout>(R.id.listFragmentContainer).visibility = View.GONE
            binding.toggleSwitch.setImageResource(R.drawable.icon_charging_hubs)
            binding.searchIv.setImageResource(R.drawable.icon_search)
            binding.gpsButton.visibility=View.VISIBLE
            isMapVisible = true
        }
    }


    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            Toast.makeText(this, "Location Permission Granted", Toast.LENGTH_SHORT).show()
            checkLocationServices()
            fetchAndObserveLocation() // Fetch location after permissions are granted
        } else {
            Toast.makeText(this, "Location permission is required!", Toast.LENGTH_SHORT).show()
        }
    }


    private fun checkAndRequestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Permissions already granted
            fetchAndObserveLocation()
            checkLocationServices()
        } else {
            // Request permissions
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun fetchAndObserveLocation() {
        mapsViewModel.fetchUserLocation() // Start fetching the user's location
        observeLocation() // Observe location updates
    }


    private fun checkLocationServices() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            Toast.makeText(this, "Please enable location services!", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    private fun observeLocation() {
        mapsViewModel.locationLiveData.observe(this, Observer { location ->
            location?.let {
                updateMapWithLocation(it)
            } ?: run {
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        }

        observeLocation() // Observe location updates and update the map
        val routeCoordinates = getRouteCoordinates()

        if (routeCoordinates.isNotEmpty()) {
            drawPathOnMap(routeCoordinates)
            addMarkers()
        } else {
            Log.e("TAG", "No route data found!")
        }
    }




    private fun addMarkers() {

        val jsonObject = JSONObject(SampleData.routeJson)
        val routeArray = jsonObject.getJSONArray("route")

        for (i in 0 until routeArray.length()) {
            val point = routeArray.getJSONObject(i)
            val lat = point.getDouble("latitude")
            val lng = point.getDouble("longitude")
            val iconName = point.getString("icon")

            val iconResId = when (iconName) {
                "start_marker" -> R.drawable.marker_bolt
                "end_marker" -> R.drawable.marker_statiq
                "waypoint_marker1" -> R.drawable.marker_kazam
                "waypoint_marker2" -> R.drawable.marker_statiq
                "waypoint_marker3" -> R.drawable.marker_jio_bp
                "waypoint_marker4" -> R.drawable.marker_bolt
                "waypoint_marker5" -> R.drawable.marker_statiq
                else -> R.drawable.car_navigation
            }

            val markerIcon = createCustomMarkers(this, iconResId)


            mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(lat, lng))
                    .title("$i")
                    .icon(markerIcon))

        }
    }

    private fun createCustomMarkers(context: Context, drawableId: Int): BitmapDescriptor {
        val drawable = context.getDrawable(drawableId) ?: return BitmapDescriptorFactory.defaultMarker()

        val scaleFactor = 0.7f

        val width = (drawable.intrinsicWidth * scaleFactor).toInt()
        val height = (drawable.intrinsicHeight * scaleFactor).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }




    private fun createCustomMarker(context: Context, drawableId: Int): BitmapDescriptor {
        val drawable = context.getDrawable(drawableId) ?: return BitmapDescriptorFactory.defaultMarker()

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }



    private fun updateMapWithLocation(location: Location) {
        val userLatLng = LatLng(location.latitude, location.longitude)
        val customMarker = BitmapDescriptorFactory.fromBitmap(createCustomMarker())
        mMap.addMarker(MarkerOptions().position(userLatLng).title("You are here").icon(customMarker))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 9f))
    }

    private fun createCustomMarker(): Bitmap {
        val drawable = getDrawable(R.drawable.gps_navigation1) ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun drawPathOnMap(route: List<LatLng>) {
        val polylineOptions = PolylineOptions()
            .addAll(route)
            .color(Color.BLUE)
            .width(10f)
            .geodesic(true)

        mMap.addPolyline(polylineOptions)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(route.first(), 10f))
    }

    private fun getRouteCoordinates(): List<LatLng> {
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

        val jsonObject = JSONObject(routeJson)
        val jsonArray = jsonObject.getJSONArray("route")

        val routeList = mutableListOf<LatLng>()
        for (i in 0 until jsonArray.length()) {
            val location = jsonArray.getJSONObject(i)
            val lat = location.getDouble("latitude")
            val lng = location.getDouble("longitude")
            routeList.add(LatLng(lat, lng))
        }

        return routeList
    }

}
