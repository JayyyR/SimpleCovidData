package com.joeracosta.covidtracker.data

import io.reactivex.Flowable
import okhttp3.ResponseBody
import retrofit2.http.GET

interface OurWorldInDataApi {

    @GET("country_data/United%20States.csv")
    fun vaccinationUSData(): Flowable<ResponseBody>

    @GET("us_state_vaccinations.csv")
    fun vaccinationStateData(): Flowable<ResponseBody>
}