package com.example.covidtracker.data

import androidx.room.Entity
import java.util.*

@Entity(tableName = "covid_data_table")
data class CovidData(

    val state: String? = null,

    val date: Date? = null,

    val postiveTestRate: Long? = null

)