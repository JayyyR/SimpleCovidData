package com.joeracosta.covidtracker.viewmodel

import androidx.lifecycle.ViewModel
import com.joeracosta.covidtracker.data.CovidDataApi
import com.joeracosta.covidtracker.data.CovidDataRepo
import com.joeracosta.covidtracker.data.db.CovidDataDao
import io.reactivex.disposables.CompositeDisposable

class CovidViewModel(private val covidDataApi: CovidDataApi,
                     private val covidDataDao: CovidDataDao): ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val covidDataRepo = CovidDataRepo(
        covidDataApi = covidDataApi,
        compositeDisposable = compositeDisposable,
        covidDataDao = covidDataDao
    )

    init {
        //todo check if I need to refresh data based on time
        refreshData()
    }

    fun refreshData() {
        covidDataRepo.fetchLatestCovidData()
    }


    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }



}