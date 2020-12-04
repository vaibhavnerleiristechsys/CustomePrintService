package com.example.customeprintservice.signin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.customeprintservice.R
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        firebaseAnalytics = Firebase.analytics

        Handler().postDelayed({
            val mainIntent = Intent(this@SplashScreenActivity, SignInCompany::class.java)
            startActivity(mainIntent)
            finish()
        }, 500)

        val bundle = Bundle()
        bundle.putString("test", "testSiddhesh")

        firebaseAnalytics.setDefaultEventParameters(bundle)
    }
}