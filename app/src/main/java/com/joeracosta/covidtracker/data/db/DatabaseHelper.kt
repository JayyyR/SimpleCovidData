package com.joeracosta.covidtracker.data.db

import android.content.Context
import androidx.room.*
import com.joeracosta.covidtracker.data.CovidData
import com.joeracosta.covidtracker.data.State
import java.util.*

class DatabaseHelper(val appContext: Context){

    val covidDb by lazy {
        Room.databaseBuilder(appContext, CovidDatabase::class.java, DB_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    companion object {
        const val DB_NAME = "covid_db"
    }
}

@Database(
    entities = [CovidData::class],
    version = 3
)
@TypeConverters(value = [Converters::class])
abstract class CovidDatabase: RoomDatabase() {
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

    @TypeConverter
    fun fromState(value: State?): String? {
        return value?.postalCode
    }

    @TypeConverter
    fun toState(postalCode: String?): State? {
        return enumValues<State>().find {
            it.postalCode == postalCode
        }
    }
}