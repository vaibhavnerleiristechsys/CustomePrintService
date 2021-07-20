package com.printerlogic.printerlogic

import android.app.Application
import com.printerlogic.printerlogic.room.AppDatabase

class PrintService:Application() {

    fun dbInstance() = AppDatabase.getInstance(this)
}
