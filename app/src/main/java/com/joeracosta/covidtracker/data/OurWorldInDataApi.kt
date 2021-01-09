package com.joeracosta.covidtracker.data

import io.reactivex.Flowable
import okhttp3.ResponseBody
import retrofit2.http.GET

interface OurWorldInDataApi {

    @GET("data/vaccinations/country_data/United%20States.csv")
    fun vaccinationData(): Flowable<ResponseBody>
}