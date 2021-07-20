package com.printerlogic.printerlogic.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class SelectedFile(

    @ColumnInfo(name = "first_name")
    var filePath: String? = null,

    @ColumnInfo(name = "last_name")
    var fileName: String? = null,

    @ColumnInfo(name = "file_selected_date")
    var fileSelectedDate: String? = null,

    @ColumnInfo(name = "is_from_api")
    var isFromApi: Boolean? = null,

    @ColumnInfo(name = "work_station_id")
    var workStationId: Int? = null,

    @ColumnInfo(name = "Fqueue_id")
    var queueId: Int? = null,

    @ColumnInfo(name = "job_num")
    var jobNum: Int? = null,

    @ColumnInfo(name = "job_type")
    var jobType: String? = null,

    @ColumnInfo(name = "user_name")
    var userName: String? = null,

    @ColumnInfo(name = "printerId")
    var printerId: Int? = null,

    @ColumnInfo(name = "jobSize")
    var jobSize: String? = null,

    @ColumnInfo(name = "pages")
    var pages: Int? = null,

    @ColumnInfo(name = "sourceMachine")
    var sourceMachine: String? = null,

    @ColumnInfo(name = "jobId")
    var jobId: String? = null


) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
