package com.example.customeprintservice.model

import com.google.gson.annotations.SerializedName

data class IdpResponse(

	@field:SerializedName("idp_require_login")
	val idpRequireLogin: Boolean? = null,

	@field:SerializedName("auth_type")
	val authType: String? = null,

	@field:SerializedName("token_uri")
	val tokenUri: String? = null,

	@field:SerializedName("desktopLoginUrl")
	val desktopLoginUrl: String? = null,

	@field:SerializedName("auth_uri")
	val authUri: String? = null,

	@field:SerializedName("idp_id")
	val idpId: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("idp_type")
	val idpType: String? = null,

	@field:SerializedName("idp_use_local_server")
	val idpUseLocalServer: Boolean? = null
)
