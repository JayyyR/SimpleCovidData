package com.example.covidtracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.covidtracker.data.CovidData
import java.util.*

@Dao
interface CovidDataDao {

    @Query("SELECT * FROM covid_data_table WHERE state = :state AND date > :date")
    fun getPostiveRateByStateAfterDate(state: String, date: Date): List<CovidData>

    @Insert
    fun insertData(covidData: List<CovidData>)

    @Query("DELETE FROM covid_data_table")
    fun clearAllData()
}