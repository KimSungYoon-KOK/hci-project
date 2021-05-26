package com.android.hciproject.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.naver.maps.geometry.LatLng
import java.io.IOException

object LocationUtils {
    fun addressToLocation(searchWord: String, context: Context): LatLng? {
        val geocoder = Geocoder(context)
        val addresses: List<Address>

        try {
            addresses = geocoder.getFromLocationName(searchWord, 1)
            if(addresses.isNotEmpty())
                return LatLng(addresses[0].latitude, addresses[0].longitude)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }
}