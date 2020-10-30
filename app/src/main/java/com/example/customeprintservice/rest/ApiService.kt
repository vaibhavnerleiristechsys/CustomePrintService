package com.example.customeprintservice.rest

import com.example.customeprintservice.model.IdpResponse
import com.example.customeprintservice.model.TokenResponse
import com.example.customeprintservice.printjobstatus.model.PrintJobStatusResponse
import retrofit2.Call
import retrofit2.http.*
import retrofit2.http.Headers as Headers1


interface ApiService {

    @Headers1("Content-Type: application/json")
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
    ): Call<Any>?


    @GET(".")
    fun printJobstatus(
        @Header("Authorization") Authorization: String?,
        @Header("X-User-Name")userName: String?,
        @Header("X-Idp_Type")idpType:String?,
        @Header("X-Idp-Name")idpName:String?
    ):Call<PrintJobStatusResponse>?

}