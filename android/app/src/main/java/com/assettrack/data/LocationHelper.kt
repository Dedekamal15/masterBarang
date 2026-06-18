package com.assettrack.data

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class GpsLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float
)

@Singleton
class LocationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): GpsLocation? = suspendCancellableCoroutine { cont ->
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(GpsLocation(location.latitude, location.longitude, location.accuracy))
                } else {
                    fusedClient.lastLocation
                        .addOnSuccessListener { last ->
                            cont.resume(last?.let { GpsLocation(it.latitude, it.longitude, it.accuracy) })
                        }
                        .addOnFailureListener { cont.resume(null) }
                }
            }
            .addOnFailureListener { cont.resume(null) }
    }
}
