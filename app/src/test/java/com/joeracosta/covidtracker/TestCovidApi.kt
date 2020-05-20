package com.joeracosta.covidtracker

import com.joeracosta.covidtracker.data.CovidDataApi
import com.joeracosta.covidtracker.data.CovidRawData
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject

class TestCovidApi: CovidDataApi {

    override fun getStateData(): Flowable<List<CovidRawData>> {
        val test = BehaviorSubject.create<List<CovidRawData>>()
        test.onNext(listOf(CovidRawData()))
        return test.toFlowable(BackpressureStrategy.LATEST)
    }
}