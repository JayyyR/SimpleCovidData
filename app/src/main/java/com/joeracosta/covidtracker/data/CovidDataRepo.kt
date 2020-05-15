package com.joeracosta.covidtracker.data

import com.joeracosta.covidtracker.addToComposite
import com.joeracosta.covidtracker.data.db.CovidDataDao
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CovidDataRepo(
    private val covidDataApi: CovidDataApi,
    private val compositeDisposable: CompositeDisposable,
    private val covidDataDao: CovidDataDao
) {


    fun fetchLatestCovidData() {
        covidDataApi.getStateData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val covidData = it.map { rawData ->
                    rawData.toCovidData()
                }
                updateDatabaseData(covidData)

            }, {
                println()
                //todo error
            }).addToComposite(compositeDisposable)
    }

    fun updateDatabaseData(data: List<CovidData>) {
        Single.fromCallable {
            covidDataDao.clearAllData()
        }
            .subscribeOn(Schedulers.io())
            .subscribe({
                Single.fromCallable {
                    covidDataDao.insertData(data)
                }
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        println()
                    }, {
                        //todo error
                    })

            }, {
                //todo error
            }).addToComposite(compositeDisposable)
    }

}