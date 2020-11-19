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
                    negativeIncrease?.let {
                        totalTestResultsIncrease?.let {
                            if (positiveIncrease == 0L) {
                                positiveIncrease.toDouble()
                            } else {
                                (positiveIncrease.toDouble() / (totalTestResultsIncrease)) * 100
                            }
                        }
                    }
                },
            hospitalizedCurrently = hospitalizedCurrently
        )
    }
}