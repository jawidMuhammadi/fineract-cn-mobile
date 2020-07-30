package org.apache.fineract.utils

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.apache.fineract.R

/**
 * Created by Ahmad Jawid Muhammadi on 22/7/20.
 */

fun buildAlertMessageNoGps(activity: Activity) {
    MaterialDialog.Builder()
            .init(activity)
            .setCancelable(true)
            .setMessage(R.string.application_require_gps_msg)
            .setPositiveButton(R.string.yes) { di: DialogInterface, _ ->
                di.dismiss()
                val enableGpsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity.startActivityForResult(enableGpsIntent, Constants.PERMISSIONS_REQUEST_ENABLE_GPS)
            }.createMaterialDialog()
            .show()
}

fun addMarkerOnMap(map: GoogleMap?, latLng: LatLng, title: String?) {
    map?.addMarker(MarkerOptions().position(latLng).title(title))
    val cameraPosition = CameraPosition.Builder()
            .target(latLng)
            .zoom(15f)
            .bearing(90f)
            .tilt(30f)
            .build()
    map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
}

fun addMarkerOnNearByCustomers(map: GoogleMap?, onInfoWindowClickListener: GoogleMap.OnInfoWindowClickListener) {
    val locations = arrayListOf(
            LatLng(40.743595, -74.0078),
            LatLng(40.744180, -74.006513),
            LatLng(40.741549, -74.005417)
    )
    for (location in locations) {
        map?.addMarker(
                MarkerOptions()
                        .position(location)
                        .title("Customer Office Name")
                        .snippet("Location")
        )
        map?.setOnInfoWindowClickListener(onInfoWindowClickListener)
    }
    val cameraPosition = CameraPosition.Builder()
            .target(LatLng(40.743595, -74.0078))
            .zoom(15f) // Sets the zoom
            .bearing(90f) // Sets the orientation of the camera to east
            .tilt(30f) // Sets the tilt of the camera to 30 degrees
            .build()
    map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
}


fun isMapsEnabled(activity: Activity): Boolean {
    val manager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        buildAlertMessageNoGps(activity)
        return false
    }
    return true
}

