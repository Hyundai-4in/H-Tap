package kr.co.htap.navigation.location

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices


/**
 *
 * @author eunku
 */
class LocationProvider(private val context: Context) {
    private val REQUEST_LOCATION = 1
    private var locations: Location? = null
    private var fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    init {
        locations = getLocation()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val permissionsLocationUpApi29Impl = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @TargetApi(Build.VERSION_CODES.P)
    private val permissionsLocationDownApi29Impl = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun requestLocation() {
        if (Build.VERSION.SDK_INT >= 29) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    permissionsLocationUpApi29Impl[0]
                ) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                    context,
                    permissionsLocationUpApi29Impl[1]
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    permissionsLocationUpApi29Impl,
                    REQUEST_LOCATION
                )
            }
        } else if (Build.VERSION.SDK_INT < 29) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    permissionsLocationDownApi29Impl[0]
                ) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                    context,
                    permissionsLocationDownApi29Impl[1]
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    permissionsLocationDownApi29Impl,
                    REQUEST_LOCATION
                )
            }
        }
    }

    fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= 29) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    permissionsLocationUpApi29Impl[1]
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return true
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    permissionsLocationDownApi29Impl[1]
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return true
            }
        }
        return false
    }

    @SuppressLint("MissingPermission")
    public fun getLocation(): Location? {
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { success: Location? ->
                success?.let { location ->
                    this.locations = location
                }
            }
            .addOnFailureListener { fail ->
                Log.d("getLocation", "실패")

            }
        return this.locations
    }

    fun getDistance(branch: BranchEntity): Double {

        val endPoint = Location("endPoint")
        endPoint.apply {
            latitude = branch.latitude
            longitude = branch.longitude
        }
        try {
            return locations!!.distanceTo(endPoint).toDouble()
        } catch (e: Exception) {
            return 0.0
        }
    }

    @SuppressLint("MissingPermission")
    fun updateLocation() {
        try {
            val locationRequest =
                LocationRequest.Builder(1).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .build()
            var locationCallback = object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    locations = p0.lastLocation
                }
            }
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            Log.d("test", "update 위치 실패")
        }
    }

}