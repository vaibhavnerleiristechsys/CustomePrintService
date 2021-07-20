package com.printerlogic.printerlogic.model

import com.google.gson.annotations.SerializedName

data class TokenResponse (
    @field:SerializedName("token")
    val token: String? = null
)