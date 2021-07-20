package com.printerlogic.printerlogic.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = arrayOf(SelectedFile::class), version = 3)
abstract class AppDatabase : RoomDatabase() {

    abstract fun selectedFileDao(): SelectedFileDao

    companion object {
        var dbInstance: AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            if (dbInstance == null) {
                dbInstance = Room
                    .databaseBuilder(context, AppDatabase::class.java, "printer")
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return dbInstance!!
        }
    }
}

