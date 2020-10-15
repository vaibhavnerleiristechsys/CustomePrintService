package com.example.customeprintservice.signin

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.customeprintservice.R
import com.example.customeprintservice.jipp.MainActivity
import com.example.customeprintservice.print.BottomNavigationActivity
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() {

    private var isShowPass = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.button_sign_in_google)

        imgShowPassword.setOnClickListener {
            isShowPass = !isShowPass
            showPassword(isShowPass)
        }

        showPassword(isShowPass)

        btnSignIn.setOnClickListener {
            val intent = Intent(this@SignInActivity, BottomNavigationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showPassword(isShow: Boolean) {
        if (isShow) {
            edtPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            imgShowPassword.setImageResource(R.mipmap.visible)
        } else {
            edtPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            imgShowPassword.setImageResource(R.mipmap.hidden)
        }
        edtPassword.setSelection(edtPassword.text.toString().length)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}