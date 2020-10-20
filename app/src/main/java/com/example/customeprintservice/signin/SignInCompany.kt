package com.example.customeprintservice.signin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.customeprintservice.R
import com.example.customeprintservice.model.IdpResponse
import com.example.customeprintservice.prefs.LoginPrefs
import com.example.customeprintservice.print.BottomNavigationActivity
import com.example.customeprintservice.rest.ApiService
import com.example.customeprintservice.rest.RetrofitClient
import com.example.customeprintservice.utils.CheckInternetConnection
import com.example.customeprintservice.utils.ProgressDialog
import kotlinx.android.synthetic.main.activity_sign_in_company.*
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SignInCompany : AppCompatActivity() {

    private var bundle = Bundle()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (LoginPrefs.getOCTAToken(this@SignInCompany) != null) {
            val intent = Intent(this@SignInCompany, BottomNavigationActivity::class.java)
            startActivity(intent)
        }
        setContentView(R.layout.activity_sign_in_company)
        setAlpha()
        if (CheckInternetConnection().isNetworkConnected(this@SignInCompany)) {
            if (LoginPrefs.getOCTAToken(this@SignInCompany) == null) {
                ProgressDialog.showLoadingDialog(this@SignInCompany, "Loading")
                getIdpInfo()
            }
        } else {
            toast("No Internet Connection")
        }
        btnNextSignInCompany.setOnClickListener {
            if (CheckInternetConnection().isNetworkConnected(this@SignInCompany)) {
                val intent = Intent(this@SignInCompany, SignInActivity::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            } else {
                toast("No Internet Connection")
            }
        }
    }

    private fun setAlpha() {
        edtYourCompany.doOnTextChanged { text, start, before, count ->

            if (start == 0) {
                edtYourCompany.alpha = 0.75F
                btnNextSignInCompany.setBackgroundResource(R.drawable.button_change_color)
                btnNextSignInCompany.setTextColor(resources.getColor(R.color.white))
            }
            if (text?.length == 0) {
                edtYourCompany.alpha = 0.42F
                btnNextSignInCompany.setBackgroundResource(R.drawable.button_radius)
                btnNextSignInCompany.setTextColor(resources.getColor(R.color.silver))
            }
        }
    }

    private fun getIdpInfo() {
        val BASE_URL: String = "https://devncookta.printercloud.com/api/"
        val apiService = RetrofitClient.getRetrofitInstance(BASE_URL).create(ApiService::class.java)
        val call = apiService.getIdpResponse()

        call.enqueue(object : Callback<List<IdpResponse>> {
            override fun onResponse(
                call: Call<List<IdpResponse>>,
                response: Response<List<IdpResponse>>
            ) {
                if (response.code() == 200) {
                    ProgressDialog.cancelLoading()
                    Log.i("printer", "response of api==>" + response.isSuccessful)
                    val list: List<IdpResponse>? = response.body()?.toList()

                    list?.forEach { idp ->
                        run {
                            bundle.putString("desktopLoginUrl", idp.desktopLoginUrl)
                        }
                    }
                    toast("Idp response getting Success")
                } else {
                    toast("Response is Not Successful")
                }
            }

            override fun onFailure(call: Call<List<IdpResponse>>, t: Throwable) {
                ProgressDialog.cancelLoading()
                toast("Idp response ${t.message}")
                Log.i("printer", "Error response of api==>" + t.message)
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}