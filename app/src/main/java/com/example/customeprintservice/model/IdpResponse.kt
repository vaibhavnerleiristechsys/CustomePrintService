package com.example.customeprintservice.model

import com.google.gson.annotations.SerializedName

data class IdpResponse(

	@field:SerializedName("idp_require_login")
	val idpRequireLogin: Boolean,

	@field:SerializedName("auth_type")
	val authType: String,

	@field:SerializedName("token_uri")
	val tokenUri: String,

	@field:SerializedName("desktopLoginUrl")
	val desktopLoginUrl: String,

	@field:SerializedName("auth_uri")
	val authUri: String,

	@field:SerializedName("idp_id")
	val idpId: String,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("client_id")
	val client_id: String,


	@field:SerializedName("idp_type")
	val idpType: String,

	@field:SerializedName("idp_use_local_server")
	val idpUseLocalServer: Boolean
)
