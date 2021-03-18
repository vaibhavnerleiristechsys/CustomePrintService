package com.example.customeprintservice.print

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.customeprintservice.R
import com.example.customeprintservice.jipp.FileUtils
import com.example.customeprintservice.jipp.PrintUtils
import com.example.customeprintservice.model.DecodedJWTResponse
import com.example.customeprintservice.model.TokenResponse
import com.example.customeprintservice.prefs.LoginPrefs
import com.example.customeprintservice.rest.ApiService
import com.example.customeprintservice.rest.RetrofitClient
import com.example.customeprintservice.room.SelectedFile
import com.example.customeprintservice.signin.SignInActivity
import com.example.customeprintservice.signin.SignInCompany
import com.example.customeprintservice.utils.JwtDecode
import com.example.customeprintservice.utils.ProgressDialog
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.android.synthetic.main.activity_bottom_navigation.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.noHistory
import org.jetbrains.anko.toast
import org.slf4j.LoggerFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class BottomNavigationActivity : AppCompatActivity() {

    private var list = ArrayList<SelectedFile>()
    private var bundle = Bundle()
    val printReleaseFragment = PrintReleaseFragment()
    var logger = LoggerFactory.getLogger(BottomNavigationActivity::class.java)

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_navigation)
     //   val printersFragment1 = PrintersFragment()
      //  printersFragment1.getPrinterList(applicationContext,decodeJWT(applicationContext))

      /*  list.clear()
        when (intent.action) {
            Intent.ACTION_SEND_MULTIPLE -> {
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM).let {
                    it?.forEach {
                        val realPath = FileUtils.getPath(this@BottomNavigationActivity, it)
                        Log.i("printer", "realpath==>$realPath")
                        logger.info("Devnco_Android printer"+ "realpath==>$realPath")
                        val selectedFile = SelectedFile()
                        selectedFile.filePath = realPath
                        selectedFile.fileName = File(realPath).name
                        selectedFile.isFromApi = false
                        selectedFile.fileSelectedDate =
                            SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date())
                        list.add(selectedFile)

                    }
                }
                Log.i("printer", "list of shared url=>$list")
                logger.info("Devnco_Android printer"+ "list of shared url=>$list")
                if (LoginPrefs.getOCTAToken(this@BottomNavigationActivity) == null) {

                    startActivity(intentFor<SignInCompany>().noHistory())
                    this@BottomNavigationActivity.overridePendingTransition(
                        R.anim.entry,
                        R.anim.end
                    )
                    finishAffinity()
                }
            }

            Intent.ACTION_SEND -> {

                val imageUri =
                    intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM)

                if (imageUri != null) {
                    val realPath = FileUtils.getPath(this, imageUri as Uri?)
                    val selectedFile = SelectedFile()
                    selectedFile.filePath = realPath
                    selectedFile.fileName = File(realPath).name
                    selectedFile.isFromApi = false
                    selectedFile.fileSelectedDate =
                        SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date())
                    list.add(selectedFile)
                } else {
                    Toast.makeText(this, "Error Occurred, URI is invalid", Toast.LENGTH_LONG)
                        .show()
                }
                if (LoginPrefs.getOCTAToken(this@BottomNavigationActivity) == null) {

                    startActivity(intentFor<SignInCompany>().noHistory())
                    this@BottomNavigationActivity.overridePendingTransition(
                        R.anim.entry,
                        R.anim.end
                    )
                    finishAffinity()
                }
            }
        }
*/
  /*      val intent = intent.data


        if (intent != null) {
            Log.i("printer", "intent data--->${intent.encodedPath}")
            logger.info("Devnco_Android printer"+ "intent data--->${intent.encodedPath}")

            val decodeUrl: String =
                intent.encodedPath
                    ?.replaceFirst("/", "").toString().let { decode(it).toString() }

            val url = Uri.parse(decodeUrl)

            val expires: String = url.getQueryParameter("expires").toString()
            val sessionId: String = url.getQueryParameter("sessionId").toString()
            val signature: String = url.getQueryParameter("signature").toString()

            val finalUrl = "https://${url.host + url.path}/"

            ProgressDialog.showLoadingDialog(this@BottomNavigationActivity, "getting Token")
            getToken(finalUrl, expires, sessionId, signature,this@BottomNavigationActivity)

        } else {


            printReleaseFragment.arguments = bundle
        }


        if (list.isNotEmpty()) {
            bundle.putSerializable("sharedFileList", list)
        }
*/
/*
        Handler().postDelayed({
            //doSomethingHere()
            PrintUtils().setContextAndInitializeJMDNS(this@BottomNavigationActivity)
        }, 5000)
*/
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
                logger.info("Devnco_Android printer"+ "token url->" + call.request().url())

                if (response.isSuccessful) {
                    val token = response.body()?.token
                    LoginPrefs.saveOctaToken(context, token.toString())
                    Log.i("printer", "tok==>$token")
                    logger.info("Devnco_Android printer"+ "tok==>$token")
               //     ProgressDialog.cancelLoading()
                    printReleaseFragment.arguments = bundle

                  //  val printersFragment1 = PrintersFragment()
                  //  printersFragment1.getPrinterList(context,decodeJWT(context))
                } else {
                   // toast("Response is Not Successful")
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Log.i("printer", "token url->" + call.request().url())
                logger.info("Devnco_Android printer"+ "token url->" + call.request().url())
                Log.i("printer", "Token error response-->" + t.message)
                logger.info("Devnco_Android printer"+ "Token error response-->" + t.message)
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
            //context.toast("Failed to Decode Jwt Token")
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