package org.apache.fineract.ui.online.geo_location.visit_customer

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import org.apache.fineract.data.datamanager.api.DataManagerGeolocation
import org.apache.fineract.data.models.geolocation.UserLocation

/**
 * Created by Ahmad Jawid Muhammadi on 20/7/20.
 */

class VisitCustomerViewModel constructor(
        private val dataManagerGeolocation: DataManagerGeolocation
) : ViewModel() {

    private val job = Job()
    private val uiScope = CoroutineScope(job + Dispatchers.Default)

    fun saveLastKnownLocation(userLocation: UserLocation) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                dataManagerGeolocation.saveLastKnownLocation(userLocation)
            }
        }
    }

    fun saveLocationPath(userLocation: UserLocation) {
        uiScope.launch {
            try {
                dataManagerGeolocation.saveLocationPathAsync(userLocation).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}