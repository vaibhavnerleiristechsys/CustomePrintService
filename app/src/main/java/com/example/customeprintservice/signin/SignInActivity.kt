package com.example.customeprintservice.signin

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.customeprintservice.MainActivity
import com.example.customeprintservice.R
import com.example.customeprintservice.prefs.LoginPrefs
import com.example.customeprintservice.print.BottomNavigationActivity
import com.example.customeprintservice.rest.ApiService
import com.example.customeprintservice.rest.RetrofitClient
import com.example.customeprintservice.utils.GoogleAPI
import com.example.customeprintservice.utils.ProgressDialog
import kotlinx.android.synthetic.main.activity_sign_in.*
import okhttp3.ResponseBody
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInActivity : AppCompatActivity() {

    private var isShowPass = false
    private var bundle = Bundle()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        try {
            bundle = intent.extras!!
            btnSignInWithOkta.visibility= View.VISIBLE
            txtOr.visibility= View.VISIBLE
            val sharedPreferences: SharedPreferences =
                getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
            val myEdit = sharedPreferences.edit()
            myEdit.putString("IsLdap","");
            myEdit.putString("LdapUsername","");
            myEdit.putString("LdapPassword","");
            myEdit.commit()


            if (bundle.getString("buttonName") == "Okta") {
                btnSignInWithOkta.text = bundle.getString("buttonName")
                btnSignInWithOkta.setBackgroundResource(R.drawable.button_sign_in_okta)
                val drawable = baseContext.resources.getDrawable(R.mipmap.icon_okta)
                btnSignInWithOkta.setCompoundDrawablesWithIntrinsicBounds(
                    drawable, null, null, null)
                btnSignInWithOkta.gravity = Gravity.CENTER
                edtUserName.visibility= View.GONE
                edtPassword.visibility= View.GONE
                btnSignIn.visibility= View.GONE
                txtOr.visibility= View.GONE
                imgShowPassword.visibility= View.GONE

            } else if (bundle.getString("buttonName") == "Azure AD") {
                btnSignInWithOkta.text = bundle.getString("buttonName")
                btnSignInWithOkta.setBackgroundResource(R.drawable.button_sign_in_azure)
                val drawable = baseContext.resources.getDrawable(R.mipmap.icon_azure)
                btnSignInWithOkta.setCompoundDrawablesWithIntrinsicBounds(
                    drawable, null, null, null)
                btnSignInWithOkta.gravity = Gravity.CENTER
                edtUserName.visibility= View.GONE
                edtPassword.visibility= View.GONE
                btnSignIn.visibility= View.GONE
                txtOr.visibility= View.GONE
                imgShowPassword.visibility= View.GONE
            }else if(bundle.getString("buttonName")=="Google"){
                btnSignInWithOkta.text = bundle.getString("buttonName")
                btnSignInWithOkta.setBackgroundResource(R.drawable.button_sign_in_google)
                val drawable = baseContext.resources.getDrawable(R.mipmap.icon_google)
                btnSignInWithOkta.setCompoundDrawablesWithIntrinsicBounds(
                    drawable, null, null, null)
                btnSignInWithOkta.gravity = Gravity.CENTER
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
          //  HideKeyboard.hideKeyboard(this@SignInActivity)
           // val intent = Intent(this@SignInActivity, BottomNavigationActivity::class.java)
           // startActivity(intent)
            val username = edtUserName.text.toString()
            val password = edtPassword.text.toString()

           Log.d("username",edtUserName.text.toString())
                   Log.d("password",edtPassword.text.toString())
          //  getPrinterListForCheckLdapLogin(this@SignInActivity,username,password)
            checkLdapLogin(this@SignInActivity,username,password)

        }

        backarrow.setOnClickListener {
            val intent = Intent(this@SignInActivity , SignInCompany::class.java)
            startActivity(intent)
        }

        val desktopUrl: String? = bundle.getString("desktopLoginUrl")
        Log.i("printer", "desktopUrl--->${desktopUrl}")
        btnSignInWithOkta.setOnClickListener {
            if (bundle.getString("buttonName") == "Okta") {
                searchWeb(desktopUrl)
            }
            else if(bundle.getString("buttonName")=="Google"){

                //val intent = Intent(this@SignInActivity, GoogleLoginActivity::class.java)
                //intent.putExtras(bundle)
                //startActivity(intent)

                val desktopUrl = "https://gw.app.printercloud.com/googleid/authn/idp/Google/oidc/desktop/login"
                searchWeb(desktopUrl)
            }
            else{
                searchWeb(desktopUrl)
            }
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

    fun getPrinterListForCheckLdapLogin(
        context: Context,username:String,password:String
    ) {
        val BASE_URL =
            "https://gw.app.printercloud.com/devncoldap/prs/v1/printers/1/"

        val apiService = RetrofitClient(context)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)

        val call = apiService.getPrinterForLdap(
            "devncoldap",
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
                if (response.isSuccessful) {
                    Log.i("LDAP printers Response",response.toString())
                    toast("Login Successfully")
                    val sharedPreferences: SharedPreferences =
                        getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
                    val myEdit = sharedPreferences.edit()
                    myEdit.putString("IsLdap","LDAP");
                    myEdit.putString("LdapUsername",username);
                    myEdit.putString("LdapPassword",password);
                    myEdit.commit()
                    val intent = Intent(this@SignInActivity,MainActivity::class.java)
                     startActivity(intent)
                }
                if (response.code()==401){
                    toast("Login Not Successfully Please Try Again")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Log.i("printer", "Error html response==>${t.message.toString()}")
                toast("Login Not Successfully Please Try Again")
            }
        })
    }


    fun checkLdapLogin(
        context: Context,username:String,password:String
    ) {

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
                if (response.code()==204) {
                    Log.i("LDAP printers Response",response.toString())
                    toast("Login Successfully")
                    val sharedPreferences: SharedPreferences =
                        getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
                    val myEdit = sharedPreferences.edit()
                    myEdit.putString("IsLdap","LDAP");
                    myEdit.putString("LdapUsername",username);
                    myEdit.putString("LdapPassword",password);
                    myEdit.commit()
                    val intent = Intent(this@SignInActivity,MainActivity::class.java)
                    startActivity(intent)
                }
                else{
                    toast("Login Not Successfully Please Try Again")
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Log.i("printer", "Error html response==>${t.message.toString()}")
                toast("Login Not Successfully Please Try Again")
            }
        })
    }
}