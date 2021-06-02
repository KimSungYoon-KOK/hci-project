package com.android.hciproject.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import com.naver.maps.geometry.LatLng
import java.io.IOException
import kotlin.math.round
import kotlin.math.roundToInt

object LocationUtils {
    fun addressToLocation(searchWord: String, context: Context): LatLng? {
        val geocoder = Geocoder(context)
        val addresses: List<Address>

        try {
            addresses = geocoder.getFromLocationName(searchWord, 1)
            if (addresses.isNotEmpty())
                return LatLng(addresses[0].latitude, addresses[0].longitude)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    fun distanceToText(distance: Float): String {
        return if (distance >= 1000) {
            "나로부터 " + (distance / 1000).roundToInt().toString() + "km"
        } else {
            "나로부터 " + round(distance).roundToInt().toString() + "m"
        }
    }

    fun getDistance(first: LatLng, second: LatLng): Float {

        val firstLoc = Location(LocationManager.NETWORK_PROVIDER)
        val secondLoc = Location(LocationManager.NETWORK_PROVIDER)

        firstLoc.latitude = first.latitude
        firstLoc.longitude = first.longitude
        secondLoc.latitude = second.latitude
        secondLoc.longitude = second.longitude

        return firstLoc.distanceTo(secondLoc)

    }
}