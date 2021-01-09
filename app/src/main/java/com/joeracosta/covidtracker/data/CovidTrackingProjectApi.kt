package com.joeracosta.covidtracker.data

import io.reactivex.Flowable
import retrofit2.http.GET

interface CovidTrackingProjectApi {

    @GET("states/daily.json")
    fun getStateData(): Flowable<List<CovidRawData>>

    @GET("us/daily.json")
    fun getUSData(): Flowable<List<CovidRawData>>
}