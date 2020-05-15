package com.example.covidtracker.data

import android.annotation.SuppressLint
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "covid_data_table")
data class CovidData(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val state: State? = null,

    val date: Date? = null,

    val postiveTestRate: Double? = null
)


data class CovidRawData(

    @SerializedName("state")
    val state: State? = null,

    @SerializedName("date")
    val date: String? = null,

    @SerializedName("positive")
    val positive: Long? = null,

    @SerializedName("negative")
    val negative: Long? = null

) {
    @SuppressLint("SimpleDateFormat")
    fun toCovidData(): CovidData {
        return CovidData(
            state = state,
            date = date?.let {
                val dateFormat = SimpleDateFormat("yyyyMMdd")
                dateFormat.parse(it)
            },
            postiveTestRate = positive?.let {
                negative?. let {

                    if (positive == 0L) {
                        positive.toDouble()
                    } else {
                        (positive.toDouble() / (positive + negative)) * 100
                    }
                }
            }
        )
    }
}