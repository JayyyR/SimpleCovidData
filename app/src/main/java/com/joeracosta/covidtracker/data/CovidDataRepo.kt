package com.joeracosta.covidtracker.data

import com.joeracosta.covidtracker.addToComposite
import com.joeracosta.covidtracker.data.db.CovidDataDao
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class CovidDataRepo(
    private val covidDataApi: CovidDataApi,
    private val compositeDisposable: CompositeDisposable,
    private val covidDataDao: CovidDataDao
) {


    /**
     * Fetches data from server, returns a stream that indicates whether we succeeded gathering and
     * storing latest data
     */
    fun fetchLatestCovidData(): Observable<Boolean> {
        val successObservable = BehaviorSubject.create<Boolean>()
        covidDataApi.getStateData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val covidData = it.map { rawData ->
                    rawData.toCovidData()
                }

                val dataWithSevenDayAverages = calculateSevenDayAverages(covidData)

                updateDatabaseData(dataWithSevenDayAverages).subscribe({ success ->
                    successObservable.onNext(success)
                }, {
                    successObservable.onNext(false)
                })

            }, {
                successObservable.onNext(false)
            }).addToComposite(compositeDisposable)

        return successObservable
    }

    private fun calculateSevenDayAverages(covidData: List<CovidData>): List<CovidData> {

        val mapOfStateData = hashMapOf<State, ArrayList<CovidData>>()

        covidData.forEach {

            val state = it.state ?: return@forEach

            //add empty list of needed
            if (!mapOfStateData.containsKey(state)) {
                mapOfStateData[state] = arrayListOf()
            }

            mapOfStateData[state]?.add(it)
        }

        val listOfStateDataWithAverages = mapOfStateData.values.map { list ->
            //sort by date
            list.sortBy {
                it.date
            }

            //calculate averages
            val listDataWithSevenDayAvgs = list.mapIndexed { index, covidData ->

                val lastSevenDaysPositiveRate = if (index >= 6) {
                    list.slice(index - 6..index).map {
                        it.postiveTestRate
                    }
                } else {
                    null
                }

                //sometimes days have null data because of garabage from the API but we still want to calculate averages from the data we have
                val positiveRateFromActualDaysWithDataFromLastSeven = lastSevenDaysPositiveRate?.filterNotNull()

                val postiveTestRateSevenDayAvg =
                    if (positiveRateFromActualDaysWithDataFromLastSeven != null) {
                        (positiveRateFromActualDaysWithDataFromLastSeven.sumByDouble { it }) / positiveRateFromActualDaysWithDataFromLastSeven.size
                    } else {
                        null
                    }

                covidData.copy(
                    postiveTestRateSevenDayAvg = postiveTestRateSevenDayAvg
                )

            }

            listDataWithSevenDayAvgs
        }

        return listOfStateDataWithAverages.flatten()

    }

    private fun updateDatabaseData(data: List<CovidData>): Observable<Boolean> {
        val successObservable = BehaviorSubject.create<Boolean>()

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
                        successObservable.onNext(true)
                    }, {
                        successObservable.onNext(false)
                    })

            }, {
                successObservable.onNext(false)
            }).addToComposite(compositeDisposable)

        return successObservable
    }

}