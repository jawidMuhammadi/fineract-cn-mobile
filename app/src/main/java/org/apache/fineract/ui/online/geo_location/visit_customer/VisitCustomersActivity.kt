package org.apache.fineract.ui.online.geo_location.visit_customer

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
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

    override fun onMapReady(map: GoogleMap?) {
        val sydney = LatLng(-34.0, 151.0)
        map?.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        map?.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}