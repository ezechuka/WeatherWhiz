package com.javalon.weatherwhiz.utils

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext

class GpsUtils(@ApplicationContext private val context: Context) {
    private val TAG: String? = context.packageName
    private var settingsClient: SettingsClient
    private var locationManager: LocationManager
    private var locationSettingRequest: LocationSettingsRequest
    private var locationRequest: LocationRequest

    init {
        settingsClient = LocationServices.getSettingsClient(context)
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationRequest = LocationRequest.create()

        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10_000
        locationRequest.fastestInterval = 2_000
        val locationSettingsBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        locationSettingRequest = locationSettingsBuilder.build()

        locationSettingsBuilder.setAlwaysShow(true)
    }

    fun turnOnGps(gpsListener: OnGpsListener) {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (gpsListener != null) {
                gpsListener.gpsStatus(true)
            }
        } else {
            settingsClient.checkLocationSettings(locationSettingRequest)
                .addOnSuccessListener {
                    if (gpsListener != null) {
                        gpsListener.gpsStatus(true)
                    }
                }.addOnFailureListener {
                    val statusCode = (it as (ApiException)).statusCode
                    when (statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            try {
                                val rae = (it as (ResolvableApiException))
                                rae.startResolutionForResult(context as Activity?, Constants.APP_GPS_REQUEST)

                            } catch (sie: IntentSender.SendIntentException) {
                                Log.i(TAG, "PendingIntent unable to execute request.")
                            }
                        }

                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            val errorMessage = "Location settings are inadequate, and cannot be " +
                                    "fixed here. Fix in Settings.";
                            Log.e(TAG, errorMessage);
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                }
        }
    }

    interface OnGpsListener {
        fun gpsStatus(isGPSEnabled: Boolean)
    }
}