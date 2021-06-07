package com.android.hciproject.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.android.hciproject.R
import java.util.*

object ColorUtils {
    val redColors = arrayOf(
        R.color.greenColor100,
        R.color.greenColor200,
        R.color.greenColor300,
        R.color.greenColor400,
        R.color.greenColor500,
        R.color.greenColor600,
        R.color.greenColor700,
        R.color.greenColor800
    )

    fun getColorFromTime(time: Int): Int {
        return when {
            time < 3 -> redColors[7]
            time < 6 -> redColors[6]
            time < 9 -> redColors[5]
            time < 12 -> redColors[4]
            time < 15 -> redColors[3]
            time < 18 -> redColors[2]
            time < 21 -> redColors[1]
            else -> redColors[0]
        }
    }
}