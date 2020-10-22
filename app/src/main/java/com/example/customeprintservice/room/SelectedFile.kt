package com.example.customeprintservice.room

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
    var fileSelectedDate:String? = null
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}