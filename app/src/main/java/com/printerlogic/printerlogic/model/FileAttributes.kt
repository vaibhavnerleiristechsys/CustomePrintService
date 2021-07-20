package com.printerlogic.printerlogic.model

import java.io.Serializable

data class FileAttributes(
    var fileRealPath:String? = null,
    var fileName: String? = null,
    var fileSize: Long? = null,
    var fileSelectedDate: String? = null
):Serializable