package com.example.covidtracker

import android.app.Application
import com.example.covidtracker.data.db.DatabaseHelper
import retrofit2.Retrofit

class CovidApp: Application() {

    val databaseHelper by lazy {
        DatabaseHelper(this)
    }

    val retrofit by lazy {

        Retrofit.Builder()
            .baseUrl("https://covidtracking.com/api/v1/")
            .addConverterFactory()
    }

}