package com.example.covidtracker

import android.app.Application
import com.example.covidtracker.data.db.DatabaseHelper
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class CovidApp: Application() {

    val databaseHelper by lazy {
        DatabaseHelper(this)
    }

    val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://covidtracking.com/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()
    }

}

