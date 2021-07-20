package com.printerlogic.printerlogic.model

data class DecodedJWTResponse(
	val aud: String? = null,
	val site: String? = null,
	val idp: String? = null,
	val session: String? = null,
	val iss: String? = null,
	val exp: Int? = null,
	val user: String? = null,
	val iat: Int? = null,
	val jti: String? = null,
	val email: String? = null,
	val azp: String? = null,
	val sub: String? = null,
	val hd: String? = null,
	val email_verified: String? = null,
	val at_hash: String? = null,
	val name: String? = null,
	val picture: String? = null,
	val given_name: String? = null,
	val family_name: String? = null,
	val locale: String? = null

)

