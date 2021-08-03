package com.printerlogic.printerlogic.signin

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.datadog.android.log.Logger
import com.printerlogic.printerlogic.MainActivity
import com.printerlogic.printerlogic.R
import com.printerlogic.printerlogic.prefs.LoginPrefs
import com.printerlogic.printerlogic.printjobstatus.model.ldapResponse.LdapSessionResponse
import com.printerlogic.printerlogic.printjobstatus.model.ldapResponse.LdapUserNameResponse
import com.printerlogic.printerlogic.rest.ApiService
import com.printerlogic.printerlogic.rest.RetrofitClient
import com.printerlogic.printerlogic.utils.ProgressDialog
import com.printerlogic.printerlogic.utils.ProgressDialog.Companion.showLoadingDialog
import kotlinx.android.synthetic.main.activity_sign_in.*
import okhttp3.ResponseBody
import org.jetbrains.anko.toast
import org.slf4j.LoggerFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInActivity : AppCompatActivity() {

    private var isShowPass = false
    private var bundle = Bundle()
    var LOG = LoggerFactory.getLogger(SignInActivity::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        try {



            val logger = Logger.Builder()
                .setNetworkInfoEnabled(true)
                .setServiceName("MobilePrint")
                .setLogcatLogsEnabled(true)
                .setDatadogLogsEnabled(true)
                .setLoggerName("name")
                .build();
            logger.i("Activity loaded")



            bundle = intent.extras!!
            btnSignInWithOkta.visibility= View.VISIBLE
            txtOr.visibility= View.VISIBLE
            val sharedPreferences: SharedPreferences = getSharedPreferences(
                "MySharedPref",
                Context.MODE_PRIVATE
            )
            val myEdit = sharedPreferences.edit()
            myEdit.putString("IsLdap", "");
            myEdit.putString("LdapUsername", "");
            myEdit.putString("LdapPassword", "");
            myEdit.commit()


            if (bundle.getString("buttonName") == "Okta") {
                btnSignInWithOkta.text ="    Sign In With Okta"
                btnSignInWithOkta.setTextColor(getResources().getColor(R.color.black))
                btnSignInWithOkta.setBackgroundResource(R.drawable.button_sign_in_okta)
                val drawable = baseContext.resources.getDrawable(R.drawable.okta2x)
                btnSignInWithOkta.setCompoundDrawablesWithIntrinsicBounds(
                    drawable,
                    null,
                    null,
                    null
                )
                edtUserName.visibility= View.GONE
                edtPassword.visibility= View.GONE
                btnSignIn.visibility= View.GONE
                txtOr.visibility= View.GONE
                imgShowPassword.visibility= View.GONE

            } else if (bundle.getString("buttonName") == "Azure AD") {
                btnSignInWithOkta.text = "    Sign In With Azure AD"
                btnSignInWithOkta.setTextColor(getResources().getColor(R.color.black))
                btnSignInWithOkta.setBackgroundResource(R.drawable.button_sign_in_azure)
                val drawable = baseContext.resources.getDrawable(R.drawable.azuread2x)
                btnSignInWithOkta.setCompoundDrawablesWithIntrinsicBounds(
                    drawable,
                    null,
                    null,
                    null
                )
                edtUserName.visibility= View.GONE
                edtPassword.visibility= View.GONE
                btnSignIn.visibility= View.GONE
                txtOr.visibility= View.GONE
                imgShowPassword.visibility= View.GONE
            }else if(bundle.getString("buttonName").toString().contains("Google")){
                btnSignInWithOkta.text = "    Sign In With Google"
                btnSignInWithOkta.setTextColor(getResources().getColor(R.color.black))
                btnSignInWithOkta.setBackgroundResource(R.drawable.button_sign_in_google)
                val drawable = baseContext.resources.getDrawable(R.drawable.google2x)
                btnSignInWithOkta.setCompoundDrawablesWithIntrinsicBounds(
                    drawable,
                    null,
                    null,
                    null
                )
                edtUserName.visibility= View.GONE
                edtPassword.visibility= View.GONE
                btnSignIn.visibility= View.GONE
                txtOr.visibility= View.GONE
                imgShowPassword.visibility= View.GONE
            }else  if (bundle.getString("buttonName") == "LDAP") {
                btnSignInWithOkta.visibility= View.GONE
                txtOr.visibility= View.GONE
                edtUserName.visibility= View.VISIBLE
                edtPassword.visibility= View.VISIBLE
                btnSignIn.visibility= View.VISIBLE
                imgShowPassword.visibility= View.VISIBLE
            }
        } catch (e: Exception) {
            Log.i("printer", "exception=>$e")
            LOG.info("printer", "exception=>$e")
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

            val username = edtUserName.text.toString()
            val password = edtPassword.text.toString()

            Log.d("username", edtUserName.text.toString())
            LOG.info("username:" + edtUserName.text.toString())
            Log.d("password", edtPassword.text.toString())
            LOG.info("password:" + edtPassword.text.toString())
            getSessionIdForLdapLogin(this@SignInActivity, username, password)
            //checkLdapLogin(this@SignInActivity, username, password)

        }

        backarrow.setOnClickListener {
            val intent = Intent(this@SignInActivity, SignInCompany::class.java)
            startActivity(intent)
        }

        val desktopUrl: String? = bundle.getString("desktopLoginUrl")
        Log.i("printer", "desktopUrl--->${desktopUrl}")
        LOG.info("printer", "desktopUrl--->${desktopUrl}")
        btnSignInWithOkta.setOnClickListener {
            if (bundle.getString("buttonName") == "Okta") {
                //searchWeb(desktopUrl)
                searchWebForBrowser(desktopUrl)
            }
            else if(bundle.getString("buttonName")=="Google"){

               // val desktopUrl = "https://gw.app.printercloud.com/googleid/authn/idp/Google/oidc/desktop/login"
               // val desktopUrl
               // searchWeb(desktopUrl)
                searchWebForBrowser(desktopUrl)
            }
            else{
               // searchWeb(desktopUrl)
                searchWebForBrowser(desktopUrl)
            }
        }
    }

    private fun searchWebForBrowser(query: String?){
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(query)
        startActivity(i)
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

    fun checkLdapLogin(context: Context, username: String, password: String) {
        showLoadingDialog(context, "Please Wait")
        val companyUrl = LoginPrefs.getCompanyUrl(context)
        val BASE_URL = "https://"+companyUrl+"/api/verify-login/"
        val apiService = RetrofitClient(context)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)

        val call = apiService.checkLdapLogin(
            "portal",
            username,
            password
        )

        call.enqueue(object : Callback<ResponseBody> {

            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                ProgressDialog.cancelLoading()
                if (response.code() == 204) {
                    Log.i("LDAP printers Response", response.toString())
                    LOG.info("LDAP printers Response" + response.toString())
                    //   toast("Login Successfully")
                    val sharedPreferences: SharedPreferences =
                        getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
                    val myEdit = sharedPreferences.edit()
                    myEdit.putString("IsLdap", "LDAP");
                    myEdit.putString("LdapUsername", username);
                    myEdit.putString("LdapPassword", password);
                    myEdit.commit()
                    val intent = Intent(this@SignInActivity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    toast("Login Not Successfully Please Try Again")
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Log.i("printer", "Error html response==>${t.message.toString()}")
                LOG.info("printer", "Error html response==>${t.message.toString()}")
                toast("Login Not Successfully Please Try Again")
            }
        })
    }

    fun getSessionIdForLdapLogin(context: Context, username: String, password: String) {
        showLoadingDialog(context, "Please Wait")
        val companyUrl = LoginPrefs.getCompanyUrl(context)
        val usernameAndPassword:String=username+":"+password
        val base64encodedtoken:String= MainActivity.encodeString(usernameAndPassword);
        Log.i("encoded String", "encoded Ldap String::" + base64encodedtoken.toString())
        val BASE_URL = "https://"+companyUrl+"/api/portal-session-id/"
        val apiService = RetrofitClient(context)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)

        val call = apiService.getSessionForLdapLogin(
            "Basic " + base64encodedtoken
        )

        call.enqueue(object : Callback<LdapSessionResponse> {

            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(
                call: Call<LdapSessionResponse>,
                response: Response<LdapSessionResponse>
            ) {
                ProgressDialog.cancelLoading()
                if (response.code() == 200) {
                    Log.i("LDAP  id Response", response.body()?.data?.id.toString())
                    Log.i("LDAP type Response", response.body()?.data?.type.toString())
                    val sessionId: String = response.body()?.data?.id.toString()
                    LoginPrefs.saveSessionIdForLdap(context, sessionId)

                    //   toast("Login Successfully")
                    val sharedPreferences: SharedPreferences =
                        getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
                    val myEdit = sharedPreferences.edit()
                    myEdit.putString("IsLdap", "LDAP");
                    myEdit.putString("LdapUsername", username);
                    myEdit.putString("LdapPassword", password);
                    myEdit.commit()

                    checkValidSessionForLdapLogin(context, sessionId)
                    // val intent = Intent(this@SignInActivity, MainActivity::class.java)
                    //startActivity(intent)
                } else {
                    toast("Login Not Successfully Please Try Again")
                }

            }

            override fun onFailure(call: Call<LdapSessionResponse>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Log.i("printer", "Error html response==>${t.message.toString()}")
                LOG.info("printer", "Error html response==>${t.message.toString()}")
                toast("Login Not Successfully Please Try Again")
            }
        })
    }

    fun checkValidSessionForLdapLogin(context: Context, sessionId: String) {
        showLoadingDialog(context, "Please Wait")
        val companyUrl = LoginPrefs.getCompanyUrl(context)

        val BASE_URL = "https://"+companyUrl+"/api/logged-in-portal/"
        val apiService = RetrofitClient(context)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)

        val call = apiService.getUsernameForLdapLogin(
            "PHPSESSID=" + sessionId
        )

        call.enqueue(object : Callback<LdapUserNameResponse> {

            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(
                call: Call<LdapUserNameResponse>,
                response: Response<LdapUserNameResponse>
            ) {
                ProgressDialog.cancelLoading()
                if (response.code() == 200) {
                    Log.i(
                        "LDAP user Response",
                        "LDAP userName:" + (response.body()?.meta?.userName.toString())
                    )
                    Log.i(
                        "LDAP user Response",
                        "LDAP isPortal:" + (response.body()?.meta?.isPortalUser.toString())
                    )
                    //   toast("Login Successfully")
                    val userName: String = response.body()?.meta?.userName.toString()
                    if (userName != "") {
                        val intent = Intent(this@SignInActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        toast("Login Not Successfully Please Try Again")
                    }
                } else {
                    toast("Login Not Successfully Please Try Again")
                }

            }

            override fun onFailure(call: Call<LdapUserNameResponse>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Log.i("printer", "Error html response==>${t.message.toString()}")
                LOG.info("printer", "Error html response==>${t.message.toString()}")
                toast("Login Not Successfully Please Try Again")
            }
        })
    }
}