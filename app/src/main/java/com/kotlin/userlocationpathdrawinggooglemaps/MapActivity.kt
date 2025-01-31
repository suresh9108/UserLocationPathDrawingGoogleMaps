package com.kotlin.userlocationpathdrawinggooglemaps

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.kotlin.userlocationpathdrawinggooglemaps.databinding.ActivityMapBinding
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min


class MapActivity : AppCompatActivity() ,OnMapReadyCallback,OnSnapPositionChangeListener{

    private lateinit var binding: ActivityMapBinding
    private val mapsViewModel: MapsViewModel by viewModels()
    private lateinit var mMap: GoogleMap
    private var isMapVisible: Boolean = true
    private lateinit var snapHelper: LinearSnapHelper



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)


        checkAndRequestLocationPermission()
        observeLocation()
        onclicks()

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.layoutManager = SingleItemScrollBehavior(this)
        binding.recyclerView.adapter = HorizantalListAdapter(getRouteCoordinates())

        // Attach SnapHelper
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

        // Attach scroll listener
        binding.recyclerView.addOnScrollListener(SnapOnScrollListener(snapHelper, this))

        // Initialize Google Map
        mapFragment.getMapAsync { map ->
            mMap = map
            updateMapMarker(0) // Set initial marker
            mMap.setOnMarkerClickListener { marker ->
                // Get the index from the marker's tag
                val index = marker.title
                Log.e("TAG", "onCreate: ${marker.title}", )
                index?.let {
                    // Scroll to the corresponding position in the RecyclerView
                    (binding.recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(it.toInt(), 0)
                    binding.recyclerView.smoothScrollToPosition(it.toInt())
                    return@setOnMarkerClickListener true
                }
                false
            }

        }



    }

    override fun onSnapPositionChange(position: Int) {
        updateMapMarker(position)
    }

    private fun updateMapMarker(position: Int) {
        val location = getRouteCoordinates()[position]
        if (::mMap.isInitialized) {
          //  mMap.clear()
            mMap.addMarker(MarkerOptions().position(location).title("${position + 1}"))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12f))
        }
    }


    private fun onclicks() {
        binding.toggleSwitch.setOnClickListener {
            if (isMapVisible) {
                // Hide the map and show the list fragment

                findViewById<FragmentContainerView>(R.id.mapFragment).visibility = View.GONE
                findViewById<FrameLayout>(R.id.listFragmentContainer).visibility = View.VISIBLE

                // Show the list fragment
                supportFragmentManager.beginTransaction()
                    .replace(R.id.listFragmentContainer, ListFragment())
                    .commit()

                isMapVisible = false
            } else {
                // Show the map and hide the list fragment
                findViewById<FragmentContainerView>(R.id.mapFragment).visibility = View.VISIBLE
                findViewById<FrameLayout>(R.id.listFragmentContainer).visibility = View.GONE

                isMapVisible = true
            }

        }
    }

    // Launcher to request permissions
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            //statusTextView.text = "Location Permission Granted"
            Toast.makeText(this, "Location Permission Granted", Toast.LENGTH_SHORT).show()

            checkLocationServices()
        } else {
            //statusTextView.text = "Permission Denied"
            Log.e("TAG", ": Location Permission Granted")
            Toast.makeText(this, "Location permission is required!", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to check and request location permission
    private fun checkAndRequestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

          //  statusTextView.text = "Location Permission Already Granted"
            mapsViewModel.fetchUserLocation()
            checkLocationServices()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Function to check if location services are enabled
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

        val routeCoordinates = getRouteCoordinates()

        if (routeCoordinates.isNotEmpty()) {
            drawPathOnMap(routeCoordinates)
        } else {
            Log.e("TAG", "No route data found!")
        }
    }


    private fun updateMapWithLocation(location: Location) {
        Log.e("TAG", "updateMapWithLocation: $location")
        val userLatLng = LatLng(location.latitude, location.longitude)
        val customMarker = BitmapDescriptorFactory.fromBitmap(createCustomMarker())

        //mMap.clear()
        mMap.addMarker(MarkerOptions().position(userLatLng).title("You are here").icon(customMarker))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 7f))
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
        if (::mMap.isInitialized) {
            val polylineOptions = PolylineOptions()
                .addAll(route)
                .color(Color.BLUE)
                .width(10f)
                .geodesic(true)

            Log.e("TAG", "drawPathOnMap: ${route.first()}")

            mMap.addPolyline(polylineOptions)

            // Add start and end markers
            mMap.addMarker(MarkerOptions().position(route.first()).title("Start Point"))
            mMap.addMarker(MarkerOptions().position(route.last()).title("End Point"))

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(route.first(), 10f))
        }
    }


    private fun getRouteCoordinates(): List<LatLng> {
        val routeJson = """
        {
            "route": [
                {"latitude":12.9716, "longitude":77.5946},
                {"latitude":12.9062, "longitude":77.4846},
                {"latitude":12.7159, "longitude":77.2810},
                {"latitude":12.6516, "longitude":77.2067},
                {"latitude":12.5823, "longitude":77.0429},
                {"latitude":12.5222, "longitude":76.8971},
                {"latitude":12.4136, "longitude":76.7041},
                {"latitude":12.2958, "longitude":76.6394}
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