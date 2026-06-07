package com.example.ui.utils

import android.text.format.DateUtils

fun getRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val difference = now - timestamp
    
    return when {
        difference < DateUtils.MINUTE_IN_MILLIS -> "Před chvílí"
        difference < DateUtils.HOUR_IN_MILLIS -> "Před ${difference / DateUtils.MINUTE_IN_MILLIS} min"
        difference < DateUtils.DAY_IN_MILLIS -> "Před ${difference / DateUtils.HOUR_IN_MILLIS} hod"
        difference < 2 * DateUtils.DAY_IN_MILLIS -> "Včera"
        else -> DateUtils.getRelativeTimeSpanString(
            timestamp,
            now,
            DateUtils.DAY_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }
}
