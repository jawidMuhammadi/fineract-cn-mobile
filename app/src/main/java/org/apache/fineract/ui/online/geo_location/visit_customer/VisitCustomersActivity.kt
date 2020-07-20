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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.activity_visit_customers2.*
import org.apache.fineract.R
import org.apache.fineract.ui.base.FineractBaseActivity
import org.apache.fineract.utils.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import org.apache.fineract.utils.Constants.PERMISSIONS_REQUEST_ENABLE_GPS
import org.apache.fineract.utils.MaterialDialog
import org.apache.fineract.utils.Utils


class VisitCustomersActivity : FineractBaseActivity(), OnMapReadyCallback {
    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_customers2)
        showBackButton()
        setToolbarTitle(getString(R.string.visit_customer))
        setupMapView()
        changeBackgroundColor(fabEnterManually, false)

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

    private fun setupMapView() {
        mapView.onCreate(null)
        mapView?.getMapAsync(this)
        MapsInitializer.initialize(this)
        mapView.onResume()
        mapView.postInvalidate()
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
        if (checkMapServices()) {
            getLocationPermission()
        }
    }

    private fun checkMapServices(): Boolean {
        if (isMapsEnabled()) {
            return true
        }
        return false
    }

    override fun onMapReady(map: GoogleMap?) {
        val sydney = LatLng(-34.0, 151.0)
        map?.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        map?.moveCamera(CameraUpdateFactory.newLatLng(sydney))
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