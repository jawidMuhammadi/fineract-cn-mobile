package org.apache.fineract.data.models.geolocation

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by Ahmad Jawid Muhammadi on 15/7/20.
 */

@Parcelize
data class UserLocation(
        @SerializedName("user_id")
        var userId: Int? = null,

        @SerializedName("client_name")
        var clientName: String? = null,

        @SerializedName("address")
        var address: String? = null,

        @SerializedName("latlng")
        var latlng: UserLatLng? = null,

        @SerializedName("start_time")
        var startTime: String? = null,

        @SerializedName("stop_time")
        var stopTime: String? = null,

        @SerializedName("date")
        var date: String? = null

) : Parcelable {
    var dateFormat = "dd MMMM yyyy HH:mm"
    var locale = "en"
}

