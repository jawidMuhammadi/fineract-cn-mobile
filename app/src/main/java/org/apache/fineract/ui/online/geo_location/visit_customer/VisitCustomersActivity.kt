package org.apache.fineract.ui.online.geo_location.visit_customer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.activity_visit_customers2.*
import org.apache.fineract.R
import org.apache.fineract.data.models.geolocation.GeoPoint
import org.apache.fineract.ui.base.FineractBaseActivity
import org.apache.fineract.utils.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import org.apache.fineract.utils.Constants.PERMISSIONS_REQUEST_ENABLE_GPS
import org.apache.fineract.utils.MaterialDialog
import org.apache.fineract.utils.Utils
import javax.inject.Inject


class VisitCustomersActivity : FineractBaseActivity(), OnMapReadyCallback {

    private var fusedLocationClient: FusedLocationProviderClient? = null

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
            //start navigating
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


    fun getLastKnownLocation() {
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
            val location = it.result
            val geoPoint = GeoPoint(location?.latitude, location?.longitude)
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
        if (isMapsEnabled()) {
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
        map?.isMyLocationEnabled = true
        val sydney = LatLng(-34.0, 151.0)
        map?.addMarker(MarkerOptions().position(sydney).title("Marker Mifos"))
        map?.moveCamera(CameraUpdateFactory.newLatLng(sydney))
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

    private fun isMapsEnabled(): Boolean {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
            return false
        }
        return true
    }

    private fun buildAlertMessageNoGps() {
        MaterialDialog.Builder()
                .init(this)
                .setCancelable(true)
                .setMessage(R.string.application_require_gps_msg)
                .setPositiveButton(R.string.yes) { di: DialogInterface, _ ->
                    di.dismiss()
                    val enableGpsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS)
                }.createMaterialDialog()
                .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSIONS_REQUEST_ENABLE_GPS) {
            getLocationPermission()
        }
    }
}