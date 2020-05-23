package com.joeracosta.covidtracker.data

import com.joeracosta.covidtracker.addToComposite
import com.joeracosta.covidtracker.data.db.CovidDataDao
import io.reactivex.Maybe
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

                val dataWithThreeDayAverages = calculateThreeDayAverages(covidData)

                updateDatabaseData(dataWithThreeDayAverages).subscribe({ success ->
                    successObservable.onNext(success)
                }, {
                    successObservable.onNext(false)
                })

            }, {
                successObservable.onNext(false)
            }).addToComposite(compositeDisposable)

        return successObservable
    }

    private fun calculateThreeDayAverages(covidData: List<CovidData>): List<CovidData> {

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
            val listDataWithThreeDayAvgs = list.mapIndexed { index, covidData ->
                val currentPostiveRate = covidData.postiveTestRate
                val positiveRateOneDayAgo = list.getOrNull(index - 1)?.postiveTestRate
                val positiveRateTwoDaysAgo = list.getOrNull(index - 2)?.postiveTestRate

                val threeDayAvg =
                    if (currentPostiveRate != null && positiveRateOneDayAgo != null && positiveRateTwoDaysAgo != null) {
                        (positiveRateOneDayAgo + positiveRateTwoDaysAgo + currentPostiveRate) / 3.0
                    } else {
                        null
                    }

                covidData.copy(
                    threeDayPostiveTestRateAvg = threeDayAvg
                )

            }

            listDataWithThreeDayAvgs
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