package com.example.covidtracker.data

import androidx.room.Entity

@Entity(tableName = "covid_data_table")
data class CovidData(

    val state: String? = null,

    val date: Long? = null,

    val postiveTestRate: Long? = null

)