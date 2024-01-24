package kr.co.htap.navigation.location

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import kr.co.htap.navigation.reservation.BranchEntity

/**
 *
 * @author eunku
 */
class LocationProvider(private val context: Context) {
    private val REQUEST_LOCATION = 1
    private var locations: Location? = null
    init {
        locations = getLocation()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val permissionsLocationUpApi29Impl = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
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
                ) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    permissionsLocationUpApi29Impl[2]
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    permissionsLocationUpApi29Impl,
                    REQUEST_LOCATION
                )
                Log.d("test111", "1번")
            } else Log.d("test", "null값인가")
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
                Log.d("test", "22번")
            } else Log.d("test", "null값인가")
        }
    }

    @SuppressLint("MissingPermission")
    public fun getLocation(): Location? {
        var fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { success: Location? ->
                success?.let { location ->
                    this.locations = location }
            }
            .addOnFailureListener { fail ->
                Log.d("testtest", "실패")

            }
        return this.locations
    }

    fun getDistance(branch: BranchEntity, textView: TextView) {

        val endPoint = Location("endPoint")
        endPoint.apply {
            latitude = branch.latitude
            longitude = branch.longitude
        }
        if (locations != null) {
            textView.text = "${String.format("%0.1f", this.locations!!.distanceTo(endPoint) / 1000)}KM"
        } else {
            Log.d("test", "오류")
        }
    }
}