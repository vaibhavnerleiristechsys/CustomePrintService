package com.printerlogic.printerlogic.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Permissions {

    val REQUEST_ID_MULTIPLE_PERMISSIONS = 1

    fun checkAndRequestPermissions(context: Context): Boolean {
        val storage = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE)

        val listPermissionsNeeded: ArrayList<String> = ArrayList()

        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                context as Activity, listPermissionsNeeded.toTypedArray
                    (), REQUEST_ID_MULTIPLE_PERMISSIONS
            )
            return false
        }
        return true
    }


}