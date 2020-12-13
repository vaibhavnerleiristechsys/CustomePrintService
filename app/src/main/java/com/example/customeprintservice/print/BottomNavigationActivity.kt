package com.example.customeprintservice.print

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.example.customeprintservice.utils.ProgressDialog
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
                        selectedFile.isFromApi = false
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
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                val imageUri =
                    intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM)
//
//                Log.i("printer", "image uri=>${imageUri}")
//                val fileImage = File(imageUri.toString())
//                Log.i("printer", "file image =>${fileImage.name + " path =>${fileImage.path}"}")
//
//                var bmpUri: Uri? = null
//                try {
//                    val file = File(
//                        Environment.getExternalStoragePublicDirectory(
//                            Environment.DIRECTORY_DOWNLOADS
//                        ), "share_image_$imageUri"
//                    )
//                    file.parentFile?.mkdirs()
//                    val inputStream = contentResolver.openInputStream(Uri.parse(imageUri.toString()))
//                    Log.i("printer", "Input stream ===>${inputStream?.readBytes()}")
//
//                    val fileUtils =  org.apache.commons.io.FileUtils.copyToFile(inputStream,file)
//
//                } catch (e: IOException) {
//                    Log.i("printer", "exception ===>${e.message}")
//
//                }

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

        val intent = intent.data
        if (intent != null) {
            Log.i("printer", "intent data--->${intent.encodedPath}")

            val decodeUrl: String =
                intent.encodedPath
                    ?.replaceFirst("/", "").toString().let { decode(it).toString() }

            val url = Uri.parse(decodeUrl)

            val expires: String = url.getQueryParameter("expires").toString()
            val sessionId: String = url.getQueryParameter("sessionId").toString()
            val signature: String = url.getQueryParameter("signature").toString()

            val finalUrl = "https://${url.host + url.path}/"

            ProgressDialog.showLoadingDialog(this@BottomNavigationActivity, "getting Token")
            getToken(finalUrl, expires, sessionId, signature)

        } else {
            printReleaseFragment.arguments = bundle
            setCurrentFragment(printReleaseFragment)
        }
        PrintUtils().setContextAndInitializeJMDNS(this@BottomNavigationActivity)

        val printersFragment = PrintersFragment()
        val servicePortalFragment = ServicePortalFragment()

        if (list.isNotEmpty()) {
            bundle.putSerializable("sharedFileList", list)
        }


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
        return String(Base64.decode(encoded.toByteArray(), Base64.DEFAULT))
    }

    fun getToken(
        finalUrl: String,
        expire: String,
        sessionId: String,
        signature: String
    ) {
        val apiService =
            RetrofitClient(this).getRetrofitInstance(finalUrl).create(ApiService::class.java)
        val call = apiService.getToken(expire, sessionId, signature)

        call.enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                Log.i("printer", "token url->" + call.request().url())

                if (response.isSuccessful) {
                    val token = response.body()?.token
                    LoginPrefs.saveOctaToken(this@BottomNavigationActivity, token.toString())
                    Log.i("printer", "tok==>$token")
                    ProgressDialog.cancelLoading()
                    printReleaseFragment.arguments = bundle
                    setCurrentFragment(printReleaseFragment)
                    toast("Login Successfully")
                } else {
                    toast("Response is Not Successful")
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                ProgressDialog.cancelLoading()
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


//eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiJkZjZiZjk5Zi0zNjExLTQzZTUtYTI2MS1hZWQyNjc4ZGRiN2QiLCJpZHAiOiJPa3RhIiwic2l0ZSI6ImRldm5jb29rdGEiLCJ1c2VyIjoicHJhbmF2LnBhdGlsQGRldm5jby5jbyIsInNlc3Npb24iOiJmM2E1MTE3ZC00OGI1LTQ2YzQtOWM3ZC0wN2FlZWYxOTAwZDYiLCJleHAiOjE2MzU0ODI3MTAsImlhdCI6MTYwMzk0NjcxMCwiaXNzIjoiY29tLnByaW50ZXJsb2dpYy5zZXJ2aWNlcy5hdXRobiIsImF1ZCI6ImNvbS5wcmludGVybG9naWMuY2xpZW50cy5kZXNrdG9wLmlkcCJ9.TTj-6qa5toLcZ4yk-MCcOxi0H3JQBtvCBCQ3PCN1YuEnAYrbWGZQMkwC-KCWNrDlxQngbK7xn-munOGQN-42mAjlU8LrTG1p4lsXJybKaBgstDgTVOZAXHvOQ_OufNn2QQOBybuIxkPCNR7D7Xbr9x6LWzs4TujX-AcjVTIvtA3bM_l-WC-ak8wtMQBeKG5WTLW42-hUr33IbGfq6NM6lTjibTaxMHl1SknWqcK0sHS4YD7ADnr0RzIyCkB7-7TQu9xhyypzZG2O_5MvRnONhZ6QWC22WuBnnBiNz6h6dYVjmLFblQs3afcGApjIkwnmaGfYYGepfMlXzbWIiQsoNQ