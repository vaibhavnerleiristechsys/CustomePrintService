package com.example.customeprintservice.rest

import com.example.customeprintservice.model.IdpResponse
import com.example.customeprintservice.model.TokenResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService {

    @Headers("Content-Type: application/json")

    @GET("idp")
    fun getIdpResponse(): Call<List<IdpResponse>>

    @GET("devncookta/authn/idp/Okta/desktop/validation/pranav.patil@devnco.co/")
    fun getToken(@Query("expires")expires:String,
                 @Query("sessionId")sessionId:String,
    @Query("signature")signature:String):Call<TokenResponse>

}