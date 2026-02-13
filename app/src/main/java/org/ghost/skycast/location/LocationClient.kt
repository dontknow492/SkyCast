package org.ghost.skycast.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * A simple "Library" class to handle fetching the device location.
 * It wraps the Google FusedLocationProviderClient in a clean coroutine.
 */
interface LocationClient {
    suspend fun getCurrentLocation(): Location?

    class LocationException(message: String) : Exception(message)
}

class DefaultLocationClient(
    private val context: Context,
    private val client: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
        context
    )
) : LocationClient {

    @SuppressLint("MissingPermission") // We assume permission is checked before calling this
    override suspend fun getCurrentLocation(): Location? {
        // 1. Check if user has GPS enabled in system settings
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            throw LocationClient.LocationException("GPS is disabled. Please enable location services.")
        }

        // 2. Fetch the location using a coroutine
        return suspendCancellableCoroutine { continuation ->
            client.lastLocation
                .addOnSuccessListener { location ->
                    // Note: location can be null if the GPS was just turned on and hasn't fixed yet
                    continuation.resume(location)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
                .addOnCanceledListener {
                    continuation.cancel()
                }
        }
    }
}