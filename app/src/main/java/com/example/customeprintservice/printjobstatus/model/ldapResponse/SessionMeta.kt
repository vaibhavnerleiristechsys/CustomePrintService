package com.example.customeprintservice.printjobstatus.model.ldapResponse

import com.fasterxml.jackson.annotation.JsonProperty

data class SessionMeta(


    @field:JsonProperty("isPortalUser")
    val isPortalUser: Boolean? = null,

    @field:JsonProperty("userName")
    val userName: String? = null

)