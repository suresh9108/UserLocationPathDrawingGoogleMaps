package com.kotlin.userlocationpathdrawinggooglemaps.view.activity

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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.kotlin.userlocationpathdrawinggooglemaps.viewmodel.MapsViewModel
import com.kotlin.userlocationpathdrawinggooglemaps.utils.OnSnapPositionChangeListener
import com.kotlin.userlocationpathdrawinggooglemaps.R
import com.kotlin.userlocationpathdrawinggooglemaps.data.model.RoutePoint
import com.kotlin.userlocationpathdrawinggooglemaps.utils.SingleItemScrollBehavior
import com.kotlin.userlocationpathdrawinggooglemaps.utils.SnapOnScrollListener
import com.kotlin.userlocationpathdrawinggooglemaps.databinding.ActivityMapBinding
import com.kotlin.userlocationpathdrawinggooglemaps.data.model.SampleData
import com.kotlin.userlocationpathdrawinggooglemaps.view.adapter.HorizantalListAdapter
import com.kotlin.userlocationpathdrawinggooglemaps.view.fragment.ListFragment
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min


class MapActivity : AppCompatActivity(), OnMapReadyCallback, OnSnapPositionChangeListener {

    private lateinit var binding: ActivityMapBinding
    private val mapsViewModel: MapsViewModel by viewModels()
    private lateinit var mMap: GoogleMap
    private var isMapVisible: Boolean = true
    private lateinit var snapHelper: LinearSnapHelper
    private var currentLocationMarker: Marker? = null
    private var selectedMarker: Marker? = null
    private val markersList = mutableListOf<Marker>()
    private lateinit var routeSampleData:List<RoutePoint>

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            showToast("Location Permission Granted")
            checkLocationServices()
            fetchAndObserveLocation()
        } else {
            showPermissionDeniedDialog()
            showToast("Location permission is required!")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeMapFragment()
        checkAndRequestLocationPermission()
        observeViewModel()
        setClickListeners()
    }

    private fun createScaledMarker(marker: Marker, scaleFactor: Float): BitmapDescriptor {
        val drawableId = when (marker.title?.toIntOrNull()) {
            0, 4, 6 -> R.drawable.marker_bolt
            1, 7 -> R.drawable.marker_kazam
            2, 5 -> R.drawable.marker_statiq
            3 -> R.drawable.marker_jio_bp
            else -> R.drawable.marker_kazam
        }
        val drawable = getDrawable(drawableId) ?: return BitmapDescriptorFactory.defaultMarker()
        val bitmap = Bitmap.createBitmap(
            (drawable.intrinsicWidth * scaleFactor).toInt(),
            (drawable.intrinsicHeight * scaleFactor).toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)

    }

    private fun restorePreviousMarker() {
        selectedMarker?.let {
            it.setIcon(createScaledMarker(it, 0.7f))
        }
    }

    override fun onResume() {
        super.onResume()
        checkLocationServices()
    }

    override fun onSnapPositionChange(position: Int) {
        updateMapMarker(position)
        restorePreviousMarker()
        if (position in markersList.indices) {
            selectedMarker = markersList[position]
            selectedMarker?.setIcon(createScaledMarker(selectedMarker!!, 1.1f))
        }
    }

    private fun updateMapMarker(position: Int) {
        if (::mMap.isInitialized) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(routeSampleData[position].latLng, 15f))
        }
    }

    private fun setClickListeners() {
        binding.gpsButton.setOnClickListener {
            checkAndRequestLocationPermission()
        }
        listOf(binding.toggleSwitch, binding.searchIv).forEach {
            it.setOnClickListener { toggleViewMode() }
        }

    }

    private fun toggleViewMode() {
        val mapFragment = findViewById<FragmentContainerView>(R.id.mapFragment)
        val listContainer = findViewById<FrameLayout>(R.id.listFragmentContainer)

        if (isMapVisible) {
            mapFragment.visibility = View.GONE
            listContainer.visibility = View.VISIBLE
            binding.toggleSwitch.setImageResource(R.drawable.icon_map)
            binding.searchIv.setImageResource(R.drawable.icon_arrow_left)
            binding.gpsButton.visibility = View.GONE
            supportFragmentManager.beginTransaction()
                .replace(R.id.listFragmentContainer, ListFragment())
                .commit()
        } else {
            mapFragment.visibility = View.VISIBLE
            listContainer.visibility = View.GONE
            binding.toggleSwitch.setImageResource(R.drawable.icon_charging_hubs)
            binding.searchIv.setImageResource(R.drawable.icon_search)
            binding.gpsButton.visibility = View.VISIBLE
        }
        isMapVisible = !isMapVisible
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.permission_denied_message))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.parse("package:$packageName")
                })
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun checkAndRequestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fetchAndObserveLocation()
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

    private fun fetchAndObserveLocation() {
        mapsViewModel.fetchUserLocation()
        //observeViewModel()
    }

    private fun checkLocationServices() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
          showToast("Please enable location services!")
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        } else {
            mapsViewModel.fetchUserLocation()
        }
    }

    private fun observeViewModel() {
        mapsViewModel.locationLiveData.observe(this, Observer { location ->
            location?.let { updateMapWithLocation(it) }
        })

        mapsViewModel.routeLiveData.observe(this, Observer { route ->
            if (::mMap.isInitialized) {
                drawPathOnMap(route)
                routeSampleData=route
                horizontalItems()
                addMarkersDataOnMap()
            }
        })

        mapsViewModel.fetchRouteData()

    }

    private fun addMarkersDataOnMap() {
        val routeCoordinates = routeSampleData
        if (routeCoordinates.isNotEmpty()) {
            drawPathOnMap(routeCoordinates)
            addMarkers()
        } else {
            Log.e("TAG", "No route data found!")
        }
    }

    private fun horizontalItems() {
        binding.recyclerView.visibility = View.GONE
        binding.recyclerView.layoutManager = SingleItemScrollBehavior(this@MapActivity)
        binding.recyclerView.adapter = HorizantalListAdapter(routeSampleData)

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

        binding.recyclerView.onFlingListener = null
        snapHelper.attachToRecyclerView(binding.recyclerView)
        binding.recyclerView.addOnScrollListener(SnapOnScrollListener(snapHelper, this@MapActivity))

    }

    private fun initializeMapFragment() {
        (supportFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment)?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener { marker ->
            handleMarkerClick(marker)
            true
        }
        mapsViewModel.fetchRouteData()

    }
    private fun handleMarkerClick(marker: Marker) {
        val index = marker.title?.toIntOrNull()
        Log.e("TAG", "Clicked marker index -> $index")

        if (index != null) {
            if (selectedMarker == marker) {
                binding.recyclerView.visibility = View.GONE
                restorePreviousMarker()
                selectedMarker = null
            } else {
                restorePreviousMarker()
                selectedMarker = marker
                selectedMarker?.setIcon(createScaledMarker(marker, 1.1f))
                binding.recyclerView.visibility = View.VISIBLE
                binding.recyclerView.post {
                    binding.recyclerView.smoothScrollToPosition(index)
                }
            }
        }
    }

    private fun addMarkers() {
        val jsonObject = JSONObject(SampleData.routeJson)
        val routeArray = jsonObject.getJSONArray("route")
        Log.e("TAG", "addMarkers: $routeArray", )

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
                "waypoint_marker6" -> R.drawable.marker_kazam
                else -> R.drawable.car_navigation
            }
            val markerIcon = createCustomMarkers(this, iconResId)
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(lat, lng))
                    .title("$i")
                    .icon(markerIcon)
            )
            marker?.let { markersList.add(it) }

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

    private fun updateMapWithLocation(location: Location) {
        val userLatLng = LatLng(location.latitude, location.longitude)
        currentLocationMarker?.remove()
        currentLocationMarker = mMap.addMarker(
            MarkerOptions()
                .position(userLatLng)
                .title("You are here")
                .icon(createCustomMarkers(this,R.drawable.tracking)))
        currentLocationMarker?.showInfoWindow()
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 9f))
    }

    private fun drawPathOnMap(route: List<RoutePoint>) {
        val polylineOptions = PolylineOptions()
            .addAll(route.map { it.latLng })
            .color(Color.BLUE)
            .width(10f)
            .geodesic(true)
        mMap.addPolyline(polylineOptions)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(route.map { it.latLng }.first(), 10f))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
