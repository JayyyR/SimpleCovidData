package com.joeracosta.covidtracker.data

import android.annotation.SuppressLint
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "covid_data_table")
data class CovidData(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val state: State? = null,

    val date: Date? = null,

    val postiveTestRate: Double? = null,

    val postiveTestRateSevenDayAvg: Double? = null,

    val hospitalizedCurrently: Int? = null
)


data class CovidRawData(

    @SerializedName("state")
    val state: State? = null,

    @SerializedName("date")
    val date: String? = null,

    @SerializedName("positiveIncrease")
    val positiveIncrease: Long? = null,

    @SerializedName("negativeIncrease")
    val negativeIncrease: Long? = null,

    @SerializedName("totalTestResultsIncrease")
    val totalTestResultsIncrease: Long? = null,

    @SerializedName("hospitalizedCurrently")
    val hospitalizedCurrently: Int? = null

) {
    @SuppressLint("SimpleDateFormat")
    fun toCovidData(): CovidData {
        return CovidData(
            state = state,
            date = date?.let {
                val dateFormat = SimpleDateFormat("yyyyMMdd")
                dateFormat.parse(it)
            },
            postiveTestRate =
            positiveIncrease?.let {

                //the data is funky sometimes, we need to work with what we have and throw out anomalies
                val totalToCalculateAgainst =
                    if (totalTestResultsIncrease != null && totalTestResultsIncrease > positiveIncrease) { //throw away data where total = positive. It happens and it's garbage data
                        totalTestResultsIncrease
                    } else if (negativeIncrease != null && negativeIncrease > 0) {
                        positiveIncrease + negativeIncrease
                    } else {
                        null
                    }

                val postiveTestRate = when {
                    positiveIncrease == 0L -> {
                        positiveIncrease.toDouble()
                    }
                    totalToCalculateAgainst != null -> {
                        (positiveIncrease.toDouble() / (totalToCalculateAgainst)) * 100
                    }
                    else -> {
                        null
                    }
                }

                //todo there are still anomalies like this...should I get rid of em?
                /* Weird anomaly example with NJ in SEPT
                if (postiveTestRate == 29.20353982300885 && state == State.NEW_JERSEY) {
                    println()
                }*/

                postiveTestRate

            },
            hospitalizedCurrently = hospitalizedCurrently
        )
    }
}