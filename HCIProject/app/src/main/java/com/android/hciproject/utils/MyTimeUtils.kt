package com.android.hciproject.utils

import android.content.ReceiverCallNotAllowedException
import okhttp3.internal.format
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

object MyTimeUtils {
    fun getTimeInMillis(s: String): Long {
        return try {
            val time = s.substring(0, 18)
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val date: Date = formatter.parse(time)
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.timeInMillis
        } catch (e: Exception) {
            0
        }
    }

    fun getTimeDiff(postTime: Long): Int {
        val nowTime = Calendar.getInstance().timeInMillis
        val diff = nowTime - postTime
        return (diff / (60 * 60 * 1000)).toInt() - 6
    }
}