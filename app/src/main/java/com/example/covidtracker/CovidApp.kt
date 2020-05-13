package com.example.covidtracker

import android.app.Application
import com.example.covidtracker.data.db.DatabaseHelper

class CovidApp: Application() {

    val databaseHelper by lazy {
        DatabaseHelper(this)
    }

}