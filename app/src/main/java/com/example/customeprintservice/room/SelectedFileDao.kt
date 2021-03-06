package com.example.customeprintservice.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Single

@Dao
interface SelectedFileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(users: List<SelectedFile>)

    @Query("SELECT * FROM selectedfile")
    fun loadAll(): List<SelectedFile>

    @Query("delete from selectedfile where is_from_api = 1")
    fun deleteItemsFromApi()

}