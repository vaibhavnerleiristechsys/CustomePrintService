package com.example.customeprintservice.signin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.customeprintservice.R

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Handler().postDelayed({
            val mainIntent = Intent(this@SplashScreenActivity, SignInCompany::class.java)
            startActivity(mainIntent)
            finish()
        }, 500)
    }
}