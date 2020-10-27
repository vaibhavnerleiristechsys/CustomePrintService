package com.example.customeprintservice.model

data class DecodedJWTResponse(
	val aud: String? = null,
	val site: String? = null,
	val idp: String? = null,
	val session: String? = null,
	val iss: String? = null,
	val exp: Int? = null,
	val user: String? = null,
	val iat: Int? = null,
	val jti: String? = null
)

