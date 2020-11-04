package com.example.customeprintservice.signin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.customeprintservice.R
import com.example.customeprintservice.jipp.MainActivity

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