package com.example.covidtracker.data

import io.reactivex.Flowable
import retrofit2.http.GET

interface CovidDataApi {

    @GET("states/daily.json")
    fun getStateData(): Flowable<List<CovidRawData>>
}