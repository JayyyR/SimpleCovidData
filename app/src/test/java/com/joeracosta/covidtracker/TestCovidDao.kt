package com.joeracosta.covidtracker

import com.joeracosta.covidtracker.data.CovidData
import com.joeracosta.covidtracker.data.State
import com.joeracosta.covidtracker.data.db.CovidDataDao
import io.reactivex.Flowable
import java.util.*

class TestCovidDao: CovidDataDao {
    override fun getPostiveRateByStateAfterDate(
        state: String,
        date: Date
    ): Flowable<List<CovidData>> {
       return Flowable.fromCallable {
           listOf(CovidData(
               state = State.values().find { it.postalCode == state }
           ))
       }
    }

    override fun insertData(covidData: List<CovidData>) {
    }

    override fun clearAllData() {
    }
}