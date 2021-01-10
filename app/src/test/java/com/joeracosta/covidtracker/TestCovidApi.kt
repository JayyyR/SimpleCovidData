package com.joeracosta.covidtracker

import com.joeracosta.covidtracker.data.CovidTrackingProjectApi
import com.joeracosta.covidtracker.data.CovidRawData
import com.joeracosta.covidtracker.data.OurWorldInDataApi
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Response

class TestCovidApi: CovidTrackingProjectApi {

    override fun getStateData(): Flowable<List<CovidRawData>> {
        val test = BehaviorSubject.create<List<CovidRawData>>()
        test.onNext(listOf(CovidRawData()))
        return test.toFlowable(BackpressureStrategy.LATEST)
    }

    override fun getUSData(): Flowable<List<CovidRawData>> {
        val test = BehaviorSubject.create<List<CovidRawData>>()
        test.onNext(listOf(CovidRawData()))
        return test.toFlowable(BackpressureStrategy.LATEST)
    }
}

class TestOurWorldInDataApi: OurWorldInDataApi {
    override fun vaccinationData(): Flowable<ResponseBody> {
        val test = BehaviorSubject.create<ResponseBody>()
        test.onNext(ResponseBody.create(
            MediaType.parse("application/json"),
            "{\"key\":[\"somestuff\"]}"
        ))
        return test.toFlowable(BackpressureStrategy.LATEST)
    }


}