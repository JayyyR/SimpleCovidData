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

    val location: Location? = null,

    val date: Date? = null,

    val newCasesSevenDayAvg: Double? = null,

    val newDeathsSevenDayAvg: Double? = null,

    val newPeopleVaccinated: Long? = null,

    val newVaccinationsSevenDayAvg: Double? = null,

    val totalPeopleVaccinated: Long? = null,

    val percentOfPopulationVaccinated: Double? = null
)

data class CovidRawResponse(
    @SerializedName("us_compare_trends_data")
    val covidData: List<CovidRawData>
)

data class USRawResponse(
    @SerializedName("us_trend_data")
    val usData: List<RawUSData>
)

data class RawUSData(

    @SerializedName("date")
    val date: String? = null,

    @SerializedName("seven_day_avg_new_cases")
    val newCasesSevenDayAvg: Double? = null,

    @SerializedName("seven_day_avg_new_deaths")
    val newDeathsSevenDayAvg: Double? = null,

    @SerializedName("state")
    val location: String? = null
) {
    @SuppressLint("SimpleDateFormat")
    fun toCovidData(): CovidData? {
        if (!location.equals("United States", true)) return null

        return CovidData(
            location = Location.UNITED_STATES,
            date = date?.let {
                val dateFormat = SimpleDateFormat("MMM dd yyyy")
                dateFormat.parse(it)
            },
            newCasesSevenDayAvg = newCasesSevenDayAvg,
            newDeathsSevenDayAvg = newDeathsSevenDayAvg
        )
    }
}

data class CovidRawData(

    @SerializedName("state")
    val location: Location? = null,

    @SerializedName("submission_date")
    val date: String? = null,

    @SerializedName("seven_day_avg_new_deaths")
    val newDeathsSevenDayAvg: Double? = null,

    @SerializedName("seven_day_avg_new_cases")
    val newCasesSevenDayAvg: Double? = null

) {
    @SuppressLint("SimpleDateFormat")
    fun toCovidData(): CovidData {
        return CovidData(
            location = location,
            date = date?.let {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                dateFormat.parse(it)
            },
            newCasesSevenDayAvg = newCasesSevenDayAvg,
            newDeathsSevenDayAvg = newDeathsSevenDayAvg
        )
    }
}