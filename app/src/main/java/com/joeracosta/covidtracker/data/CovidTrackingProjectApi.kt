package com.joeracosta.covidtracker.data

import io.reactivex.Flowable
import retrofit2.http.GET

interface CovidTrackingProjectApi {

    @GET("getAjaxData?id=us_compare_trends_data")
    fun getStateData(): Flowable<CovidRawResponse>

    @GET("getAjaxData?id=us_trend_data")
    fun getUSData(): Flowable<USRawResponse>
}