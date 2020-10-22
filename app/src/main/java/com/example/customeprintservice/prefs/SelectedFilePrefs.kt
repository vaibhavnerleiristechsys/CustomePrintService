package com.example.customeprintservice.prefs

import android.content.Context
import android.content.SharedPreferences
import com.example.customeprintservice.utils.Constants
import org.json.JSONArray


class SelectedFilePrefs {

    private val SAVEFILELIST = "saveFileList"

    fun SaveFileList( value: HashSet<String>, context: Context) {
        val selectedFileLst = context.getSharedPreferences(SAVEFILELIST, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = selectedFileLst.edit()
        editor.putStringSet(SAVEFILELIST, value)
        editor.apply()
    }

    fun getFileList(context: Context): MutableSet<String>? {
        val sharedPreferences = context.getSharedPreferences(SAVEFILELIST, Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet(SAVEFILELIST, null)
    }
}