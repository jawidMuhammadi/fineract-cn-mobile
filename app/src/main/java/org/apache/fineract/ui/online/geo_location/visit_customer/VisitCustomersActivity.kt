package org.apache.fineract.ui.online.geo_location.visit_customer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.material.button.MaterialButton
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.DirectionsResult
import kotlinx.android.synthetic.main.activity_visit_customers2.*
import org.apache.fineract.R
import org.apache.fineract.data.models.geolocation.PolylineData
import org.apache.fineract.ui.base.FineractBaseActivity
import org.apache.fineract.utils.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import org.apache.fineract.utils.Constants.PERMISSIONS_REQUEST_ENABLE_GPS
import org.apache.fineract.utils.Utils
import org.apache.fineract.utils.addMarkerOnNearByCustomers
import org.apache.fineract.utils.isMapsEnabled
import org.apache.fineract.worker.LocationTrackWorker
import org.apache.fineract.worker.LocationTrackWorker.Companion.KEY_CLIENT_NAME
import java.util.*
import javax.inject.Inject

val MOUNTAIN_VIEW = LatLng(37.4, -122.1)


class VisitCustomersActivity : FineractBaseActivity(),
        OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener, GoogleMap.OnInfoWindowClickListener {

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var lastKnowLocation: Location? = null
    private var geoApiContext: GeoApiContext? = null
    private var polyLinesData = ArrayList<PolylineData>()
    private var googleMap: GoogleMap? = null

    @Inject
    lateinit var factory: VisitCustomerViewModelFactory
    private var viewModel: VisitCustomerViewModel? = null

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_customers2)
        activityComponent.inject(this)
        showBackButton()
        setToolbarTitle(getString(R.string.visit_customer))
        setupMapView(savedInstanceState)
        viewModel = ViewModelProviders.of(this, factory).get(VisitCustomerViewModel::class.java)
        changeBackgroundColor(fabEnterManually, false)
        subscribeUI()
        geoApiContext = GeoApiContext.Builder()
                .apiKey(getString(R.string.app_map_key))
                .build()
        getLastKnownLocation()
    }

    private fun subscribeUI() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fabEnterManually?.setOnClickListener {
            changeBackgroundColor(fabEnterManually, true)
            changeBackgroundColor(fabNearbyCustomers, false)
        }
        fabNearbyCustomers?.setOnClickListener {
            changeBackgroundColor(fabEnterManually, false)
            changeBackgroundColor(fabNearbyCustomers, true)
        }
        fabNavigate?.setOnClickListener {
            val data = workDataOf(KEY_CLIENT_NAME to "Ahmad jawid")

            val trackerWorker = OneTimeWorkRequestBuilder<LocationTrackWorker>()
                    .setInputData(data)
                    .build()

            with(WorkManager.getInstance(this)) {
                enqueue(trackerWorker)
            }
        }
    }


    private fun changeBackgroundColor(button: MaterialButton, isToPrimary: Boolean) {
        if (isToPrimary) {
            button.setBackgroundColor(resources.getColor(R.color.colorPrimary))
            button.setTextColor(resources.getColor(R.color.white))
            button.setIconTintResource(R.color.white)
        } else {
            button.setBackgroundColor(Color.WHITE)
            button.setTextColor(resources.getColor(R.color.black))
            button.setIconTintResource(R.color.black)
        }
    }

    private fun setupMapView(bundle: Bundle?) {
        mapView.onCreate(bundle)
        mapView?.getMapAsync(this)
    }


    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationPermission()
            return
        }
        fusedLocationClient?.lastLocation?.addOnCompleteListener {
            lastKnowLocation = it.result
            Log.e("VisitCustomerActivity", it.result.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        Utils.setToolbarIconColor(this, menu, R.color.white)
        return true
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        if (checkMapServices()) {
            getLocationPermission()
        }
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    private fun checkMapServices(): Boolean {
        if (isMapsEnabled(this)) {
            return true
        }
        return false
    }

    override fun onMapReady(map: GoogleMap?) {
        if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        googleMap = map
        map?.isMyLocationEnabled = true
        addMarkerOnNearByCustomers(map, this)
//        map?.setOnMarkerClickListener { it ->
//            drawDirection(it)
//            true
//        }
        // map?.setOnPolylineClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        onLowMemory()
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //show current location of the user on map
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //show current location
            }

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSIONS_REQUEST_ENABLE_GPS) {
            getLocationPermission()
        }
    }

    override fun onPolylineClick(polyline: Polyline?) {
        var index = 0
        for (polylineData in polyLinesData) {
            index++
            if (polyline!!.id == polylineData.polyline.id) {
                polylineData.polyline.color = ContextCompat.getColor(this, R.color.blue)
                polylineData.polyline.zIndex = 1F
                val endLocation = LatLng(
                        polylineData.leg.endLocation.lat,
                        polylineData.leg.endLocation.lng
                )
                val marker: Marker = googleMap!!.addMarker(MarkerOptions()
                        .position(endLocation)
                        .title("Trip #$index")
                        .snippet("Duration: " + polylineData.leg.duration
                        ))
                // mTripMarkers.add(marker)
                marker.showInfoWindow()
            } else {
                polylineData.polyline.color = ContextCompat.getColor(this, R.color.grey_500)
                polylineData.polyline.zIndex = 1F
            }
        }
    }

    private fun calculateDirections(marker: Marker, currentPosition: com.google.maps.model.LatLng) {
        val destination = com.google.maps.model.LatLng(
                marker.position.latitude,
                marker.position.longitude
        )
        val directions = DirectionsApiRequest(geoApiContext)
        directions.alternatives(true)
        directions.origin(currentPosition)
        directions.destination(destination).setCallback(object : PendingResult.Callback<DirectionsResult?> {
            override fun onResult(result: DirectionsResult?) {
//                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
//                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
//                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
//                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
                addPolylinesToMap(result)
            }

            override fun onFailure(e: Throwable) {
                Log.e("VisitCustomerActivity", e.printStackTrace().toString())
            }
        })
    }

    fun addPolylinesToMap(result: DirectionsResult?) {
        Handler(Looper.getMainLooper()).post {
            if (polyLinesData.size > 0) {
                for (polylineData in polyLinesData) {
                    polylineData.polyline.remove()
                }
                polyLinesData.clear()
                polyLinesData = ArrayList<PolylineData>()
            }
            var duration = 999999999.0
            for (route in result!!.routes) {
                val decodedPath = PolylineEncoding.decode(route.overviewPolyline.encodedPath)
                val newDecodedPath: MutableList<LatLng> = ArrayList()

                // This loops through all the LatLng coordinates of ONE polyline.
                for (latLng in decodedPath) {
//                        Log.d(TAG, "run: latlng: " + latLng.toString());
                    newDecodedPath.add(LatLng(
                            latLng.lat,
                            latLng.lng
                    ))
                }
                val polyline: Polyline = googleMap!!.addPolyline(PolylineOptions().addAll(newDecodedPath))
                polyline.color = ContextCompat.getColor(this, R.color.grey_500)
                polyline.isClickable = true
                polyLinesData.add(PolylineData(polyline, route.legs[0]))

                // highlight the fastest route and adjust camera
                val tempDuration = route.legs[0].duration.inSeconds.toDouble()
                if (tempDuration < duration) {
                    duration = tempDuration
                    onPolylineClick(polyline)
                    zoomRoute(polyline.points)
                }
                //  mSelectedMarker.setVisible(false)
            }
        }
    }

    private fun zoomRoute(lstLatLngRoute: List<LatLng?>?) {
        if (googleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return
        val boundsBuilder = LatLngBounds.Builder()
        for (latLngPoint in lstLatLngRoute) boundsBuilder.include(latLngPoint)
        val routePadding = 50
        val latLngBounds = boundsBuilder.build()
        googleMap?.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        )
    }

    override fun onInfoWindowClick(marker: Marker?) {
        marker?.let {
            calculateDirections(
                    it,
                    com.google.maps.model.LatLng(40.743595, -74.0078)
            )
        }
    }

}