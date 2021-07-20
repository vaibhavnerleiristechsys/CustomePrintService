package com.printerlogic.printerlogic.print

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.printerlogic.printerlogic.R
import com.printerlogic.printerlogic.model.DecodedJWTResponse
import com.printerlogic.printerlogic.model.TokenResponse
import com.printerlogic.printerlogic.prefs.LoginPrefs
import com.printerlogic.printerlogic.rest.ApiService
import com.printerlogic.printerlogic.rest.RetrofitClient
import com.printerlogic.printerlogic.room.SelectedFile
import com.printerlogic.printerlogic.utils.DataDogLogger
import com.printerlogic.printerlogic.utils.JwtDecode
import com.printerlogic.printerlogic.utils.ProgressDialog
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class BottomNavigationActivity : AppCompatActivity() {

    private var list = ArrayList<SelectedFile>()
    private var bundle = Bundle()
    val printReleaseFragment = PrintReleaseFragment()
   // var logger = LoggerFactory.getLogger(BottomNavigationActivity::class.java)

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_navigation)
    }




     fun decode(encoded: String): String? {
        return String(Base64.decode(encoded.toByteArray(), Base64.DEFAULT))
    }

    fun getToken(
        finalUrl: String,
        expire: String,
        sessionId: String,
        signature: String,
        context:Context
    ) {
        val apiService =
            RetrofitClient(context).getRetrofitInstance(finalUrl).create(ApiService::class.java)
        val call = apiService.getToken(expire, sessionId, signature)

        call.enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                Log.i("printer", "token url->" + call.request().url())
                DataDogLogger.getLogger().i("Devnco_Android printer"+ "token url->" + call.request().url())

                if (response.isSuccessful) {
                    val token = response.body()?.token
                    LoginPrefs.saveOctaToken(context, token.toString())
                    Log.i("printer", "tok==>$token")
                    DataDogLogger.getLogger().i("Devnco_Android printer"+ "tok==>$token")
                    printReleaseFragment.arguments = bundle

                } else {

                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Log.i("printer", "token url->" + call.request().url())
                DataDogLogger.getLogger().i("Devnco_Android printer"+ "token url->" + call.request().url())
                Log.i("printer", "Token error response-->" + t.message)
                DataDogLogger.getLogger().i("Devnco_Android printer"+ "Token error response-->" + t.message)
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


    fun decodeJWT(context:Context): String {
        var userName: String? = null
        try {
            val mapper = jacksonObjectMapper()
            val decoded: DecodedJWTResponse = mapper.readValue<DecodedJWTResponse>(
                LoginPrefs.getOCTAToken(context)?.let { JwtDecode.decoded(it) }!!
            )
            userName = decoded.user.toString()
        } catch (ex: Exception) {

        }
        return userName.toString()
    }

    fun getTokenFromMainAcitivity(decodeUrl: String,context: Context){
        val url = Uri.parse(decodeUrl)

        val expires: String = url.getQueryParameter("expires").toString()
        val sessionId: String = url.getQueryParameter("sessionId").toString()
        val signature: String = url.getQueryParameter("signature").toString()

        val finalUrl = "https://${url.host + url.path}/"

        ProgressDialog.showLoadingDialog(context, "getting Token")
        getToken(finalUrl, expires, sessionId, signature,context)
    }

}


//eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiJkZjZiZjk5Zi0zNjExLTQzZTUtYTI2MS1hZWQyNjc4ZGRiN2QiLCJpZHAiOiJPa3RhIiwic2l0ZSI6ImRldm5jb29rdGEiLCJ1c2VyIjoicHJhbmF2LnBhdGlsQGRldm5jby5jbyIsInNlc3Npb24iOiJmM2E1MTE3ZC00OGI1LTQ2YzQtOWM3ZC0wN2FlZWYxOTAwZDYiLCJleHAiOjE2MzU0ODI3MTAsImlhdCI6MTYwMzk0NjcxMCwiaXNzIjoiY29tLnByaW50ZXJsb2dpYy5zZXJ2aWNlcy5hdXRobiIsImF1ZCI6ImNvbS5wcmludGVybG9naWMuY2xpZW50cy5kZXNrdG9wLmlkcCJ9.TTj-6qa5toLcZ4yk-MCcOxi0H3JQBtvCBCQ3PCN1YuEnAYrbWGZQMkwC-KCWNrDlxQngbK7xn-munOGQN-42mAjlU8LrTG1p4lsXJybKaBgstDgTVOZAXHvOQ_OufNn2QQOBybuIxkPCNR7D7Xbr9x6LWzs4TujX-AcjVTIvtA3bM_l-WC-ak8wtMQBeKG5WTLW42-hUr33IbGfq6NM6lTjibTaxMHl1SknWqcK0sHS4YD7ADnr0RzIyCkB7-7TQu9xhyypzZG2O_5MvRnONhZ6QWC22WuBnnBiNz6h6dYVjmLFblQs3afcGApjIkwnmaGfYYGepfMlXzbWIiQsoNQ