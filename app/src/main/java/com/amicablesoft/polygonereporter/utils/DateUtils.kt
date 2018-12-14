package com.amicablesoft.polygonereporter.utils

import java.text.SimpleDateFormat
import java.util.*

private const val DATE_FORMAT = "dd.MM.yyyy"
private const val SECOND = 1000
private const val MINUTE = SECOND * 60
private const val HOUR = MINUTE * 60
private const val DAY = HOUR * 24
private const val WEEK = DAY * 7

fun getDateFormat(date: Date): String {
    return SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(date)
}

fun Date.isValid(): Boolean {
    val dif = (Date().time - this.time)
    return dif < WEEK
}
