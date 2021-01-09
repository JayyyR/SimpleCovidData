package com.joeracosta.covidtracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.joeracosta.covidtracker.data.CovidData
import io.reactivex.Flowable
import java.util.*

@Dao
interface CovidDataDao {

    @Query("SELECT * FROM covid_data_table WHERE location = :state AND date > :date")
    fun getPostiveRateByStateAfterDate(state: String, date: Date): Flowable<List<CovidData>>

    @Insert
    fun insertData(covidData: List<CovidData>)

    @Query("DELETE FROM covid_data_table")
    fun clearAllData()
}