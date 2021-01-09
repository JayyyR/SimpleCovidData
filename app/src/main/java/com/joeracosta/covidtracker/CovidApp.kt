package com.joeracosta.covidtracker

import android.app.Application
import android.content.Context
import com.joeracosta.covidtracker.data.LastUpdatedData
import com.joeracosta.covidtracker.data.LastUpdatedDataConcrete
import com.joeracosta.covidtracker.data.StringGetter
import com.joeracosta.covidtracker.data.StringGetterConcrete
import com.joeracosta.covidtracker.data.db.DatabaseHelper
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class CovidApp: Application() {

    val databaseHelper by lazy {
        DatabaseHelper(this)
    }

    val lastUpdatedData by lazy {
        LastUpdatedDataConcrete(
            getSharedPreferences(CovidApp::javaClass.name, Context.MODE_PRIVATE)
        )
    }

    val stringGetter by lazy {
        StringGetterConcrete(
            this.resources
        )
    }

    val covidTrackingRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://covidtracking.com/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()
    }

    val ourWorldInDataRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/owid/covid-19-data/master/public/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()
    }

}

