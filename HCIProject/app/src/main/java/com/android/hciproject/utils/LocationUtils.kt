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

    fun getDistance(first: LatLng, second: LatLng): Float { // 미터 계산

        val firstLoc = Location(LocationManager.NETWORK_PROVIDER)
        val secondLoc = Location(LocationManager.NETWORK_PROVIDER)

        firstLoc.latitude = first.latitude
        firstLoc.longitude = first.longitude
        secondLoc.latitude = second.latitude
        secondLoc.longitude = second.longitude

        return firstLoc.distanceTo(secondLoc)

    }

    fun getAverageLatLng(list: ArrayList<LatLng>): LatLng {
        var avgLat = 0.0
        var avgLng = 0.0
        for (latLng in list) {
            avgLat += latLng.latitude
            avgLng += latLng.longitude
        }
        return LatLng(avgLat / list.size, avgLng / list.size)
    }

    fun getMaxDistance(list: ArrayList<LatLng>): Double {
        var avgLat = 0.0
        var avgLng = 0.0
        for (latLng in list) {
            avgLat += latLng.latitude
            avgLng += latLng.longitude
        }
        val target = LatLng(avgLat / list.size, avgLng / list.size)

        var maxDistance = 0.0
        for (value in list) {
            val distance = getDistance(value, target).toDouble()
            if (distance > maxDistance) {
                maxDistance = distance
            }
        }
        return maxDistance
    }
}