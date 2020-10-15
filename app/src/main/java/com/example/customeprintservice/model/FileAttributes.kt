package com.example.customeprintservice.model

import java.util.*

data class FileAttributes(
    var fileRealPath:String? = null,
    var fileName: String? = null,
    var fileSize: Long? = null,
    var fileSelectedDate: String? = null
)