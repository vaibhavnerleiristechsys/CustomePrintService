package com.printerlogic.printerlogic.signin

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.datadog.android.Datadog
import com.datadog.android.core.configuration.Configuration
import com.datadog.android.core.configuration.Credentials
import com.datadog.android.privacy.TrackingConsent
import com.printerlogic.printerlogic.MainActivity
import com.printerlogic.printerlogic.R
import com.printerlogic.printerlogic.model.IdpResponse
import com.printerlogic.printerlogic.prefs.LoginPrefs
import com.printerlogic.printerlogic.prefs.SignInCompanyPrefs
import com.printerlogic.printerlogic.print.ServerPrintRelaseFragment
import com.printerlogic.printerlogic.rest.ApiService
import com.printerlogic.printerlogic.rest.RetrofitClient
import com.google.gson.JsonObject
import com.google.gson.internal.LinkedTreeMap
import com.printerlogic.printerlogic.utils.*
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
        val configuration = Configuration.Builder(true, true, true, true).build()
        val credentials = Credentials(
            "pube211f0482bac89ed37305673b5e0928e",
            "VasionPrint",
            "admin",
            "com.example.customeprintservice"
        )
        Datadog.initialize(this@SignInCompany, credentials, configuration, TrackingConsent.GRANTED)

        Datadog.setVerbosity(Log.INFO)

        if (LoginPrefs.getOCTAToken(this@SignInCompany) != null) {
                val serverPrintRelaseFragment = ServerPrintRelaseFragment()
                serverPrintRelaseFragment.serverCallForGettingAllPrinters(this@SignInCompany)
                val intent = Intent(this@SignInCompany, MainActivity::class.java)
                startActivity(intent)

        }else{
            @SuppressLint("WrongConstant")val sh: SharedPreferences = getSharedPreferences(
                "MySharedPref",
                Context.MODE_APPEND
            )
            val IsLdap = sh.getString("IsLdap", "")
            if(IsLdap.equals("LDAP")){
                val serverPrintRelaseFragment = ServerPrintRelaseFragment()
                serverPrintRelaseFragment.serverCallForGettingAllPrinters(this@SignInCompany)
                val intent = Intent(this@SignInCompany, MainActivity::class.java)
                startActivity(intent)
            }
        }



        setContentView(R.layout.activity_sign_in_company)

        Selection.setSelection(edtYourCompany.getText(), edtYourCompany.getText().length);
           val companyUrl =LoginPrefs.getCompanyUrl(this@SignInCompany)
        if(companyUrl !=null){
            edtYourCompany.setText(companyUrl.toString())
            edtYourCompany.setSelection(edtYourCompany.getText().length)
            btnNextSignInCompany.setBackgroundResource(R.drawable.button_change_color)
            btnNextSignInCompany.setTextColor(resources.getColor(R.color.white))
        }
       // edtYourCompany.setText("unicef.printercloud.com")
       // edtYourCompany.setSelection(edtYourCompany.getText().length)

        edtYourCompany.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.toString().startsWith("https://")) {
                    //  Selection.setSelection(edtYourCompany.getText(), edtYourCompany.getText().length);
                }


            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })


        setAlpha()

        btnNextSignInCompany.setOnClickListener {
            HideKeyboard.hideKeyboard(this@SignInCompany)
            if (CheckInternetConnection.isNetworkConnected(this@SignInCompany)) {
                if (LoginPrefs.getOCTAToken(this@SignInCompany) == null) {
                    ProgressDialog.showLoadingDialog(this@SignInCompany, "Loading")
                    try {
                        checkValidation()
                    }catch (e: java.lang.Exception) {
                        DataDogLogger.getLogger().i(
                            "Devnco_Android signIn excetion: " +e.message)
                        ProgressDialog.cancelLoading()
                        toast("Please check URL")
                    }
                   if(edtYourCompany.text.toString().contains("google")) {
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
                   var url:String= edtYourCompany.text.toString().trim()
                    val stringurl=url.substring(0, url.length)
                    LoginPrefs.saveCompanyUrl(this@SignInCompany, stringurl.toString())
                  //  val siteId: String = MainActivity.findSiteId(stringurl)
                   // LoginPrefs.saveSiteId(this@SignInCompany, siteId.toString())

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
             //   edtYourCompany.alpha = 0.75F
                btnNextSignInCompany.setBackgroundResource(R.drawable.button_change_color)
                btnNextSignInCompany.setTextColor(resources.getColor(R.color.white))
            }
            if (text?.length == 0) {
             //   edtYourCompany.alpha = 0.42F
                btnNextSignInCompany.setBackgroundResource(R.drawable.button_radius)
                btnNextSignInCompany.setTextColor(resources.getColor(R.color.silver))
            }
        }
    }

    private fun getIdpInfo(url: String) {
        logger.info("Devnco_Android logging url" + url)
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
                    getTenantBaseUrl()
                    getSiteId()
                    Log.i("printer", "response of api==>" + response.isSuccessful)
                    logger.info("Devnco_Android printer" + "response of api==>" + response.isSuccessful)
                    if (edtYourCompany.text.toString().contains("ldap")) {
                        bundle.putString("buttonName", "LDAP")
                    }

                    val list: List<IdpResponse>? = response.body()?.toList()
                    var size = 0
                    if (list != null) {
                        size = list.size
                    }
                    list?.forEach { idp ->
                        run {
                            bundle.putString("desktopLoginUrl", idp.desktopLoginUrl)
                            bundle.putString("buttonName", idp.name)
                            bundle.putString("clientId", idp.client_id)
                            bundle.putString("token_uri", idp.tokenUri)

                            if (idp.name.toString().toLowerCase().contains("google")) {
                                GoogleAPI.getGoogleData(this@SignInCompany)
                            }

                            SignInCompanyPrefs.saveIdpUrl(
                                this@SignInCompany,
                                idp.tokenUri.toString()
                            )
                            LoginPrefs.savegoogleTokenUrl(
                                this@SignInCompany,
                                idp.tokenUri.toString()
                            )
                            SignInCompanyPrefs.saveIdpName(
                                this@SignInCompany,
                                idp.idp_type.toString()
                            )
                            SignInCompanyPrefs.saveIdpType(
                                this@SignInCompany,
                                idp.authType.toString()
                            )
                        }
                    }


                    if (size == 0) {
                        if (!url.contains("ldap")) {
                            // toast("please check url")
                            val intent = Intent(this@SignInCompany, SignInActivity::class.java)
                            bundle.putString("buttonName", "LDAP")
                            intent.putExtras(bundle)
                            startActivity(intent)
                        }
                        if (url.contains("ldap")) {
                            val intent = Intent(this@SignInCompany, SignInActivity::class.java)
                            intent.putExtras(bundle)
                            startActivity(intent)
                        }
                    } else {
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
                logger.info("Devnco_Android printer" + "Error response of api==>" + t.message)
            }
        })
    }

   

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun getTenantBaseUrl() {
        val companyUrl = LoginPrefs.getCompanyUrl(this@SignInCompany)

        val BASE_URL = "https://"+companyUrl+"/api/discovery/gateway-tenant/"

        val apiService = RetrofitClient(this@SignInCompany)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)
        val call = apiService.getTenantBaseUrlResponse()

        call.enqueue(object : Callback<LinkedTreeMap<String, String>> {
            override fun onResponse(
                call: Call<LinkedTreeMap<String, String>>,
                response: Response<LinkedTreeMap<String, String>>
            ) {

                ProgressDialog.cancelLoading()
                Log.d("TenantBaseUrl", "response of api==>" + response.body())
                logger.info("Devnco_Android TenantBaseUrl" + "response of api==>" + response.isSuccessful)
                val map: LinkedTreeMap<String, String>? = response.body()
                if (map != null) {
                    Log.d("TenantBaseUrl:", map.getValue("tenantBaseUrl"))
                    val tenantBaseUrl = map.getValue("tenantBaseUrl")
                    val findTenantBaseUrl: String = MainActivity.findTenantBaseUrl(tenantBaseUrl)
                    LoginPrefs.saveTenantUrl(this@SignInCompany, findTenantBaseUrl.toString())
                }
                ProgressDialog.cancelLoading()

            }

            override fun onFailure(call: Call<LinkedTreeMap<String, String>>, t: Throwable) {
                ProgressDialog.cancelLoading()
                toast("Idp response ${t.message}")
                Log.i("printer", "Error response of api==>" + t.message)
                logger.info("Devnco_Android printer" + "Error response of api==>" + t.message)
            }
        })
    }



    private fun getSiteId() {
        val companyUrl = LoginPrefs.getCompanyUrl(this@SignInCompany)

        val BASE_URL = "https://"+companyUrl+"/api/discovery/site-id/"

        val apiService = RetrofitClient(this@SignInCompany)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)
        val call = apiService.getSiteIdResponse()

        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {

                ProgressDialog.cancelLoading()
                Log.d("siteId", "response of api==>" + (response.body()))
                logger.info("Devnco_Android response for siteId ==>" + response.body())
                val jsonObject: JsonObject? = response.body()?.getAsJsonObject("meta")
                if (jsonObject != null) {
                    Log.d("siteId", "siteId ==>" + jsonObject.get("site-id").toString())
                    logger.info("Devnco_Android siteId ==>" + jsonObject.get("site-id").toString())
                    val siteId: String = jsonObject.get("site-id").toString().replace("\"", "")
                    LoginPrefs.saveSiteId(this@SignInCompany, siteId)
                }
                ProgressDialog.cancelLoading()

            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                ProgressDialog.cancelLoading()
                toast("Idp response ${t.message}")
                Log.i("printer", "Error response of api==>" + t.message)
                logger.info("Devnco_Android printer" + "Error response of api==>" + t.message)
            }
        })
    }


}

//https://docs.microsoft.com/en-us/azure/active-directory/develop/tutorial-v2-android