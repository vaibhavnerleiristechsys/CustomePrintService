package com.example.customeprintservice

import android.app.Application
import com.example.customeprintservice.room.AppDatabase

class PrintService:Application() {

    fun dbInstance() = AppDatabase.getInstance(this)
}
