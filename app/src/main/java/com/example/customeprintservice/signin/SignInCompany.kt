package com.example.customeprintservice.signin

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.customeprintservice.MainActivity
import com.example.customeprintservice.R
import com.example.customeprintservice.model.IdpResponse
import com.example.customeprintservice.prefs.LoginPrefs
import com.example.customeprintservice.prefs.SignInCompanyPrefs
import com.example.customeprintservice.rest.ApiService
import com.example.customeprintservice.rest.RetrofitClient
import com.example.customeprintservice.utils.CheckInternetConnection
import com.example.customeprintservice.utils.GoogleAPI
import com.example.customeprintservice.utils.HideKeyboard
import com.example.customeprintservice.utils.ProgressDialog
import kotlinx.android.synthetic.main.activity_sign_in_company.*
import org.jetbrains.anko.toast
import org.slf4j.LoggerFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SignInCompany : AppCompatActivity() {

    private var bundle = Bundle()
    var logger = LoggerFactory.getLogger(SignInActivity::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (LoginPrefs.getOCTAToken(this@SignInCompany) != null) {
            val intent = Intent(this@SignInCompany, MainActivity::class.java)
            startActivity(intent)
        }
        setContentView(R.layout.activity_sign_in_company)
        Selection.setSelection(edtYourCompany.getText(), edtYourCompany.getText().length);
           val companyUrl =LoginPrefs.getCompanyUrl(this@SignInCompany)
        if(companyUrl !=null){
            edtYourCompany.setText(companyUrl.toString())
            edtYourCompany.setSelection(edtYourCompany.getText().length);
        }


        edtYourCompany.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if(!s.toString().startsWith("https://")){
                    Selection.setSelection(edtYourCompany.getText(), edtYourCompany.getText().length);
                }


            }
            override  fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })


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
            if(edtYourCompany.text.toString().isEmpty()){
                toast("please enter url")
                ProgressDialog.cancelLoading()

            }
            else{
                   var url= edtYourCompany.text.toString()
                    val stringurl=url.substring(0,url.length)
                    LoginPrefs.saveCompanyUrl(this@SignInCompany, stringurl.toString())
                    val siteId: String = MainActivity.findSiteId(stringurl)
                    LoginPrefs.saveSiteId(this@SignInCompany, siteId.toString())

                    if(!url.contains("https://")){
                        url= "https://"+url
                    }
                    if(!url.contains("/api/idp")){
                        url= url +"/api/idp"
                    }



                    getIdpInfo(url)
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

    private fun getIdpInfo(url:String) {
        val BASE_URL: String = (url + "/")
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
                    logger.info("printer"+ "response of api==>" + response.isSuccessful)
                    if(edtYourCompany.text.toString().contains("devncoldap")){
                        bundle.putString("buttonName", "LDAP")
                    }

                    val list: List<IdpResponse>? = response.body()?.toList()
                    var size=0
                    if (list != null) {
                        size =list.size
                    }
                    list?.forEach { idp ->
                        run {
                            bundle.putString("desktopLoginUrl", idp.desktopLoginUrl)
                            bundle.putString("buttonName", idp.name)
                            bundle.putString("clientId",idp.client_id)
                            bundle.putString("token_uri",idp.tokenUri)

                            SignInCompanyPrefs.saveIdpUrl(
                                this@SignInCompany,
                                idp.tokenUri.toString()
                            )
                            LoginPrefs.savegoogleTokenUrl(this@SignInCompany,idp.tokenUri.toString())
                            SignInCompanyPrefs.saveIdpName(this@SignInCompany, idp.idp_type.toString())
                            SignInCompanyPrefs.saveIdpType(
                                this@SignInCompany,
                                idp.authType.toString()
                            )
                        }
                    }


                    if(size==0){
                        if(!url.contains("ldap")) {
                            toast("please check url")
                        }
                        if(url.contains("ldap")){
                            val intent = Intent(this@SignInCompany, SignInActivity::class.java)
                            intent.putExtras(bundle)
                            startActivity(intent)
                        }
                    }else {
                        toast("Idp response getting Successful")
                        val intent = Intent(this@SignInCompany, SignInActivity::class.java)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    }
                } else {
                    toast("Response is Not Successful")
                    ProgressDialog.cancelLoading()

                }
            }

            override fun onFailure(call: Call<List<IdpResponse>>, t: Throwable) {
                ProgressDialog.cancelLoading()
                toast("Idp response ${t.message}")
                Log.i("printer", "Error response of api==>" + t.message)
                logger.info("printer"+ "Error response of api==>" + t.message)
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


}

//https://docs.microsoft.com/en-us/azure/active-directory/develop/tutorial-v2-android