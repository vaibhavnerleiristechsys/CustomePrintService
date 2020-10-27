package com.example.customeprintservice.rest

import com.example.customeprintservice.model.IdpResponse
import com.example.customeprintservice.model.TokenResponse
import retrofit2.Call
import retrofit2.http.*


interface ApiService {

    @Headers("Content-Type: application/json")

    @GET(".")
    fun getIdpResponse(): Call<List<IdpResponse>>

    @GET(".")
    fun getToken(
        @Query("expires") expires: String,
        @Query("sessionId") sessionId: String,
        @Query("signature") signature: String
    ): Call<TokenResponse>


    @FormUrlEncoded
    @POST(".")
    fun validateToken(
        @Field("token") token: String?,
        @Field("userName") userName: String?
    ): Call<String>?

}