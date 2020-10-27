package com.example.customeprintservice.rest

import android.content.Context
import android.util.Log
import com.example.customeprintservice.prefs.LoginPrefs
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient(context: Context) {

    var client = OkHttpClient.Builder()
        .connectTimeout(50, TimeUnit.SECONDS)
        .writeTimeout(50, TimeUnit.SECONDS)
        .readTimeout(50, TimeUnit.SECONDS)
        .addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val regRequest = chain.request().newBuilder()
//                    .addHeader("Authorization", "${LoginPrefs.getOCTAToken(context)}")
                    .build()
                val response = chain.proceed(regRequest)
//                if(response.code() ==403){
//                    Log.i("printer", "Token is Expired")
//                }
                return response
            }
        })
        .build()

    fun getRetrofitInstance(baseUrl: String): Retrofit {
        val retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        return retrofit
    }
}
