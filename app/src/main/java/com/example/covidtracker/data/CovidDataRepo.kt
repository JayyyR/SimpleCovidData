package com.example.covidtracker.data

import com.example.covidtracker.addToComposite
import com.example.covidtracker.data.db.CovidDataDao
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CovidDataRepo(private val covidDataApi: CovidDataApi,
                    private val compositeDisposable: CompositeDisposable,
                    private val covidDataDao: CovidDataDao) {


    fun fetchLatestCovidData() {
        covidDataApi.getStateData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val covidData = it.map { rawData ->
                    rawData.toCovidData()
                }

                println()
            }, {
                println()
                //todo error
            }).addToComposite(compositeDisposable)
    }
}