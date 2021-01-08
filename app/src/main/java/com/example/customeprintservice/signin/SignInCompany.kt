package com.example.customeprintservice.signin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.customeprintservice.MainActivity
import com.example.customeprintservice.R
import com.example.customeprintservice.model.IdpResponse
import com.example.customeprintservice.prefs.LoginPrefs
import com.example.customeprintservice.prefs.SignInCompanyPrefs
import com.example.customeprintservice.print.BottomNavigationActivity
import com.example.customeprintservice.rest.ApiService
import com.example.customeprintservice.rest.RetrofitClient
import com.example.customeprintservice.utils.CheckInternetConnection
import com.example.customeprintservice.utils.GoogleAPI
import com.example.customeprintservice.utils.HideKeyboard
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
        //    val intent = Intent(this@SignInCompany, BottomNavigationActivity::class.java)
            val intent = Intent(this@SignInCompany, MainActivity::class.java)
            startActivity(intent)
        }
        setContentView(R.layout.activity_sign_in_company)

        setAlpha()

        btnNextSignInCompany.setOnClickListener {
            HideKeyboard.hideKeyboard(this@SignInCompany)
            if (CheckInternetConnection.isNetworkConnected(this@SignInCompany)) {
                if (LoginPrefs.getOCTAToken(this@SignInCompany) == null) {
                    ProgressDialog.showLoadingDialog(this@SignInCompany, "Loading")
                    checkValidation()

                   if(edtYourCompany.text.toString().contains("googleid")) {
                      GoogleAPI.getGoogleData(this@SignInCompany)
                    }
                }

            } else {
                toast("No Internet Connection")
            }
        }
    }


    private fun checkValidation() {
        when {
            edtYourCompany.text.toString().isEmpty() -> edtYourCompany.error = "Enter URL"
            else -> {
                if(edtYourCompany.text.toString().equals("https://devncookta.printercloud.com/api/idp") ||
                   edtYourCompany.text.toString().equals("https://devncoazure.printercloud.com/api/idp") ||
                   edtYourCompany.text.toString().equals("https://devncoping.printercloud.com/api/idp") ||
                   edtYourCompany.text.toString().equals("https://googleid.printercloud.com/api/idp")   ||
                    edtYourCompany.text.toString().equals("https://devncoldap.printercloud.com/api/idp")) {
                    getIdpInfo()
                }else{
                    toast("please check url")
                    ProgressDialog.cancelLoading()
                }

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
        val BASE_URL: String = (edtYourCompany.text.toString() + "/")
        val apiService = RetrofitClient(this@SignInCompany)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)
        val call = apiService.getIdpResponse()

        call.enqueue(object : Callback<List<IdpResponse>> {
            override fun onResponse(
                call: Call<List<IdpResponse>>,
                response: Response<List<IdpResponse>>
            ) {
                if (response.code() == 200) {
                    ProgressDialog.cancelLoading()
                    Log.i("printer", "response of api==>" + response.isSuccessful)

                    if(edtYourCompany.text.toString().contains("devncoldap")){

                        bundle.putString("buttonName", "LDAP")
                    }

                    val list: List<IdpResponse>? = response.body()?.toList()

                    list?.forEach { idp ->
                        run {
                            bundle.putString("desktopLoginUrl", idp.desktopLoginUrl)
                            bundle.putString("buttonName", idp.name)
                            bundle.putString("clientId",idp.client_id)
                            SignInCompanyPrefs.saveIdpUrl(
                                this@SignInCompany,
                                idp.tokenUri.toString()
                            )
                            SignInCompanyPrefs.saveIdpName(this@SignInCompany, idp.name.toString())
                            SignInCompanyPrefs.saveIdpType(
                                this@SignInCompany,
                                idp.authType.toString()
                            )
                        }
                    }
                    toast("Idp response getting Successful")
                    val intent = Intent(this@SignInCompany, SignInActivity::class.java)
                    intent.putExtras(bundle)
                    startActivity(intent)
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

//https://docs.microsoft.com/en-us/azure/active-directory/develop/tutorial-v2-android