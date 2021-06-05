package com.android.hciproject.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.android.hciproject.R
import java.util.*

object ColorUtils {
    val redColors = arrayOf(
        R.color.redColor100,
        R.color.redColor200,
        R.color.redColor300,
        R.color.redColor400,
        R.color.redColor500,
        R.color.redColor600,
        R.color.redColor700,
        R.color.redColor800
    )

    fun getColorFromTime(time: Int): Int {
//        return when {
//            time < 50 -> redColors[7]
//            time < 10 -> redColors[6]
//            time < 15 -> redColors[5]
//            time < 20 -> redColors[4]
//            time < 25 -> redColors[3]
//            time < 30 -> redColors[2]
//            time < 40 -> redColors[1]
//            else -> redColors[0]
//        }
        return redColors[Random().nextInt(8)]
    }
}