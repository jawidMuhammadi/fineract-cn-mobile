package org.apache.fineract.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.item_client_location.view.*
import org.apache.fineract.R
import org.apache.fineract.data.models.geolocation.UserLocation
import org.apache.fineract.injection.ApplicationContext
import javax.inject.Inject


/**
 * Created by Ahmad Jawid Muhammadi on 16/7/20.
 */

class VisitedClientLocationAdapter @Inject constructor(
        @ApplicationContext val context: Context
) : ListAdapter<UserLocation, ViewHolder>(ItemDiffUtil()) {

    companion object {
        class ItemDiffUtil : DiffUtil.ItemCallback<UserLocation>() {
            override fun areItemsTheSame(oldItem: UserLocation, newItem: UserLocation): Boolean {
                return oldItem.userId == newItem.userId
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: UserLocation, newItem: UserLocation): Boolean {
                return oldItem === oldItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), context)
    }

}

class ViewHolder private constructor(itemView: View)
    : RecyclerView.ViewHolder(itemView) {

    companion object {
        fun from(parent: ViewGroup): ViewHolder {
            return ViewHolder(LayoutInflater.from(
                    parent.context).inflate(R.layout.item_client_location, parent, false)
            )
        }
    }

    fun bind(userLocation: UserLocation, context: Context) {
        itemView.tvClientName.text = userLocation.clientName
        itemView.tvVisitedDate.text = userLocation.date
        itemView.tvAddress.text = userLocation.address
        itemView.mapView.onCreate(null)
        itemView.mapView.getMapAsync { map ->
            map?.addMarker(MarkerOptions().position(
                    LatLng(userLocation.latlng?.lat!!,
                            userLocation.latlng?.lng!!))
            )
        }
        MapsInitializer.initialize(context)
        itemView.mapView.onResume()
        itemView.mapView.postInvalidate()
    }
}