package com.example.customeprintservice.print

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.customeprintservice.R
import com.example.customeprintservice.jipp.FileUtils
import com.example.customeprintservice.jipp.PrintUtils
import com.example.customeprintservice.model.TokenResponse
import com.example.customeprintservice.prefs.LoginPrefs
import com.example.customeprintservice.rest.ApiService
import com.example.customeprintservice.rest.RetrofitClient
import com.example.customeprintservice.room.SelectedFile
import com.example.customeprintservice.signin.SignInCompany
import kotlinx.android.synthetic.main.activity_bottom_navigation.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.noHistory
import org.jetbrains.anko.toast
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

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_navigation)

        list.clear()
        when (intent.action) {
            Intent.ACTION_SEND_MULTIPLE -> {
                Log.i("printer", "in action")
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM).let {
                    it?.forEach {
                        val realPath = FileUtils.getPath(this@BottomNavigationActivity, it)
                        Log.i("printer", "realpath==>$realPath")
                        val selectedFile = SelectedFile()
                        selectedFile.filePath = realPath
                        selectedFile.fileName = File(realPath).name
                        selectedFile.fileSelectedDate =
                            SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date())
                        list.add(selectedFile)

                    }
                }
                Log.i("printer", "list of shared url=>$list")
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
                    intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri?
                if (imageUri != null) {
                    val realPath = FileUtils.getPath(this, imageUri)
                    val selectedFile = SelectedFile()
                    selectedFile.filePath = realPath
                    selectedFile.fileName = File(realPath).name
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

        val intent = intent.data
        if (intent != null) {
            Log.i("printer", "intent data--->${intent.encodedPath}")

            val decodeUrl: String = intent.encodedPath?.let { decode(it) }!!
            val url = Uri.parse(decodeUrl)

            val expires: String = url.getQueryParameter("expires").toString()
            val sessionId: String = url.getQueryParameter("sessionId").toString()
            val signature: String = url.getQueryParameter("signature").toString()

            val finalUrl = "https://${url.host + url.path}/"

            getToken(finalUrl, expires, sessionId, signature)
        }
        PrintUtils().setContextAndInitializeJMDNS(this@BottomNavigationActivity)

        val printersFragment = PrintersFragment()
        val servicePortalFragment = ServicePortalFragment()

        if (list.isNotEmpty()) {
            bundle.putSerializable("sharedFileList", list)
        }
        printReleaseFragment.arguments = bundle
        setCurrentFragment(printReleaseFragment)

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.printRelease -> {
                    setCurrentFragment(printReleaseFragment)
                }
                R.id.printer -> {
                    setCurrentFragment(printersFragment)
                }
                R.id.servicePortal -> {
                    setCurrentFragment(servicePortalFragment)
                }
            }
            true
        }
    }


    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer, fragment)
            commit()
        }


    private fun decode(encoded: String): String? {
        return String(Base64.decode(encoded.toByteArray(), Base64.URL_SAFE))
    }

    private fun getToken(
        finalUrl: String,
        expire: String,
        sessionId: String,
        signature: String
    ) {
        val apiService = RetrofitClient.getRetrofitInstance(finalUrl).create(ApiService::class.java)
        val call = apiService.getToken(expire, sessionId, signature)

        call.enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                Log.i("printer", "token url->" + call.request().url())
                if (response.isSuccessful) {
                    val token = response.body()?.token
                    LoginPrefs.saveOctaToken(this@BottomNavigationActivity, token.toString())
                    Log.i("printer", "tok==>$token")
                    toast("OCTA Login Successful")
                } else {
                    toast("Response is Not Successful")
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                Log.i("printer", "token url->" + call.request().url())
                Log.i("printer", "Token error response-->" + t.message)
                toast("Error-" + t.message)
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
