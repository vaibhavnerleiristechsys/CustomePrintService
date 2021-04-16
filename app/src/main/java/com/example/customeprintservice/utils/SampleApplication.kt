package com.example.customeprintservice.utils
import android.app.Application
import android.util.Log
import com.datadog.android.Datadog
import com.datadog.android.core.configuration.Configuration
import com.datadog.android.core.configuration.Credentials
import com.datadog.android.privacy.TrackingConsent

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val configuration = Configuration.Builder(true,true,true,true).build()
        val credentials = Credentials("pub44227d305d57f155395314dc2415aff2","VasionPrint","admin","com.example.customeprintservice")
        Datadog.initialize(this, credentials, configuration, TrackingConsent.GRANTED)
        Datadog.setVerbosity(Log.INFO)
    }
}
