package com.example.customeprintservice.utils

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager

class HideKeyboard {
    companion object {
        fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            var view = activity.currentFocus
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}