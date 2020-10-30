package com.example.customeprintservice.utils

import android.util.Log

object DateTime {
    fun getDisplayableTime(delta: Long): String? {
        var difference: Long = 0
        val mDate = System.currentTimeMillis()
        Log.i("printer", "mDate=>$mDate")
        if (mDate > delta) {
            difference = mDate - delta
            val seconds = difference / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            val months = days / 31
            val years = days / 365
            return if (seconds < 0) {
                "not yet"
            } else if (seconds < 60) {
                if (seconds == 1L) "now" else " now"
            } else if (seconds < 120) {
                "a minute ago"
            } else if (seconds < 2700) {
                "$minutes minutes ago"
            } else if (seconds < 5400) {
                "an hour ago"
            } else if (seconds < 86400) {
                "$hours hours ago"
            } else if (seconds < 172800) {
                "yesterday"
            } else if (seconds < 2592000) {
                "$days days ago"
            } else if (seconds < 31104000) {
                if (months <= 1) "one month ago" else "$days months ago"
            } else {
                if (years <= 1) "one year ago" else "$years years ago"
            }
        }
        return null
    }
}