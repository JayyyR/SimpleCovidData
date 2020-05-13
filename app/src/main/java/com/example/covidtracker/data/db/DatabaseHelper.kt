package com.example.covidtracker.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.*

class DatabaseHelper(val appContext: Context){

    val covidDb by lazy {
        Room.databaseBuilder(appContext, CovidDatabse::class.java, DB_NAME)
            .build()
    }

    companion object {
        const val DB_NAME = "covid_db"
    }
}

@TypeConverters(value = [Converters::class])
abstract class CovidDatabse: RoomDatabase() {
    abstract fun covidDataDao(): CovidDataDao
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}