package com.example.customeprintservice.rest

import com.example.customeprintservice.model.IdpResponse
import com.example.customeprintservice.model.TokenResponse
import com.example.customeprintservice.printjobstatus.model.canceljob.CancelJobRequest
import com.example.customeprintservice.printjobstatus.model.canceljob.CancelJobResponse
import com.example.customeprintservice.printjobstatus.model.getjobstatuses.GetJobStatusesResponse
import com.example.customeprintservice.printjobstatus.model.printerdetails.PrinterDetailsResponse
import com.example.customeprintservice.printjobstatus.model.printerlist.Printer
import com.example.customeprintservice.printjobstatus.model.printerlist.PrinterListDesc
import com.example.customeprintservice.printjobstatus.model.releasejob.ReleaseJobRequest
import com.example.customeprintservice.printjobstatus.model.releasejob.ReleaseJobResponse
import com.google.gson.internal.LinkedTreeMap
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import retrofit2.http.Headers as Headers1


interface ApiService {

    @Headers1("Content-Type: application/json")
    @GET(".")
    fun getIdpResponse(): Call<List<IdpResponse>>

    @Headers1("Content-Type: application/json")
    @GET(".")
    fun getTenantBaseUrlResponse(): Call<LinkedTreeMap<String, String>>


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
        @Header("Authorization") Authorization: String,
        @Header("X-User-Name") userName: String,
        @Header("X-Idp_Type") idpType: String,
        @Header("X-Idp-Name") idpName: String
    ): Call<ResponseBody>?

    @FormUrlEncoded
    @POST(".")
    fun getPrinterList(
        @Field("idp_name") idpName: String,
        @Field("is_logged_in") isLoggedIn: Boolean,
        @Field("username") userName: String,
        @Field("auth_type") authType: String,
        @Field("token") token: String,
        @Field("is_mobile") isMobile: Boolean
    ): Call<PrinterListDesc>

    @FormUrlEncoded
    @POST(".")
    fun getPrinterNodes(
        @Header("Cookie") cookie: String,
        @Field("search_root") search_root: String,
        @Field("search") search: String,
        @Field("mobile") mobile: String,
        @Field("adminmode") adminmode: String,
        @Field("type") type: String,
        @Field("release_station_configuration_id") releaseStationConfId: String
    ): Call<ResponseBody>


    @FormUrlEncoded
    @POST(".")
    fun getPrinterList(
        @Header("X-Site-Id") XSiteId: String,
        @Header("Authorization") Authorization: String,
        @Header("X-User-Name") XUserName: String,
        @Header("X-IdP-Type") XIdPType: String,
        @Header("X-IdP-Name") XIdPName: String,
        @Field("checkin") checkin: String,
        @Field("configuration") configuration: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST(".")
    fun getPrinterListForGoogle(
        @Header("X-Site-Id") XSiteId: String,
        @Header("Authorization") Authorization: String,
        @Header("X-User-Name") XUserName: String,
        @Header("X-IdP-Type") XIdPType: String,
        @Header("X-IdP-Name") XIdPName: String,
        @Header("X-Idp-Client-Type") XIdpClientType: String,
        @Field("checkin") checkin: String,
        @Field("configuration") configuration: String
    ): Call<ResponseBody>



    @POST(".")
    fun sendMetaDataForGoogle(
        @Header("X-Site-Id") XSiteId: String,
        @Header("Authorization") Authorization: String,
        @Header("X-User-Name") XUserName: String,
        @Header("X-IdP-Type") XIdPType: String,
        @Header("X-IdP-Name") XIdPName: String,
        @Query("printjobevents") printjobevents: String,
        @Query("eventdata") eventdata: String
    ): Call<ResponseBody>


    @POST(".")
    fun sendMetaDataForOtherIdp(
        @Header("X-Site-Id") XSiteId: String,
        @Header("Authorization") Authorization: String,
        @Header("X-User-Name") XUserName: String,
        @Header("X-IdP-Type") XIdPType: String,
        @Header("X-IdP-Name") XIdPName: String,
        @Query("printjobevents") printjobevents: String,
        @Query("eventdata") eventdata: String
    ): Call<ResponseBody>



    @GET(".")
    fun getPrinterDetailsByNodeId(
        @Header("Authorization") authorization: String,
        @Header("X-User-Name") userName: String,
        @Header("X-IdP-Type") idpType: String,
        @Header("X-IdP-Name") idpName: String
    ): Call<PrinterDetailsResponse>

    @GET(".")
    fun getPrinterDetailsByNodeIdForGoogle(
        @Header("Authorization") authorization: String,
        @Header("X-User-Name") userName: String,
        @Header("X-IdP-Type") idpType: String,
        @Header("X-IdP-Name") idpName: String,
        @Header("X-Idp-Client-Type") XIdpClientType: String
    ): Call<PrinterDetailsResponse>


    @GET(".")
    fun getPrinterForLdap(
        @Header("X-Site-ID") xSiteId: String,
        @Header("X-PrinterLogic-User-Name") userName: String,
        @Header("X-PrinterLogic-Password") password: String
    ): Call<ResponseBody>

    @GET(".")
    fun checkLdapLogin(
        @Header("X-PrinterLogic-Login-Type") XPrinterLogicLoginType: String,
        @Header("X-PrinterLogic-User-Name") userName: String,
        @Header("X-PrinterLogic-Password") password: String
    ): Call<ResponseBody>

    @GET(".")
    fun getPrinterDetailsByPrinterId(
        @Header("Authorization") authorization: String,
        @Header("X-User-Name") userName: String,
        @Header("X-IdP-Type") idpType: String,
        @Header("X-IdP-Name") idpName: String
    ): Call<Any>?

    @GET(".")
    fun getPrinterDetailsByPrinterIdForGoogle(
        @Header("Authorization") authorization: String,
        @Header("X-User-Name") userName: String,
        @Header("X-IdP-Type") idpType: String,
        @Header("X-IdP-Name") idpName: String,
        @Header("X-Idp-Client-Type") XIdpClientType: String
    ): Call<Any>?

    @GET(".")
    fun getPrinterDetailsByPrinterIdForLdap(
        @Header("X-Site-ID") xSiteId: String,
        @Header("X-PrinterLogic-User-Name") userName: String,
        @Header("X-PrinterLogic-Password") password: String
    ): Call<Any>?

    @GET(".")
    fun getPrinterDetailsByNodeIdForLdap(
        @Header("X-Site-ID") xSiteId: String,
        @Header("X-PrinterLogic-User-Name") userName: String,
        @Header("X-PrinterLogic-Password") password: String
    ): Call<PrinterDetailsResponse>?

    @GET(".")
    fun getPrintJobStatuses(
        @Header("Authorization") authorization: String,
        @Header("X-User-Name") userName: String,
        @Header("X-IdP-Type") idpType: String,
        @Header("X-IdP-Name") idpName: String,
        @Query("user_name_like") user_name_like: String,
        @Query("include") include: String
    ): Call<GetJobStatusesResponse>

    @GET(".")
    fun getPrintJobStatusesForGoogle(
        @Header("Authorization") authorization: String,
        @Header("X-User-Name") userName: String,
        @Header("X-IdP-Type") idpType: String,
        @Header("X-IdP-Name") idpName: String,
        @Header("X-Idp-Client-Type") XIdpClientType: String,
        @Query("user_name_like") user_name_like: String,
        @Query("include") include: String
    ): Call<GetJobStatusesResponse>

    @GET(".")
    fun getPrintJobStatusesForQrCode(
        @Header("Authorization") authorization: String,
        @Header("X-User-Name") userName: String,
        @Header("X-IdP-Type") idpType: String,
        @Header("X-IdP-Name") idpName: String,
        @Query("printer_id") printer_id: String,
        @Query("user_name_like") user_name_like: String
    ): Call<GetJobStatusesResponse>

    @GET(".")
    fun getPrintJobStatusesForLdap(
        @Header("X-Site-ID") xSiteId: String,
        @Header("X-PrinterLogic-User-Name") userName: String,
        @Header("X-PrinterLogic-Password") password: String
    ): Call<GetJobStatusesResponse>

    @POST(".")
    fun releaseJobForLdap(
        @Body releaseJobRequest: ReleaseJobRequest,
        @Header("X-Site-ID") xSiteId: String,
        @Header("X-PrinterLogic-User-Name") userName: String,
        @Header("X-PrinterLogic-Password") password: String
    ): Call<ReleaseJobResponse>

    @POST(".")
    fun releaseJobForPullPrinterLdap(
        @Body releaseJobRequest: ReleaseJobRequest,
        @Header("X-Site-ID") xSiteId: String,
        @Header("X-PrinterLogic-User-Name") userName: String,
        @Header("X-PrinterLogic-Password") password: String,
        @Query("format") format: String,
        @Query("t") t: String

    ): Call<ReleaseJobResponse>

    @FormUrlEncoded
    @POST(".")
    fun getPrinterListForLdap(
        @Header("X-Site-Id") XSiteId: String,
        @Header("X-PrinterLogic-User-Name") userName: String,
        @Header("X-PrinterLogic-Password") password: String,
        @Field("checkin") checkin: String,
        @Field("configuration") configuration: String
    ): Call<ResponseBody>

    fun jobStatusCancelForLdap(
        @Header("X-Site-ID") xSiteId: String,
        @Header("X-PrinterLogic-User-Name") userName: String,
        @Header("X-PrinterLogic-Password") password: String,
        @Body jobStatusCancel: CancelJobRequest
    ): Call<CancelJobResponse>


    @POST(".")
    fun releaseJobForPullPrinter(
        @Body releaseJobRequest: ReleaseJobRequest,
        @Header("Authorization") authorization: String,
        @Header("X-User-Name") userName: String,
        @Header("X-IdP-Type") idpType: String,
        @Header("X-IdP-Name") idpName: String,
        @Query("format") format: String,
        @Query("t") t: String

    ): Call<ReleaseJobResponse>

    @POST(".")
    fun releaseJobForPullPrinterForGoogle(
        @Body releaseJobRequest: ReleaseJobRequest,
        @Header("Authorization") authorization: String,
        @Header("X-User-Name") userName: String,
        @Header("X-IdP-Type") idpType: String,
        @Header("X-IdP-Name") idpName: String,
        @Query("format") format: String,
        @Query("t") t: String,
        @Header("X-Idp-Client-Type") XIdpClientType: String

    ): Call<ReleaseJobResponse>

    @POST(".")
    fun releaseJob(
        @Body releaseJobRequest: ReleaseJobRequest,
        @Header("Authorization") authorization: String,
        @Header("X-User-Name") userName: String,
        @Header("X-IdP-Type") idpType: String,
        @Header("X-IdP-Name") idpName: String
    ): Call<ReleaseJobResponse>

    @POST(".")
    fun releaseJobForGoogle(
        @Body releaseJobRequest: ReleaseJobRequest,
        @Header("Authorization") authorization: String,
        @Header("X-User-Name") userName: String,
        @Header("X-IdP-Type") idpType: String,
        @Header("X-IdP-Name") idpName: String,
        @Header("X-Idp-Client-Type") XIdpClientType: String
    ): Call<ReleaseJobResponse>

    @POST(".")
    fun jobStatusCancel(
        @Header("Authorization") Authorization: String,
        @Header("X-User-Name") userName: String,
        @Header("X-IdP-Type") idpType: String,
        @Header("X-IdP-Name") idpName: String,
        @Body jobStatusCancel: CancelJobRequest
    ): Call<CancelJobResponse>

    @POST(".")
    fun jobStatusCancelForGoogle(
        @Header("Authorization") Authorization: String,
        @Header("X-User-Name") userName: String,
        @Header("X-IdP-Type") idpType: String,
        @Header("X-IdP-Name") idpName: String,
        @Body jobStatusCancel: CancelJobRequest,
        @Header("X-Idp-Client-Type") XIdpClientType: String
    ): Call<CancelJobResponse>

    @GET(".")
    fun getPrintersList(
        @Header("Authorization") authorization: String,
        @Header("X-User-Name") userName: String,
        @Header("X-IdP-Type") idpType: String,
        @Header("X-IdP-Name") idpName: String
    ): Call<List<Printer>>

    @GET(".")
    fun getPrintersListForGoogle(
        @Header("Authorization") authorization: String,
        @Header("X-User-Name") userName: String,
        @Header("X-IdP-Type") idpType: String,
        @Header("X-IdP-Name") idpName: String,
        @Header("X-Idp-Client-Type") XIdpClientType: String
    ): Call<List<Printer>>


    @GET(".")
    fun getPrintersListForLdap(
        @Header("X-Site-ID") xSiteId: String,
        @Header("X-PrinterLogic-User-Name") userName: String,
        @Header("X-PrinterLogic-Password") password: String
    ): Call<List<Printer>>

    @FormUrlEncoded
    @POST("https://oauth2.googleapis.com/token")
    fun getIdTokenFromGoogle(
        @Field("grant_type") grant_type: String,
        @Field("code") code: String,
        @Field("redirect_uri") redirect_uri: String,
        @Field("client_id") client_id: String,
        @Field("client_secret") client_secret: String

    ): Call<TokenResponse>

}
