package com.example.customeprintservice.signin

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.customeprintservice.R
import com.example.customeprintservice.print.BottomNavigationActivity
import com.example.customeprintservice.utils.HideKeyboard
import kotlinx.android.synthetic.main.activity_sign_in.*
import org.jetbrains.anko.toast

class SignInActivity : AppCompatActivity() {

    private var isShowPass = false
    private var bundle = Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        try {
            bundle = intent.extras!!

            if (bundle.getString("buttonName") == "Okta") {
                btnSignInWithOkta.text = bundle.getString("buttonName")
                btnSignInWithOkta.setBackgroundResource(R.drawable.button_sign_in_okta)
                val drawable = baseContext.resources.getDrawable(R.mipmap.icon_okta)
                btnSignInWithOkta.setCompoundDrawablesWithIntrinsicBounds(
                    drawable, null, null, null)
                btnSignInWithOkta.gravity = Gravity.CENTER
            } else if (bundle.getString("buttonName") == "Azure AD") {
                btnSignInWithOkta.text = bundle.getString("buttonName")
                btnSignInWithOkta.setBackgroundResource(R.drawable.button_sign_in_azure)
                val drawable = baseContext.resources.getDrawable(R.mipmap.icon_azure)
                btnSignInWithOkta.setCompoundDrawablesWithIntrinsicBounds(
                    drawable, null, null, null)
                btnSignInWithOkta.gravity = Gravity.CENTER
            }else if(bundle.getString("buttonName")=="Google"){
                btnSignInWithOkta.text = bundle.getString("buttonName")
                btnSignInWithOkta.setBackgroundResource(R.drawable.button_sign_in_google)
                val drawable = baseContext.resources.getDrawable(R.mipmap.icon_google)
                btnSignInWithOkta.setCompoundDrawablesWithIntrinsicBounds(
                    drawable, null, null, null)
                btnSignInWithOkta.gravity = Gravity.CENTER
            }
        } catch (e: Exception) {
            Log.i("printer", "exception=>$e")
            toast("exception=>$e")
        }
        supportActionBar?.setHomeAsUpIndicator(R.drawable.button_sign_in_google)
        setAlphaEdtUserName()
        setAlphaEdtPassword()
        imgShowPassword.setOnClickListener {
            isShowPass = !isShowPass
            showPassword(isShowPass)
        }

        showPassword(isShowPass)

        btnSignIn.setOnClickListener {
            HideKeyboard.hideKeyboard(this@SignInActivity)
            val intent = Intent(this@SignInActivity, BottomNavigationActivity::class.java)
            startActivity(intent)
        }

        val desktopUrl: String? = bundle.getString("desktopLoginUrl")
        Log.i("printer", "desktopUrl--->${desktopUrl}")
        btnSignInWithOkta.setOnClickListener {
            searchWeb(desktopUrl)
        }
    }

    private fun searchWeb(query: String?) {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, query)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun setAlphaEdtUserName() {
        edtUserName.doOnTextChanged { text, start, before, count ->
            if (start == 0) {
                edtUserName.alpha = 0.75F
            }
            if (count == 0) {
                edtUserName.alpha = 0.42F
            }
        }
    }

    private fun setAlphaEdtPassword() {
        edtPassword.doOnTextChanged { text, start, before, count ->
            if (start == 0) {
                edtPassword.alpha = 0.75F
                if (edtUserName.text.isNotEmpty()) {
                    btnSignIn.setBackgroundResource(R.drawable.button_change_color)
                    btnSignIn.setTextColor(resources.getColor(R.color.white))
                }
            }
            if (text?.length == 0) {
                edtPassword.alpha = 0.42F
                btnSignIn.setBackgroundResource(R.drawable.button_radius)
                btnSignIn.setTextColor(resources.getColor(R.color.silver))
            }
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

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}