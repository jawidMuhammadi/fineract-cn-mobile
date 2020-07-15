package org.apache.fineract.data.models.geolocation

import com.google.gson.annotations.SerializedName

/**
 * Created by Ahmad Jawid Muhammadi on 15/7/20.
 */

data class UserLatLng(
        @SerializedName("lat")
        var lat: Double? = null,

        @SerializedName("lng")
        var lng: Double? = null
)