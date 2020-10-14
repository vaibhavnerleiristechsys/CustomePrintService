package com.example.customeprintservice.signin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.customeprintservice.R
import kotlinx.android.synthetic.main.activity_sign_in_company.*

class SignInCompany : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in_company)
        btnNextSignInCompany.setOnClickListener {
            val intent = Intent(this@SignInCompany, SignInActivity::class.java)
            startActivity(intent)
        }
    }
}